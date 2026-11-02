package com.example.data.repository

import com.example.data.db.AuditLogEntity
import com.example.data.db.AutoApprovalRuleEntity
import com.example.data.db.BlockedClientEntity
import com.example.data.db.NdsDatabase
import com.example.data.db.RouterConfigEntity
import com.example.data.model.AuditAction
import com.example.data.model.AuditLogEntry
import com.example.data.model.AutoApprovalRule
import com.example.data.model.ClientSession
import com.example.data.model.ClientState
import com.example.data.model.GuestPackage
import com.example.data.model.GuestPortalPolicy
import com.example.data.model.RouterConfig
import com.example.data.model.RouterProtocol
import com.example.data.model.RuleMatchType
import com.example.data.network.OpenNDSSimulator
import com.example.data.network.OpenWrtApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RouterRepository(
    private val database: NdsDatabase,
    private val apiClient: OpenWrtApiClient = OpenWrtApiClient()
) {
    val routerConfig: Flow<RouterConfig> = database.routerConfigDao().getConfig().map { entity ->
        entity?.toDomain() ?: RouterConfig()
    }

    val autoApprovalRules: Flow<List<AutoApprovalRule>> = database.autoApprovalRuleDao().getAllRules().map { list ->
        list.map { it.toDomain() }
    }

    val recentAuditLogs: Flow<List<AuditLogEntry>> = database.auditLogDao().getRecentLogs().map { list ->
        list.map { it.toDomain() }
    }

    val blockedClients: Flow<List<BlockedClientEntity>> = database.blockedClientDao().getAllBlocked()

    suspend fun getRouterConfigSync(): RouterConfig {
        return database.routerConfigDao().getConfigSync()?.toDomain() ?: RouterConfig()
    }

    suspend fun saveRouterConfig(config: RouterConfig) {
        database.routerConfigDao().saveConfig(RouterConfigEntity.fromDomain(config))
    }

    suspend fun setAutomationActive(isActive: Boolean) {
        database.routerConfigDao().setAutomationActive(isActive)
    }

    suspend fun fetchClients(config: RouterConfig): Result<List<ClientSession>> {
        return apiClient.fetchStatus(config)
    }

    suspend fun fetchPackages(): Result<List<GuestPackage>> {
        val config = getRouterConfigSync()
        return apiClient.fetchPackages(config)
    }

    suspend fun assignPackage(pkg: GuestPackage): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.assignPackage(config, pkg)
    }

    suspend fun activatePackage(mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.activatePackage(config, mac)
    }

    suspend fun renewPackage(mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.renewPackage(config, mac)
    }

    suspend fun removePackage(mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.removePackage(config, mac)
    }

    suspend fun fetchPolicy(): Result<GuestPortalPolicy> {
        val config = getRouterConfigSync()
        return apiClient.fetchPolicy(config)
    }

    suspend fun savePolicy(policy: GuestPortalPolicy): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.savePolicy(config, policy)
    }

    suspend fun fetchQuotas(): Result<List<com.example.data.model.QuotaAccount>> {
        val config = getRouterConfigSync()
        return apiClient.fetchQuotas(config)
    }

    suspend fun setQuotaLimits(mac: String, downloadMib: Int, uploadMib: Int): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.setQuotaLimits(config, mac, downloadMib, uploadMib)
    }

    suspend fun renewQuota(mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.renewQuota(config, mac)
    }

    suspend fun authorizeQuotaDevice(mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.authorizeQuotaDevice(config, mac)
    }

    suspend fun revokeDevice(requestId: String, mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.revokeDevice(config, requestId, mac)
    }

    suspend fun restoreDevice(requestId: String, mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.restoreDevice(config, requestId, mac)
    }

    suspend fun deleteDevice(requestId: String, mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.deleteDevice(config, requestId, mac)
    }

    suspend fun clearReturnBlock(mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.clearReturnBlock(config, mac)
    }

    suspend fun forgetDevice(mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.forgetDevice(config, mac)
    }

    suspend fun clearDecisionHistory(): Result<Boolean> {
        val config = getRouterConfigSync()
        return apiClient.clearDecisionHistory(config)
    }

    suspend fun fetchTrafficShaperStatus(): Result<com.example.data.model.TrafficShaperStatus> {
        val config = getRouterConfigSync()
        return apiClient.fetchTrafficShaperStatus(config)
    }

    suspend fun approveClient(
        client: ClientSession,
        durationMinutes: Int,
        downRateKbps: Int = 0,
        upRateKbps: Int = 0,
        downQuotaMb: Int = 0,
        isAuto: Boolean = false,
        ruleName: String? = null
    ): Result<Boolean> {
        val config = getRouterConfigSync()
        val result = apiClient.authenticateClient(
            config = config,
            mac = client.mac,
            durationMinutes = durationMinutes,
            downRateKbps = downRateKbps,
            upRateKbps = upRateKbps,
            downQuotaMb = downQuotaMb,
            requestId = client.requestId
        )

        if (result.isSuccess) {
            val action = if (isAuto) AuditAction.AUTO_APPROVED else AuditAction.MANUALLY_APPROVED
            val details = if (isAuto) {
                "Auto-approved for ${durationMinutes}m via rule '${ruleName ?: "Default"}' (Limits: ${downRateKbps}k down / ${upRateKbps}k up)"
            } else {
                "Manually authorized for ${durationMinutes}m"
            }

            database.auditLogDao().insertLog(
                AuditLogEntity(
                    timestamp = System.currentTimeMillis(),
                    mac = client.mac,
                    ip = client.ip,
                    hostname = client.hostname ?: client.vendor,
                    action = action.name,
                    durationMinutes = durationMinutes,
                    ruleName = ruleName,
                    details = details
                )
            )
            database.auditLogDao().pruneOldLogs()
        }
        return result
    }

    suspend fun deauthenticateClient(
        client: ClientSession,
        reason: String,
        isAuto: Boolean = false,
        isQuota: Boolean = false
    ): Result<Boolean> {
        val config = getRouterConfigSync()
        val result = apiClient.deauthenticateClient(config, client.mac)

        if (result.isSuccess) {
            val action = when {
                isQuota -> AuditAction.AUTO_EXPIRED_QUOTA
                isAuto -> AuditAction.AUTO_EXPIRED_TIME
                else -> AuditAction.MANUALLY_DEAUTHED
            }

            database.auditLogDao().insertLog(
                AuditLogEntity(
                    timestamp = System.currentTimeMillis(),
                    mac = client.mac,
                    ip = client.ip,
                    hostname = client.hostname ?: client.vendor,
                    action = action.name,
                    durationMinutes = client.durationMinutes,
                    ruleName = null,
                    details = "Session terminated: $reason (Used: ${String.format(Locale.US, "%.1f", client.usedMb())} MB)"
                )
            )
        }
        return result
    }

    suspend fun extendSession(client: ClientSession, extraMinutes: Int): Result<Boolean> {
        val config = getRouterConfigSync()
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            OpenNDSSimulator.extendSession(client.mac, extraMinutes)
        } else {
            // In openNDS re-calling auth adds/resets session
            val newTotalMins = client.durationMinutes + extraMinutes
            apiClient.authenticateClient(
                config = config,
                mac = client.mac,
                durationMinutes = newTotalMins,
                downRateKbps = client.downloadRateLimitKbps,
                upRateKbps = client.uploadRateLimitKbps,
                downQuotaMb = client.downloadQuotaMb
            )
        }

        database.auditLogDao().insertLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                mac = client.mac,
                ip = client.ip,
                hostname = client.hostname ?: client.vendor,
                action = AuditAction.SESSION_EXTENDED.name,
                durationMinutes = extraMinutes,
                ruleName = null,
                details = "Extended session by +${extraMinutes} minutes"
            )
        )
        return Result.success(true)
    }

    suspend fun blockClient(client: ClientSession, reason: String = "Manual Block"): Result<Boolean> {
        val config = getRouterConfigSync()
        database.blockedClientDao().blockClient(
            BlockedClientEntity(mac = client.mac, reason = reason)
        )
        apiClient.deauthenticateClient(config, client.mac)
        apiClient.blockClient(config, client.mac)

        database.auditLogDao().insertLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                mac = client.mac,
                ip = client.ip,
                hostname = client.hostname ?: client.vendor,
                action = AuditAction.CLIENT_BLOCKED.name,
                durationMinutes = 0,
                ruleName = null,
                details = "Blocked client: $reason"
            )
        )
        return Result.success(true)
    }

    suspend fun unblockClient(mac: String): Result<Boolean> {
        val config = getRouterConfigSync()
        database.blockedClientDao().unblockClient(mac)
        apiClient.unblockClient(config, mac)

        database.auditLogDao().insertLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                mac = mac,
                ip = "0.0.0.0",
                hostname = null,
                action = AuditAction.CLIENT_UNBLOCKED.name,
                durationMinutes = 0,
                ruleName = null,
                details = "Unblocked client $mac"
            )
        )
        return Result.success(true)
    }

    // Rule management
    suspend fun addRule(rule: AutoApprovalRule) {
        database.autoApprovalRuleDao().insertRule(AutoApprovalRuleEntity.fromDomain(rule))
    }

    suspend fun updateRule(rule: AutoApprovalRule) {
        database.autoApprovalRuleDao().updateRule(AutoApprovalRuleEntity.fromDomain(rule))
    }

    suspend fun deleteRule(ruleId: Long) {
        database.autoApprovalRuleDao().deleteRuleById(ruleId)
    }

    suspend fun toggleRule(ruleId: Long, isEnabled: Boolean) {
        database.autoApprovalRuleDao().toggleRule(ruleId, isEnabled)
    }

    suspend fun clearAuditLogs() {
        database.auditLogDao().clearLogs()
    }

    /**
     * Checks a pending client against active rules to determine if it should be auto-approved
     */
    suspend fun findMatchingRule(client: ClientSession): AutoApprovalRule? {
        val isBlocked = database.blockedClientDao().isBlocked(client.mac)
        if (isBlocked) return null

        val activeRules = database.autoApprovalRuleDao().getActiveRulesSync().map { it.toDomain() }

        for (rule in activeRules) {
            if (matchesRule(client, rule)) {
                return rule
            }
        }
        return null
    }

    private fun matchesRule(client: ClientSession, rule: AutoApprovalRule): Boolean {
        if (!rule.isEnabled) return false

        return when (rule.matchType) {
            RuleMatchType.ALL_CLIENTS -> true

            RuleMatchType.MAC_PREFIX -> {
                val cleanedPrefix = rule.matchPattern.replace("-", ":").trim()
                if (cleanedPrefix.isBlank()) return false
                val patterns = cleanedPrefix.split(",").map { it.trim().uppercase() }
                patterns.any { client.mac.uppercase().startsWith(it) }
            }

            RuleMatchType.HOSTNAME_CONTAINS -> {
                val patterns = rule.matchPattern.split(",").map { it.trim().lowercase() }
                val host = client.hostname?.lowercase() ?: ""
                patterns.any { pattern -> pattern.isNotBlank() && host.contains(pattern) }
            }

            RuleMatchType.VENDOR_MATCH -> {
                val patterns = rule.matchPattern.split(",").map { it.trim().lowercase() }
                val vendor = client.vendor?.lowercase() ?: ""
                patterns.any { pattern -> pattern.isNotBlank() && vendor.contains(pattern) }
            }

            RuleMatchType.SCHEDULE_WINDOW -> {
                // e.g. "08:00-18:00"
                try {
                    val parts = rule.matchPattern.split("-")
                    if (parts.size == 2) {
                        val sdf = SimpleDateFormat("HH:mm", Locale.US)
                        val nowStr = sdf.format(Date())
                        val now = sdf.parse(nowStr)
                        val start = sdf.parse(parts[0].trim())
                        val end = sdf.parse(parts[1].trim())
                        if (now != null && start != null && end != null) {
                            now in start..end
                        } else false
                    } else false
                } catch (e: Exception) {
                    false
                }
            }
        }
    }
}
