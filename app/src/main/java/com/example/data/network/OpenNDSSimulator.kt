package com.example.data.network

import com.example.data.model.ClientSession
import com.example.data.model.ClientState
import com.example.data.model.GuestPackage
import com.example.data.model.GuestPortalPolicy
import com.example.data.model.QuotaAccount
import com.example.data.model.TrafficShaperStatus
import com.example.data.util.MacVendorHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

object OpenNDSSimulator {
    private val _simulatedClients = MutableStateFlow<Map<String, ClientSession>>(emptyMap())
    val simulatedClients: StateFlow<Map<String, ClientSession>> = _simulatedClients.asStateFlow()

    private val _simulatedPackages = MutableStateFlow<Map<String, GuestPackage>>(emptyMap())
    val simulatedPackages: StateFlow<Map<String, GuestPackage>> = _simulatedPackages.asStateFlow()

    private val _simulatedQuotas = MutableStateFlow<Map<String, QuotaAccount>>(emptyMap())
    val simulatedQuotas: StateFlow<Map<String, QuotaAccount>> = _simulatedQuotas.asStateFlow()

    private val _simulatedPolicy = MutableStateFlow(GuestPortalPolicy())
    val simulatedPolicy: StateFlow<GuestPortalPolicy> = _simulatedPolicy.asStateFlow()

    private val _decisionHistory = MutableStateFlow<List<ClientSession>>(emptyList())
    val decisionHistory: StateFlow<List<ClientSession>> = _decisionHistory.asStateFlow()

    private var virtualTimeOffsetSeconds = 0L
    private var requestCounter = 100

    init {
        seedInitialClients()
        seedInitialPackages()
        seedInitialQuotas()
    }

    private fun currentEpochSec(): Long {
        return (System.currentTimeMillis() / 1000L) + virtualTimeOffsetSeconds
    }

    private fun generateRequestId(): String {
        requestCounter++
        return "REQ-20261102-$requestCounter"
    }

    private fun seedInitialClients() {
        val now = currentEpochSec()
        val initial = mutableMapOf<String, ClientSession>()

        // 1. Pending guest portal request (Dilshan S.)
        val guestMac = "F0:99:B6:41:88:A2"
        initial[guestMac] = ClientSession(
            mac = guestMac,
            ip = "192.168.1.145",
            state = ClientState.PREAUTHENTICATED,
            token = "tok_guest_${Random.nextInt(1000, 9999)}",
            hostname = "Alex-iPhone14",
            vendor = MacVendorHelper.resolveVendor(guestMac),
            firstSeenEpochSec = now - 45,
            lastSeenEpochSec = now,
            requestId = "REQ-20261102-1201",
            guestName = "Dilshan S.",
            mobile = "+94 77 892 1104",
            browserHint = "Mobile Safari / iOS 17",
            assignedPackage = "Standard Guest",
            requestStatus = "PENDING"
        )

        // 2. Another pending guest request
        val tabMac = "50:01:D9:AA:32:1B"
        initial[tabMac] = ClientSession(
            mac = tabMac,
            ip = "192.168.1.189",
            state = ClientState.PREAUTHENTICATED,
            token = "tok_tab_${Random.nextInt(1000, 9999)}",
            hostname = "Galaxy-Tab-S8",
            vendor = MacVendorHelper.resolveVendor(tabMac),
            firstSeenEpochSec = now - 15,
            lastSeenEpochSec = now,
            requestId = "REQ-20261102-1202",
            guestName = "Kasun Perera",
            mobile = "+94 71 450 9921",
            browserHint = "Chrome Mobile / Android 14",
            assignedPackage = "Unassigned",
            requestStatus = "PENDING"
        )

        // 3. Approved active client (Laptop)
        val laptopMac = "5C:51:88:9C:12:F0"
        val startLap = now - (25 * 60)
        val endLap = now + (35 * 60)
        initial[laptopMac] = ClientSession(
            mac = laptopMac,
            ip = "192.168.1.102",
            state = ClientState.AUTHENTICATED,
            token = "tok_lap_9912",
            sessionStartEpochSec = startLap,
            sessionEndEpochSec = endLap,
            durationMinutes = 60,
            uploadRateLimitKbps = 5000,
            downloadRateLimitKbps = 15000,
            downloadQuotaMb = 2000,
            bytesDownloaded = 420L * 1024L * 1024L,
            bytesUploaded = 55L * 1024L * 1024L,
            hostname = "Work-Dell-XPS",
            vendor = MacVendorHelper.resolveVendor(laptopMac),
            firstSeenEpochSec = startLap,
            lastSeenEpochSec = now,
            requestId = "REQ-20261102-1198",
            guestName = "Sarah Jenkins",
            mobile = "+1 415 555 2671",
            browserHint = "Edge 122 / Windows",
            assignedPackage = "Gold 15Mbps",
            requestStatus = "APPROVED"
        )

        // 4. Approved returning device
        val macApproved = "3C:22:FB:4A:91:EE"
        val startAppr = now - (15 * 60)
        val endAppr = now + (105 * 60)
        initial[macApproved] = ClientSession(
            mac = macApproved,
            ip = "192.168.1.130",
            state = ClientState.AUTHENTICATED,
            token = "tok_ret_4481",
            sessionStartEpochSec = startAppr,
            sessionEndEpochSec = endAppr,
            durationMinutes = 120,
            uploadRateLimitKbps = 64,
            downloadRateLimitKbps = 512,
            downloadQuotaMb = 1024,
            bytesDownloaded = 180L * 1024L * 1024L,
            bytesUploaded = 22L * 1024L * 1024L,
            hostname = "Pixel-8-Pro",
            vendor = MacVendorHelper.resolveVendor(macApproved),
            firstSeenEpochSec = startAppr,
            lastSeenEpochSec = now,
            requestId = "REQ-20261102-1195",
            guestName = "Naveen Fernando",
            mobile = "+94 76 112 3490",
            browserHint = "Chrome / Android 14",
            assignedPackage = "Bronze 512kbps",
            requestStatus = "APPROVED"
        )

        // 5. Blocked / Denied device
        val macBlocked = "AA:BB:CC:DD:EE:01"
        initial[macBlocked] = ClientSession(
            mac = macBlocked,
            ip = "192.168.1.205",
            state = ClientState.BLOCKED,
            hostname = "Suspicious-Device",
            vendor = "Unknown Device",
            firstSeenEpochSec = now - 7200,
            lastSeenEpochSec = now - 3600,
            requestId = "REQ-20261102-1180",
            guestName = "Unauthorized Guest",
            mobile = "Unknown",
            browserHint = "Python-requests / Linux",
            assignedPackage = "Blocked",
            requestStatus = "REJECTED"
        )

        // 6. Revoked access device (Access Revoked, History Kept)
        val macRevoked = "64:A2:F9:88:12:34"
        initial[macRevoked] = ClientSession(
            mac = macRevoked,
            ip = "192.168.1.166",
            state = ClientState.DEAUTHENTICATED,
            hostname = "iPad-Mini-Guest",
            vendor = "Apple",
            firstSeenEpochSec = now - 14400,
            lastSeenEpochSec = now - 1800,
            requestId = "REQ-20261102-1175",
            guestName = "Roshan Wickrama",
            mobile = "+94 77 334 9901",
            browserHint = "Mobile Safari / iPadOS",
            assignedPackage = "Silver 2Mbps",
            requestStatus = "REVOKED"
        )

        _simulatedClients.value = initial
        _decisionHistory.value = initial.values.toList()
    }

    private fun seedInitialPackages() {
        val now = currentEpochSec()
        val packages = mutableMapOf<String, GuestPackage>()

        // 1. Sarah's laptop: Gold tier (Auto-renew, Active)
        packages["5C:51:88:9C:12:F0"] = GuestPackage(
            mac = "5C:51:88:9C:12:F0",
            phone = "+1 415 555 2671",
            downloadLimitMib = "5120",
            uploadLimitMib = "1024",
            downloadUsedBytes = 420L * 1024L * 1024L,
            uploadUsedBytes = 55L * 1024L * 1024L,
            validityDays = 30,
            uploadKbps = 5000,
            downloadKbps = 15000,
            afterQuotaUploadKbps = 128,
            afterQuotaDownloadKbps = 256,
            afterQuotaAction = "throttle",
            freeDataEnabled = true,
            renewalMode = "auto_renew",
            status = "active",
            assignedEpochSec = now - (2 * 86400),
            expiresEpochSec = now + (28 * 86400)
        )

        // 2. Naveen's phone: Bronze tier (One-time, Active)
        packages["3C:22:FB:4A:91:EE"] = GuestPackage(
            mac = "3C:22:FB:4A:91:EE",
            phone = "+94 76 112 3490",
            downloadLimitMib = "1024",
            uploadLimitMib = "256",
            downloadUsedBytes = 180L * 1024L * 1024L,
            uploadUsedBytes = 22L * 1024L * 1024L,
            validityDays = 7,
            uploadKbps = 64,
            downloadKbps = 512,
            afterQuotaUploadKbps = 32,
            afterQuotaDownloadKbps = 64,
            afterQuotaAction = "throttle",
            freeDataEnabled = false,
            renewalMode = "one_time",
            status = "active",
            assignedEpochSec = now - 3600,
            expiresEpochSec = now + (6 * 86400)
        )

        // 3. Pending package for Dilshan
        packages["F0:99:B6:41:88:A2"] = GuestPackage(
            mac = "F0:99:B6:41:88:A2",
            phone = "+94 77 892 1104",
            downloadLimitMib = "2048",
            uploadLimitMib = "512",
            downloadUsedBytes = 0L,
            uploadUsedBytes = 0L,
            validityDays = 14,
            uploadKbps = 256,
            downloadKbps = 2048,
            afterQuotaUploadKbps = 64,
            afterQuotaDownloadKbps = 128,
            afterQuotaAction = "throttle",
            freeDataEnabled = true,
            renewalMode = "one_time",
            status = "pending",
            assignedEpochSec = now,
            expiresEpochSec = now + (14 * 86400)
        )

        // 4. Expired package for an old device
        val expMac = "18:65:90:3F:81:4A"
        packages[expMac] = GuestPackage(
            mac = expMac,
            phone = "+94 72 000 1199",
            downloadLimitMib = "500",
            uploadLimitMib = "100",
            downloadUsedBytes = 500L * 1024L * 1024L,
            uploadUsedBytes = 85L * 1024L * 1024L,
            validityDays = 3,
            uploadKbps = 64,
            downloadKbps = 512,
            afterQuotaUploadKbps = 0,
            afterQuotaDownloadKbps = 0,
            afterQuotaAction = "disconnect",
            freeDataEnabled = false,
            renewalMode = "one_time",
            status = "expired",
            assignedEpochSec = now - (5 * 86400),
            expiresEpochSec = now - (2 * 86400)
        )

        _simulatedPackages.value = packages
    }

    private fun seedInitialQuotas() {
        val quotas = mutableMapOf<String, QuotaAccount>()

        quotas["5C:51:88:9C:12:F0"] = QuotaAccount(
            mac = "5C:51:88:9C:12:F0",
            ip = "192.168.1.102",
            downloadLimitMib = 5120,
            uploadLimitMib = 1024,
            downloadUsedBytes = 420L * 1024L * 1024L,
            uploadUsedBytes = 55L * 1024L * 1024L,
            status = "active",
            hostname = "Work-Dell-XPS"
        )

        quotas["3C:22:FB:4A:91:EE"] = QuotaAccount(
            mac = "3C:22:FB:4A:91:EE",
            ip = "192.168.1.130",
            downloadLimitMib = 1024,
            uploadLimitMib = 256,
            downloadUsedBytes = 180L * 1024L * 1024L,
            uploadUsedBytes = 22L * 1024L * 1024L,
            status = "active",
            hostname = "Pixel-8-Pro"
        )

        quotas["18:65:90:3F:81:4A"] = QuotaAccount(
            mac = "18:65:90:3F:81:4A",
            ip = "192.168.1.199",
            downloadLimitMib = 500,
            uploadLimitMib = 100,
            downloadUsedBytes = 500L * 1024L * 1024L,
            uploadUsedBytes = 85L * 1024L * 1024L,
            status = "exhausted",
            hostname = "Old-Android-Phone"
        )

        _simulatedQuotas.value = quotas
    }

    fun getClients(): List<ClientSession> {
        return _simulatedClients.value.values.toList()
    }

    fun getPackages(): List<GuestPackage> {
        return _simulatedPackages.value.values.toList()
    }

    fun getQuotas(): List<QuotaAccount> {
        return _simulatedQuotas.value.values.toList()
    }

    fun getPolicy(): GuestPortalPolicy {
        return _simulatedPolicy.value
    }

    fun savePolicy(newPolicy: GuestPortalPolicy): Boolean {
        _simulatedPolicy.value = newPolicy
        return true
    }

    // --- Quotas CRUD ---
    fun setQuotaLimits(mac: String, downloadMib: Int, uploadMib: Int): Boolean {
        val current = _simulatedQuotas.value.toMutableMap()
        val existing = current[mac]
        val updated = if (existing != null) {
            existing.copy(
                downloadLimitMib = downloadMib,
                uploadLimitMib = uploadMib,
                status = if (existing.downloadUsedMb() >= downloadMib || existing.uploadUsedMb() >= uploadMib) "exhausted" else "active"
            )
        } else {
            QuotaAccount(
                mac = mac,
                downloadLimitMib = downloadMib,
                uploadLimitMib = uploadMib,
                status = "active"
            )
        }
        current[mac] = updated
        _simulatedQuotas.value = current
        return true
    }

    fun renewQuota(mac: String): Boolean {
        val current = _simulatedQuotas.value.toMutableMap()
        val existing = current[mac] ?: return false
        current[mac] = existing.copy(
            downloadUsedBytes = 0L,
            uploadUsedBytes = 0L,
            status = "active"
        )
        _simulatedQuotas.value = current
        return true
    }

    fun authorizeQuotaDevice(mac: String): Boolean {
        val pkg = _simulatedPackages.value[mac]
        val now = currentEpochSec()
        val currentClients = _simulatedClients.value.toMutableMap()
        val existingClient = currentClients[mac]

        if (pkg != null && pkg.status == "active") {
            // Apply package policy
            val client = existingClient?.copy(
                state = ClientState.AUTHENTICATED,
                sessionStartEpochSec = now,
                sessionEndEpochSec = now + (60 * 60L),
                downloadRateLimitKbps = pkg.downloadKbps,
                uploadRateLimitKbps = pkg.uploadKbps,
                assignedPackage = "Assigned Package"
            ) ?: ClientSession(
                mac = mac,
                ip = "192.168.1.${Random.nextInt(100, 240)}",
                state = ClientState.AUTHENTICATED,
                sessionStartEpochSec = now,
                sessionEndEpochSec = now + (60 * 60L),
                downloadRateLimitKbps = pkg.downloadKbps,
                uploadRateLimitKbps = pkg.uploadKbps,
                assignedPackage = "Assigned Package"
            )
            currentClients[mac] = client
            _simulatedClients.value = currentClients
        } else {
            // Apply hold-force unassigned policy
            val policy = _simulatedPolicy.value
            val durationSec = policy.unassignedSessionMinutes * 60L
            val client = existingClient?.copy(
                state = ClientState.AUTHENTICATED,
                sessionStartEpochSec = now,
                sessionEndEpochSec = now + durationSec,
                downloadRateLimitKbps = policy.unassignedRateKbps,
                uploadRateLimitKbps = policy.unassignedRateKbps,
                assignedPackage = "Unassigned Guest"
            ) ?: ClientSession(
                mac = mac,
                ip = "192.168.1.${Random.nextInt(100, 240)}",
                state = ClientState.AUTHENTICATED,
                sessionStartEpochSec = now,
                sessionEndEpochSec = now + durationSec,
                downloadRateLimitKbps = policy.unassignedRateKbps,
                uploadRateLimitKbps = policy.unassignedRateKbps,
                assignedPackage = "Unassigned Guest"
            )
            currentClients[mac] = client
            _simulatedClients.value = currentClients
        }
        return true
    }

    // --- Package Management ---
    fun assignOrEditPackage(pkg: GuestPackage): Boolean {
        val current = _simulatedPackages.value.toMutableMap()
        val now = currentEpochSec()
        val daysSec = pkg.validityDays * 86400L
        val updated = pkg.copy(
            assignedEpochSec = if (pkg.assignedEpochSec > 0) pkg.assignedEpochSec else now,
            expiresEpochSec = if (pkg.expiresEpochSec > 0) pkg.expiresEpochSec else now + daysSec,
            status = if (_simulatedPolicy.value.packageActivationMode == "immediate") "active" else pkg.status
        )
        current[pkg.mac] = updated
        _simulatedPackages.value = current

        // Also sync to quota accounts
        val downMib = pkg.downloadLimitMib.toIntOrNull() ?: 1024
        val upMib = pkg.uploadLimitMib.toIntOrNull() ?: 256
        setQuotaLimits(pkg.mac, downMib, upMib)
        return true
    }

    fun activatePackage(mac: String): Boolean {
        val current = _simulatedPackages.value.toMutableMap()
        val pkg = current[mac] ?: return false
        val now = currentEpochSec()
        current[mac] = pkg.copy(
            status = "active",
            assignedEpochSec = now,
            expiresEpochSec = now + (pkg.validityDays * 86400L)
        )
        _simulatedPackages.value = current
        return true
    }

    fun renewPackage(mac: String): Boolean {
        val current = _simulatedPackages.value.toMutableMap()
        val pkg = current[mac] ?: return false
        val now = currentEpochSec()
        current[mac] = pkg.copy(
            downloadUsedBytes = 0L,
            uploadUsedBytes = 0L,
            status = "active",
            assignedEpochSec = now,
            expiresEpochSec = now + (pkg.validityDays * 86400L)
        )
        _simulatedPackages.value = current
        renewQuota(mac)
        return true
    }

    fun removePackage(mac: String): Boolean {
        val current = _simulatedPackages.value.toMutableMap()
        if (current.remove(mac) != null) {
            _simulatedPackages.value = current
            return true
        }
        return false
    }

    // --- Requests Management & Device Actions ---
    fun approveRequest(reqId: String, pkgName: String? = null): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val target = current.values.find { it.requestId == reqId } ?: return false
        val now = currentEpochSec()
        val updated = target.copy(
            state = if (_simulatedPolicy.value.autoAuthenticate) ClientState.AUTHENTICATED else ClientState.PREAUTHENTICATED,
            sessionStartEpochSec = now,
            sessionEndEpochSec = now + (60 * 60L),
            requestStatus = "APPROVED",
            assignedPackage = pkgName ?: target.assignedPackage
        )
        current[target.mac] = updated
        _simulatedClients.value = current
        return true
    }

    fun rejectRequest(reqId: String): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val target = current.values.find { it.requestId == reqId } ?: return false
        val updated = target.copy(
            state = ClientState.BLOCKED,
            requestStatus = "REJECTED"
        )
        current[target.mac] = updated
        _simulatedClients.value = current
        return true
    }

    fun revokeDevice(reqId: String, mac: String): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val target = current[mac] ?: current.values.find { it.requestId == reqId }
        if (target != null) {
            current[target.mac] = target.copy(
                state = ClientState.DEAUTHENTICATED,
                requestStatus = "REVOKED",
                sessionEndEpochSec = currentEpochSec()
            )
            _simulatedClients.value = current
            return true
        }
        return false
    }

    fun restoreDevice(reqId: String, mac: String): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val target = current[mac] ?: current.values.find { it.requestId == reqId }
        if (target != null) {
            val now = currentEpochSec()
            current[target.mac] = target.copy(
                state = ClientState.AUTHENTICATED,
                requestStatus = "APPROVED",
                sessionStartEpochSec = now,
                sessionEndEpochSec = now + (60 * 60L)
            )
            _simulatedClients.value = current
            return true
        }
        return false
    }

    fun deleteDevice(reqId: String, mac: String): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val client = current.remove(mac) ?: current.values.find { it.requestId == reqId }?.also { current.remove(it.mac) }
        _simulatedClients.value = current

        // Clear package & quota records
        val packages = _simulatedPackages.value.toMutableMap()
        packages.remove(mac)
        _simulatedPackages.value = packages

        val quotas = _simulatedQuotas.value.toMutableMap()
        quotas.remove(mac)
        _simulatedQuotas.value = quotas

        return client != null
    }

    fun clearReturnBlock(mac: String): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val target = current[mac]
        if (target != null && target.state == ClientState.BLOCKED) {
            current[mac] = target.copy(state = ClientState.PREAUTHENTICATED, requestStatus = "PENDING")
            _simulatedClients.value = current
            return true
        }
        return false
    }

    fun forgetDevice(mac: String): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        current.remove(mac)
        _simulatedClients.value = current
        return true
    }

    fun clearDecisionHistory(): Boolean {
        _decisionHistory.value = emptyList()
        return true
    }

    fun authenticateClient(
        mac: String,
        durationMinutes: Int,
        downRateKbps: Int = 0,
        upRateKbps: Int = 0,
        downQuotaMb: Int = 0,
        upQuotaMb: Int = 0,
        requestId: String? = null
    ): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val existing = current[mac]
        val now = currentEpochSec()
        val durationSec = durationMinutes * 60L
        val updated = if (existing != null) {
            existing.copy(
                state = ClientState.AUTHENTICATED,
                sessionStartEpochSec = now,
                sessionEndEpochSec = now + durationSec,
                durationMinutes = durationMinutes,
                downloadRateLimitKbps = downRateKbps,
                uploadRateLimitKbps = upRateKbps,
                downloadQuotaMb = downQuotaMb,
                uploadQuotaMb = upQuotaMb,
                lastSeenEpochSec = now,
                requestId = requestId ?: existing.requestId,
                requestStatus = "APPROVED"
            )
        } else {
            ClientSession(
                mac = mac,
                ip = "192.168.1.${Random.nextInt(100, 250)}",
                state = ClientState.AUTHENTICATED,
                sessionStartEpochSec = now,
                sessionEndEpochSec = now + durationSec,
                durationMinutes = durationMinutes,
                downloadRateLimitKbps = downRateKbps,
                uploadRateLimitKbps = upRateKbps,
                downloadQuotaMb = downQuotaMb,
                uploadQuotaMb = upQuotaMb,
                vendor = MacVendorHelper.resolveVendor(mac),
                firstSeenEpochSec = now,
                lastSeenEpochSec = now,
                requestId = requestId ?: generateRequestId(),
                requestStatus = "APPROVED"
            )
        }
        current[mac] = updated
        _simulatedClients.value = current
        return true
    }

    fun deauthenticateClient(mac: String): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val existing = current[mac]
        if (existing != null) {
            current[mac] = existing.copy(
                state = ClientState.DEAUTHENTICATED,
                sessionEndEpochSec = currentEpochSec(),
                lastSeenEpochSec = currentEpochSec()
            )
            _simulatedClients.value = current
            return true
        }
        return false
    }

    fun blockClient(mac: String): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val existing = current[mac]
        if (existing != null) {
            current[mac] = existing.copy(
                state = ClientState.BLOCKED,
                requestStatus = "REJECTED",
                lastSeenEpochSec = currentEpochSec()
            )
        } else {
            current[mac] = ClientSession(
                mac = mac,
                ip = "0.0.0.0",
                state = ClientState.BLOCKED,
                requestStatus = "REJECTED",
                vendor = MacVendorHelper.resolveVendor(mac)
            )
        }
        _simulatedClients.value = current
        return true
    }

    fun unblockClient(mac: String): Boolean {
        return clearReturnBlock(mac)
    }

    fun extendSession(mac: String, extraMinutes: Int): Boolean {
        val current = _simulatedClients.value.toMutableMap()
        val existing = current[mac]
        if (existing != null && existing.state == ClientState.AUTHENTICATED) {
            val newEnd = existing.sessionEndEpochSec + (extraMinutes * 60L)
            current[mac] = existing.copy(
                sessionEndEpochSec = newEnd,
                durationMinutes = existing.durationMinutes + extraMinutes,
                lastSeenEpochSec = currentEpochSec()
            )
            _simulatedClients.value = current
            return true
        }
        return false
    }

    fun getTrafficShaperStatus(): TrafficShaperStatus {
        return TrafficShaperStatus()
    }

    fun tick(deltaSeconds: Long = 1) {
        val now = currentEpochSec()
        val current = _simulatedClients.value.toMutableMap()
        var changed = false

        for ((mac, client) in current.entries.toList()) {
            if (client.state == ClientState.AUTHENTICATED) {
                val rxInc = Random.nextLong(20_000, 180_000) * deltaSeconds
                val txInc = Random.nextLong(4_000, 30_000) * deltaSeconds
                val updated = client.copy(
                    bytesDownloaded = client.bytesDownloaded + rxInc,
                    bytesUploaded = client.bytesUploaded + txInc,
                    lastSeenEpochSec = now
                )
                current[mac] = updated
                changed = true
            }
        }

        if (changed) {
            _simulatedClients.value = current
        }
    }

    fun fastForwardTime(minutes: Int) {
        virtualTimeOffsetSeconds += (minutes * 60L)
        tick(5)
    }

    fun spawnSimulatedGuestClient(customHostname: String? = null): ClientSession {
        val now = currentEpochSec()
        val guestProfiles = listOf(
            Triple("Malik Ranasinghe", "+94 77 554 1290", "Safari / iOS 17.4"),
            Triple("Chamari Silva", "+94 71 882 4410", "Chrome 124 / Android"),
            Triple("Nuwan Bandara", "+94 76 990 3182", "Firefox / macOS"),
            Triple("Ananya Sen", "+94 70 119 5562", "Chrome / Windows 11"),
            Triple("Pradeep Kumara", "+94 72 443 8911", "Samsung Browser / Android")
        )
        val profile = guestProfiles[Random.nextInt(guestProfiles.size)]

        val sampleMacs = listOf(
            "A4:83:E7:5C:91:" to "Apple",
            "BC:D0:74:10:9B:" to "Apple",
            "84:25:DB:78:E2:" to "Samsung",
            "F4:F5:D8:33:41:" to "Google",
            "34:80:62:09:A4:" to "Xiaomi"
        )
        val choice = sampleMacs[Random.nextInt(sampleMacs.size)]
        val hex2 = String.format("%02X", Random.nextInt(0, 255))
        val fullMac = choice.first + hex2
        val ipSuffix = Random.nextInt(120, 250)

        val newClient = ClientSession(
            mac = fullMac,
            ip = "192.168.1.$ipSuffix",
            state = ClientState.PREAUTHENTICATED,
            token = "tok_sim_${Random.nextInt(1000, 9999)}",
            hostname = customHostname ?: "${choice.second}-Device",
            vendor = choice.second,
            firstSeenEpochSec = now,
            lastSeenEpochSec = now,
            requestId = generateRequestId(),
            guestName = profile.first,
            mobile = profile.second,
            browserHint = profile.third,
            assignedPackage = "Pending Assignment",
            requestStatus = "PENDING"
        )

        val current = _simulatedClients.value.toMutableMap()
        current[fullMac] = newClient
        _simulatedClients.value = current
        return newClient
    }

    fun reset() {
        virtualTimeOffsetSeconds = 0L
        seedInitialClients()
        seedInitialPackages()
        seedInitialQuotas()
    }
}
