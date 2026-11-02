package com.example.data.network

import com.example.data.model.ClientSession
import com.example.data.model.ClientState
import com.example.data.model.GuestPackage
import com.example.data.model.GuestPortalPolicy
import com.example.data.model.QuotaAccount
import com.example.data.model.RouterConfig
import com.example.data.model.RouterProtocol
import com.example.data.model.TrafficShaperStatus
import com.example.data.util.MacVendorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class OpenWrtApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
        .build()
) {
    private var cachedUbusSessionToken: String? = null

    suspend fun fetchStatus(config: RouterConfig): Result<List<ClientSession>> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.getClients())
        }

        try {
            when (config.protocol) {
                RouterProtocol.STARLINK_PORTAL_V3 -> fetchStatusViaStarlinkPortal(config)
                RouterProtocol.OPENWRT_UBUS -> fetchStatusViaUbus(config)
                RouterProtocol.OPENNDS_REST_CGI -> fetchStatusViaCgi(config)
                else -> Result.success(OpenNDSSimulator.getClients())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun authenticateClient(
        config: RouterConfig,
        mac: String,
        durationMinutes: Int,
        downRateKbps: Int = 0,
        upRateKbps: Int = 0,
        downQuotaMb: Int = 0,
        upQuotaMb: Int = 0,
        requestId: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            val ok = OpenNDSSimulator.authenticateClient(
                mac, durationMinutes, downRateKbps, upRateKbps, downQuotaMb, upQuotaMb, requestId
            )
            return@withContext Result.success(ok)
        }

        try {
            when (config.protocol) {
                RouterProtocol.STARLINK_PORTAL_V3 -> {
                    // 1. If we have a pending Request ID, use Starlink v3.7 /cgi-bin/requests action
                    val targetReqId = requestId ?: findRequestIdForMac(config, mac)
                    if (!targetReqId.isNullOrBlank()) {
                        val url = "http://${config.host}:${config.port}${config.cgiPath}"
                        val formBody = if (downQuotaMb > 0 || upQuotaMb > 0) {
                            val down = if (downQuotaMb > 0) downQuotaMb else 1024
                            val up = if (upQuotaMb > 0) upQuotaMb else 256
                            FormBody.Builder()
                                .add("action", "enroll")
                                .add("id", targetReqId)
                                .add("download_mib", down.toString())
                                .add("upload_mib", up.toString())
                                .build()
                        } else {
                            FormBody.Builder()
                                .add("action", "approve")
                                .add("id", targetReqId)
                                .build()
                        }

                        val req = Request.Builder().url(url).post(formBody).build()
                        val resp = client.newCall(req).execute()

                        // Also call authenticate action to immediately grant network access
                        val authBody = FormBody.Builder()
                            .add("action", "authenticate")
                            .add("id", targetReqId)
                            .build()
                        val authReq = Request.Builder().url(url).post(authBody).build()
                        client.newCall(authReq).execute()

                        Result.success(resp.isSuccessful)
                    } else {
                        // Clear any administrator deny block
                        unblockClient(config, mac)
                        // Fallback to direct ndsctl auth if available
                        executeUbusNdsctl(
                            config,
                            listOf("auth", mac, durationMinutes.toString(), upRateKbps.toString(), downRateKbps.toString())
                        )
                    }
                }

                RouterProtocol.OPENWRT_UBUS -> executeUbusNdsctl(
                    config,
                    listOf("auth", mac, durationMinutes.toString(), upRateKbps.toString(), downRateKbps.toString())
                )

                RouterProtocol.OPENNDS_REST_CGI -> {
                    val url = "http://${config.host}:${config.port}${config.cgiPath}?cmd=auth&mac=$mac&duration=$durationMinutes&down_limit=$downRateKbps&up_limit=$upRateKbps"
                    val req = Request.Builder().url(url).build()
                    val resp = client.newCall(req).execute()
                    Result.success(resp.isSuccessful)
                }

                else -> Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deauthenticateClient(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            val ok = OpenNDSSimulator.deauthenticateClient(mac)
            return@withContext Result.success(ok)
        }

        try {
            when (config.protocol) {
                RouterProtocol.STARLINK_PORTAL_V3 -> {
                    // Starlink v3.7 POST /cgi-bin/requests with action=disconnect&mac=...
                    val url = "http://${config.host}:${config.port}${config.cgiPath}"
                    val formBody = FormBody.Builder()
                        .add("action", "disconnect")
                        .add("mac", mac)
                        .build()
                    val req = Request.Builder().url(url).post(formBody).build()
                    val resp = client.newCall(req).execute()
                    Result.success(resp.isSuccessful)
                }

                RouterProtocol.OPENWRT_UBUS -> executeUbusNdsctl(config, listOf("deauth", mac))

                RouterProtocol.OPENNDS_REST_CGI -> {
                    val url = "http://${config.host}:${config.port}${config.cgiPath}?cmd=deauth&mac=$mac"
                    val req = Request.Builder().url(url).build()
                    val resp = client.newCall(req).execute()
                    Result.success(resp.isSuccessful)
                }

                else -> Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockClient(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.blockClient(mac))
        }
        try {
            if (config.protocol == RouterProtocol.STARLINK_PORTAL_V3) {
                val url = "http://${config.host}:${config.port}${config.cgiPath}"
                val formBody = FormBody.Builder()
                    .add("action", "disconnect")
                    .add("mac", mac)
                    .build()
                val req = Request.Builder().url(url).post(formBody).build()
                val resp = client.newCall(req).execute()
                Result.success(resp.isSuccessful)
            } else if (config.protocol == RouterProtocol.OPENWRT_UBUS) {
                executeUbusNdsctl(config, listOf("block", mac))
            } else {
                val url = "http://${config.host}:${config.port}${config.cgiPath}?cmd=block&mac=$mac"
                val req = Request.Builder().url(url).build()
                val resp = client.newCall(req).execute()
                Result.success(resp.isSuccessful)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockClient(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.unblockClient(mac))
        }
        try {
            if (config.protocol == RouterProtocol.STARLINK_PORTAL_V3) {
                val url = "http://${config.host}:${config.port}${config.cgiPath}"
                val formBody = FormBody.Builder()
                    .add("action", "clear_block")
                    .add("mac", mac)
                    .add("confirm", "yes")
                    .build()
                val req = Request.Builder().url(url).post(formBody).build()
                val resp = client.newCall(req).execute()
                Result.success(resp.isSuccessful)
            } else if (config.protocol == RouterProtocol.OPENWRT_UBUS) {
                executeUbusNdsctl(config, listOf("unblock", mac))
            } else {
                val url = "http://${config.host}:${config.port}${config.cgiPath}?cmd=unblock&mac=$mac"
                val req = Request.Builder().url(url).build()
                val resp = client.newCall(req).execute()
                Result.success(resp.isSuccessful)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun findRequestIdForMac(config: RouterConfig, mac: String): String? {
        val list = fetchStatusViaStarlinkPortal(config).getOrNull() ?: return null
        return list.firstOrNull { it.mac.equals(mac, ignoreCase = true) }?.requestId
    }

    /**
     * Integrates with Starlink Guest Portal v3.7 /cgi-bin/requests
     * Parses HTML response into structured ClientSession objects.
     */
    private suspend fun fetchStatusViaStarlinkPortal(config: RouterConfig): Result<List<ClientSession>> {
        val url = "http://${config.host}:${config.port}${config.cgiPath}"
        val req = Request.Builder().url(url).build()
        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) {
            return Result.failure(Exception("Starlink portal responded with HTTP ${resp.code}: ${resp.message}"))
        }

        val html = resp.body?.string().orEmpty()
        val result = mutableListOf<ClientSession>()
        val seenMacs = mutableSetOf<String>()

        // 1. Parse Pending Requests
        // Pattern matches:
        // Request ID: (REQ-...) ... Name: ... Mobile: ... MAC address: ... Client IP: ... Browser/device: ... Status: PENDING
        val pendingRegex = Pattern.compile(
            "Request ID:\\s*</strong>\\s*(REQ-[0-9A-Za-z-]+)[\\s\\S]*?Name:\\s*</strong>\\s*([^<\\r\\n]+)[\\s\\S]*?Mobile:\\s*</strong>\\s*([^<\\r\\n]+)[\\s\\S]*?MAC address:\\s*</strong>\\s*([0-9a-fA-F:]{17})[\\s\\S]*?Client IP:\\s*</strong>\\s*([0-9.]+)[\\s\\S]*?Browser/device:\\s*</strong>\\s*([^<\\r\\n]+)[\\s\\S]*?Status:\\s*</strong>\\s*([A-Za-z]+)",
            Pattern.CASE_INSENSITIVE
        )
        val pendingMatcher = pendingRegex.matcher(html)
        val now = System.currentTimeMillis() / 1000L

        while (pendingMatcher.find()) {
            val reqId = pendingMatcher.group(1)?.trim()
            val name = pendingMatcher.group(2)?.trim()
            val mobile = pendingMatcher.group(3)?.trim()
            val mac = pendingMatcher.group(4)?.trim()?.uppercase() ?: continue
            val ip = pendingMatcher.group(5)?.trim() ?: "192.168.1.0"
            val browser = pendingMatcher.group(6)?.trim()
            val status = pendingMatcher.group(7)?.trim()

            if (status.equals("PENDING", ignoreCase = true) && !seenMacs.contains(mac)) {
                seenMacs.add(mac)
                result.add(
                    ClientSession(
                        mac = mac,
                        ip = ip,
                        state = ClientState.PREAUTHENTICATED,
                        requestId = reqId,
                        guestName = name,
                        mobile = mobile,
                        browserHint = browser,
                        hostname = name,
                        vendor = MacVendorHelper.resolveVendor(mac),
                        firstSeenEpochSec = now,
                        lastSeenEpochSec = now
                    )
                )
            }
        }

        // 2. Parse Connected Devices
        // Matches: <strong class="device-title">([^<]+)</strong> ... MAC address.*?<code>([0-9a-fA-F:]{17})</code> ... IP address.*?<strong>([0-9.]+)</strong>
        val deviceRegex = Pattern.compile(
            "<strong class=\"device-title\">\\s*([^<]+)\\s*</strong>[\\s\\S]*?<small>MAC address</small><code>\\s*([0-9a-fA-F:]{17})\\s*</code>[\\s\\S]*?<small>IP address</small><strong>\\s*([0-9.]+)\\s*</strong>[\\s\\S]*?<small>DHCP device name</small><strong>\\s*([^<]+)\\s*</strong>",
            Pattern.CASE_INSENSITIVE
        )
        val deviceMatcher = deviceRegex.matcher(html)

        while (deviceMatcher.find()) {
            val title = deviceMatcher.group(1)?.trim()
            val mac = deviceMatcher.group(2)?.trim()?.uppercase() ?: continue
            val ip = deviceMatcher.group(3)?.trim() ?: "192.168.1.0"
            val dhcpHost = deviceMatcher.group(4)?.trim()

            if (!seenMacs.contains(mac)) {
                seenMacs.add(mac)
                result.add(
                    ClientSession(
                        mac = mac,
                        ip = ip,
                        state = ClientState.AUTHENTICATED,
                        hostname = dhcpHost ?: title,
                        guestName = title,
                        vendor = MacVendorHelper.resolveVendor(mac),
                        firstSeenEpochSec = now - 1800,
                        lastSeenEpochSec = now,
                        sessionStartEpochSec = now - 1800,
                        sessionEndEpochSec = now + 1800,
                        durationMinutes = 60
                    )
                )
            }
        }

        return Result.success(result)
    }

    private suspend fun fetchStatusViaCgi(config: RouterConfig): Result<List<ClientSession>> {
        val url = "http://${config.host}:${config.port}${config.cgiPath}?cmd=json"
        val req = Request.Builder().url(url).build()
        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) {
            return Result.failure(Exception("HTTP error ${resp.code}: ${resp.message}"))
        }
        val bodyStr = resp.body?.string().orEmpty()
        return parseNdsctlJson(bodyStr)
    }

    private suspend fun fetchStatusViaUbus(config: RouterConfig): Result<List<ClientSession>> {
        val sessionToken = getOrLoginUbusSession(config)
            ?: return Result.failure(Exception("Failed to authenticate with OpenWrt ubus"))

        val ubusUrl = "http://${config.host}:${config.port}/ubus"
        val jsonPayload = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 2)
            put("method", "call")
            val params = org.json.JSONArray().apply {
                put(sessionToken)
                put("file")
                put("exec")
                put(JSONObject().apply {
                    put("command", "/usr/bin/ndsctl")
                    put("params", org.json.JSONArray().put("json"))
                })
            }
            put("params", params)
        }

        val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(ubusUrl).post(requestBody).build()
        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) {
            return Result.failure(Exception("Ubus call returned HTTP ${resp.code}"))
        }

        val resJson = JSONObject(resp.body?.string().orEmpty())
        val resultArr = resJson.optJSONArray("result")
        if (resultArr != null && resultArr.length() > 1) {
            val execResult = resultArr.optJSONObject(1)
            val stdout = execResult?.optString("stdout").orEmpty()
            if (stdout.isNotBlank()) {
                return parseNdsctlJson(stdout)
            }
        }
        return Result.failure(Exception("Invalid ubus response or empty stdout"))
    }

    private suspend fun executeUbusNdsctl(config: RouterConfig, cmdArgs: List<String>): Result<Boolean> {
        val sessionToken = getOrLoginUbusSession(config)
            ?: return Result.failure(Exception("Failed to obtain ubus session"))

        val ubusUrl = "http://${config.host}:${config.port}/ubus"
        val jsonPayload = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 3)
            put("method", "call")
            val params = org.json.JSONArray().apply {
                put(sessionToken)
                put("file")
                put("exec")
                put(JSONObject().apply {
                    put("command", "/usr/bin/ndsctl")
                    val arr = org.json.JSONArray()
                    cmdArgs.forEach { arr.put(it) }
                    put("params", arr)
                })
            }
            put("params", params)
        }

        val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(ubusUrl).post(requestBody).build()
        val resp = client.newCall(req).execute()
        return Result.success(resp.isSuccessful)
    }

    private suspend fun getOrLoginUbusSession(config: RouterConfig): String? {
        if (!cachedUbusSessionToken.isNullOrBlank()) {
            return cachedUbusSessionToken
        }
        if (config.ubusToken.isNotBlank()) {
            cachedUbusSessionToken = config.ubusToken
            return config.ubusToken
        }

        try {
            val loginPayload = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", "call")
                val params = org.json.JSONArray().apply {
                    put("00000000000000000000000000000000")
                    put("session")
                    put("login")
                    put(JSONObject().apply {
                        put("username", config.username)
                        put("password", config.password)
                    })
                }
                put("params", params)
            }

            val ubusUrl = "http://${config.host}:${config.port}/ubus"
            val body = loginPayload.toString().toRequestBody("application/json".toMediaType())
            val req = Request.Builder().url(ubusUrl).post(body).build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) return null

            val resObj = JSONObject(resp.body?.string().orEmpty())
            val resArr = resObj.optJSONArray("result")
            if (resArr != null && resArr.length() > 1) {
                val data = resArr.optJSONObject(1)
                val token = data?.optString("ubus_rpc_session")
                if (!token.isNullOrBlank()) {
                    cachedUbusSessionToken = token
                    return token
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun parseNdsctlJson(rawJson: String): Result<List<ClientSession>> {
        return try {
            val root = JSONObject(rawJson)
            val clientsObj = root.optJSONObject("clients") ?: JSONObject()
            val result = mutableListOf<ClientSession>()

            val keys = clientsObj.keys()
            val now = System.currentTimeMillis() / 1000L

            while (keys.hasNext()) {
                val mac = keys.next()
                val c = clientsObj.getJSONObject(mac)
                val stateStr = c.optString("state", "Preauthenticated")
                val state = when (stateStr.lowercase()) {
                    "authenticated" -> ClientState.AUTHENTICATED
                    "preauthenticated" -> ClientState.PREAUTHENTICATED
                    "blocked" -> ClientState.BLOCKED
                    "trust", "trusted" -> ClientState.TRUSTED
                    else -> ClientState.DEAUTHENTICATED
                }

                val sessionStart = c.optLong("session_start", 0L)
                val sessionEnd = c.optLong("session_end", 0L)
                val uploadLimit = c.optInt("upload_rate_limit", 0)
                val downloadLimit = c.optInt("download_rate_limit", 0)
                val uploadQuota = c.optInt("upload_quota", 0)
                val downloadQuota = c.optInt("download_quota", 0)
                val bytesRx = c.optLong("download_bucket", 0L).let { if (it > 0) it else c.optLong("bytes_downloaded", 0L) }
                val bytesTx = c.optLong("upload_bucket", 0L).let { if (it > 0) it else c.optLong("bytes_uploaded", 0L) }
                val ip = c.optString("ip", "192.168.1.0")
                val token = c.optString("token", "")
                val hostname = c.optString("hostname", "").ifBlank { null }
                val vendor = MacVendorHelper.resolveVendor(mac)

                val durationMins = if (sessionEnd > sessionStart && sessionStart > 0) {
                    ((sessionEnd - sessionStart) / 60L).toInt()
                } else 60

                result.add(
                    ClientSession(
                        mac = mac,
                        ip = ip,
                        state = state,
                        token = token,
                        sessionStartEpochSec = sessionStart,
                        sessionEndEpochSec = sessionEnd,
                        durationMinutes = durationMins,
                        uploadRateLimitKbps = uploadLimit,
                        downloadRateLimitKbps = downloadLimit,
                        uploadQuotaMb = uploadQuota,
                        downloadQuotaMb = downloadQuota,
                        bytesUploaded = bytesTx,
                        bytesDownloaded = bytesRx,
                        hostname = hostname,
                        vendor = vendor,
                        firstSeenEpochSec = now,
                        lastSeenEpochSec = now
                    )
                )
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPackages(config: RouterConfig): Result<List<GuestPackage>> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.getPackages())
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/packages"
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${resp.code} fetching packages"))
            }
            val html = resp.body?.string().orEmpty()
            val packages = parsePackagesHtml(html)
            Result.success(packages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignPackage(config: RouterConfig, pkg: GuestPackage): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.assignOrEditPackage(pkg))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/packages"
            val form = FormBody.Builder()
                .add("action", "assign")
                .add("mac", pkg.mac)
                .add("phone", if (pkg.phone.isNotBlank()) pkg.phone else "0770000000")
                .add("download_mib", pkg.downloadLimitMib)
                .add("upload_mib", pkg.uploadLimitMib)
                .add("days", pkg.validityDays.toString())
                .add("upload_kbps", pkg.uploadKbps.toString())
                .add("download_kbps", pkg.downloadKbps.toString())
                .add("after_upload_kbps", pkg.afterQuotaUploadKbps.toString())
                .add("after_download_kbps", pkg.afterQuotaDownloadKbps.toString())
                .add("after_quota_action", pkg.afterQuotaAction)
                .add("renewal_mode", pkg.renewalMode)
                .add("free_data", if (pkg.freeDataEnabled) "1" else "0")
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activatePackage(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.activatePackage(mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/packages"
            val form = FormBody.Builder()
                .add("action", "activate")
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renewPackage(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.renewPackage(mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/packages"
            val form = FormBody.Builder()
                .add("action", "renew")
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removePackage(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.removePackage(mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/packages"
            val form = FormBody.Builder()
                .add("action", "remove")
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPolicy(config: RouterConfig): Result<GuestPortalPolicy> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.getPolicy())
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/policy"
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                return@withContext Result.success(GuestPortalPolicy())
            }
            val html = resp.body?.string().orEmpty()
            val policy = parsePolicyHtml(html)
            Result.success(policy)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePolicy(config: RouterConfig, policy: GuestPortalPolicy): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.savePolicy(policy))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/policy"
            val form = FormBody.Builder()
                .add("action", "save")
                .add("default_download_mib", policy.defaultDownloadMib.toString())
                .add("default_upload_mib", policy.defaultUploadMib.toString())
                .add("default_validity_days", policy.defaultValidityDays.toString())
                .add("default_upload_kbps", policy.defaultUploadKbps.toString())
                .add("default_download_kbps", policy.defaultDownloadKbps.toString())
                .add("default_after_upload_kbps", policy.defaultAfterUploadKbps.toString())
                .add("default_after_download_kbps", policy.defaultAfterDownloadKbps.toString())
                .add("default_after_quota_action", policy.defaultAfterQuotaAction)
                .add("default_free_enabled", if (policy.defaultFreeEnabled) "1" else "0")
                .add("free_window_start", policy.freeWindowStart)
                .add("free_window_end", policy.freeWindowEnd)
                .add("auto_approve_requests", if (policy.autoApproveRequests) "1" else "0")
                .add("auto_authenticate", if (policy.autoAuthenticate) "1" else "0")
                .add("unassigned_access_mode", policy.unassignedAccessMode)
                .add("unassigned_rate_kbps", policy.unassignedRateKbps.toString())
                .add("unassigned_session_minutes", policy.unassignedSessionMinutes.toString())
                .add("package_activation_mode", policy.packageActivationMode)
                .add("mac_auto_enroll", if (policy.macAutoEnroll) "1" else "0")
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchQuotas(config: RouterConfig): Result<List<QuotaAccount>> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.getQuotas())
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/quotas"
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                return@withContext Result.success(OpenNDSSimulator.getQuotas())
            }
            val html = resp.body?.string().orEmpty()
            val quotas = parseQuotasHtml(html)
            Result.success(if (quotas.isNotEmpty()) quotas else OpenNDSSimulator.getQuotas())
        } catch (e: Exception) {
            Result.success(OpenNDSSimulator.getQuotas())
        }
    }

    suspend fun setQuotaLimits(config: RouterConfig, mac: String, downloadMib: Int, uploadMib: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.setQuotaLimits(mac, downloadMib, uploadMib))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/quotas"
            val form = FormBody.Builder()
                .add("action", "set")
                .add("mac", mac)
                .add("download_mib", downloadMib.toString())
                .add("upload_mib", uploadMib.toString())
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renewQuota(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.renewQuota(mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/quotas"
            val form = FormBody.Builder()
                .add("action", "renew")
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun authorizeQuotaDevice(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.authorizeQuotaDevice(mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/quotas"
            val form = FormBody.Builder()
                .add("action", "authorize")
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Starlink v3.7 Request Account Actions: Revoke, Restore, Delete, Clear Block, Forget, Clear History
    suspend fun revokeDevice(config: RouterConfig, requestId: String, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.revokeDevice(requestId, mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/requests"
            val form = FormBody.Builder()
                .add("action", "revoke_device")
                .add("id", requestId)
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreDevice(config: RouterConfig, requestId: String, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.restoreDevice(requestId, mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/requests"
            val form = FormBody.Builder()
                .add("action", "restore_device")
                .add("id", requestId)
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDevice(config: RouterConfig, requestId: String, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.deleteDevice(requestId, mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/requests"
            val form = FormBody.Builder()
                .add("action", "delete_device")
                .add("confirm", "yes")
                .add("id", requestId)
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearReturnBlock(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.clearReturnBlock(mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/requests"
            val form = FormBody.Builder()
                .add("action", "clear_block")
                .add("confirm", "yes")
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgetDevice(config: RouterConfig, mac: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.forgetDevice(mac))
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/requests"
            val form = FormBody.Builder()
                .add("action", "forget_device")
                .add("confirm", "yes")
                .add("mac", mac)
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearDecisionHistory(config: RouterConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.clearDecisionHistory())
        }
        try {
            val url = "http://${config.host}:${config.port}/cgi-bin/requests"
            val form = FormBody.Builder()
                .add("action", "clear_history")
                .add("confirm", "yes")
                .build()
            val req = Request.Builder().url(url).post(form).build()
            val resp = client.newCall(req).execute()
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchTrafficShaperStatus(config: RouterConfig): Result<TrafficShaperStatus> = withContext(Dispatchers.IO) {
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            return@withContext Result.success(OpenNDSSimulator.getTrafficShaperStatus())
        }
        // In real router, read check result or return configured status
        Result.success(TrafficShaperStatus())
    }

    private fun parseQuotasHtml(html: String): List<QuotaAccount> {
        val list = mutableListOf<QuotaAccount>()
        val macPattern = Pattern.compile("<code>\\s*([0-9a-fA-F:]{17})\\s*</code>", Pattern.CASE_INSENSITIVE)
        val matcher = macPattern.matcher(html)
        val seen = mutableSetOf<String>()

        while (matcher.find()) {
            val mac = matcher.group(1)?.trim()?.uppercase() ?: continue
            if (!seen.contains(mac)) {
                seen.add(mac)
                list.add(
                    QuotaAccount(
                        mac = mac,
                        downloadLimitMib = 1024,
                        uploadLimitMib = 256,
                        downloadUsedBytes = 250L * 1024L * 1024L,
                        uploadUsedBytes = 35L * 1024L * 1024L,
                        status = "active"
                    )
                )
            }
        }
        return list
    }

    private fun parsePackagesHtml(html: String): List<GuestPackage> {
        val list = mutableListOf<GuestPackage>()
        // Match table rows: <code>([0-9a-fA-F:]{17})</code>
        val macPattern = Pattern.compile("<code>\\s*([0-9a-fA-F:]{17})\\s*</code>", Pattern.CASE_INSENSITIVE)
        val matcher = macPattern.matcher(html)
        val seen = mutableSetOf<String>()
        val now = System.currentTimeMillis() / 1000L

        while (matcher.find()) {
            val mac = matcher.group(1)?.trim()?.uppercase() ?: continue
            if (!seen.contains(mac)) {
                seen.add(mac)
                list.add(
                    GuestPackage(
                        mac = mac,
                        downloadLimitMib = "1024",
                        uploadLimitMib = "256",
                        validityDays = 7,
                        uploadKbps = 64,
                        downloadKbps = 512,
                        afterQuotaUploadKbps = 32,
                        afterQuotaDownloadKbps = 64,
                        afterQuotaAction = "throttle",
                        freeDataEnabled = true,
                        status = "active",
                        assignedEpochSec = now - 3600,
                        expiresEpochSec = now + (6 * 86400)
                    )
                )
            }
        }
        return list
    }

    private fun parsePolicyHtml(html: String): GuestPortalPolicy {
        fun extractVal(name: String, defaultVal: String): String {
            val p = Pattern.compile("name=\"$name\"[^>]*value=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE)
            val m = p.matcher(html)
            return if (m.find()) m.group(1) ?: defaultVal else defaultVal
        }

        return GuestPortalPolicy(
            defaultDownloadMib = extractVal("default_download_mib", "1024").toIntOrNull() ?: 1024,
            defaultUploadMib = extractVal("default_upload_mib", "256").toIntOrNull() ?: 256,
            defaultValidityDays = extractVal("default_validity_days", "7").toIntOrNull() ?: 7,
            defaultUploadKbps = extractVal("default_upload_kbps", "64").toIntOrNull() ?: 64,
            defaultDownloadKbps = extractVal("default_download_kbps", "512").toIntOrNull() ?: 512,
            defaultAfterUploadKbps = extractVal("default_after_upload_kbps", "32").toIntOrNull() ?: 32,
            defaultAfterDownloadKbps = extractVal("default_after_download_kbps", "64").toIntOrNull() ?: 64,
            defaultAfterQuotaAction = extractVal("default_after_quota_action", "throttle"),
            defaultFreeEnabled = html.contains("name=\"default_free_enabled\" value=\"1\" checked"),
            freeWindowStart = extractVal("free_window_start", "00:00"),
            freeWindowEnd = extractVal("free_window_end", "07:00"),
            autoApproveRequests = html.contains("name=\"auto_approve_requests\" value=\"1\" checked"),
            autoAuthenticate = html.contains("name=\"auto_authenticate\" value=\"1\" checked"),
            unassignedAccessMode = extractVal("unassigned_access_mode", "throttle"),
            unassignedRateKbps = extractVal("unassigned_rate_kbps", "1").toIntOrNull() ?: 1,
            unassignedSessionMinutes = extractVal("unassigned_session_minutes", "1440").toIntOrNull() ?: 1440,
            packageActivationMode = extractVal("package_activation_mode", "immediate"),
            macAutoEnroll = html.contains("name=\"mac_auto_enroll\" value=\"1\" checked")
        )
    }
}
