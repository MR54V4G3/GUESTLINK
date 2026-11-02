package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AuditAction
import com.example.data.model.AuditLogEntry
import com.example.data.model.AutoApprovalRule
import com.example.data.model.RouterConfig
import com.example.data.model.RouterProtocol
import com.example.data.model.RuleMatchType

@Entity(tableName = "auto_approval_rules")
data class AutoApprovalRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val isEnabled: Boolean = true,
    val matchType: String = RuleMatchType.ALL_CLIENTS.name,
    val matchPattern: String = "",
    val grantDurationMinutes: Int = 60,
    val downloadRateLimitKbps: Int = 0,
    val uploadRateLimitKbps: Int = 0,
    val downloadQuotaMb: Int = 0,
    val priority: Int = 0,
    val description: String = ""
) {
    fun toDomain(): AutoApprovalRule = AutoApprovalRule(
        id = id,
        name = name,
        isEnabled = isEnabled,
        matchType = try { RuleMatchType.valueOf(matchType) } catch (e: Exception) { RuleMatchType.ALL_CLIENTS },
        matchPattern = matchPattern,
        grantDurationMinutes = grantDurationMinutes,
        downloadRateLimitKbps = downloadRateLimitKbps,
        uploadRateLimitKbps = uploadRateLimitKbps,
        downloadQuotaMb = downloadQuotaMb,
        priority = priority,
        description = description
    )

    companion object {
        fun fromDomain(rule: AutoApprovalRule): AutoApprovalRuleEntity = AutoApprovalRuleEntity(
            id = rule.id,
            name = rule.name,
            isEnabled = rule.isEnabled,
            matchType = rule.matchType.name,
            matchPattern = rule.matchPattern,
            grantDurationMinutes = rule.grantDurationMinutes,
            downloadRateLimitKbps = rule.downloadRateLimitKbps,
            uploadRateLimitKbps = rule.uploadRateLimitKbps,
            downloadQuotaMb = rule.downloadQuotaMb,
            priority = rule.priority,
            description = rule.description
        )
    }
}

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long,
    val mac: String,
    val ip: String,
    val hostname: String?,
    val action: String,
    val durationMinutes: Int = 0,
    val ruleName: String?,
    val details: String
) {
    fun toDomain(): AuditLogEntry = AuditLogEntry(
        id = id,
        timestamp = timestamp,
        mac = mac,
        ip = ip,
        hostname = hostname,
        action = try { AuditAction.valueOf(action) } catch (e: Exception) { AuditAction.AUTO_APPROVED },
        durationMinutes = durationMinutes,
        ruleName = ruleName,
        details = details
    )

    companion object {
        fun fromDomain(entry: AuditLogEntry): AuditLogEntity = AuditLogEntity(
            id = entry.id,
            timestamp = entry.timestamp,
            mac = entry.mac,
            ip = entry.ip,
            hostname = entry.hostname,
            action = entry.action.name,
            durationMinutes = entry.durationMinutes,
            ruleName = entry.ruleName,
            details = entry.details
        )
    }
}

@Entity(tableName = "router_config")
data class RouterConfigEntity(
    @PrimaryKey val id: Long = 1L,
    val name: String = "Primary OpenWrt Gateway",
    val host: String = "192.168.1.1",
    val port: Int = 80,
    val protocol: String = RouterProtocol.BUILTIN_SIMULATOR.name,
    val username: String = "root",
    val password: String = "",
    val ubusToken: String = "",
    val cgiPath: String = "/cgi-bin/ndsctl",
    val pollIntervalSeconds: Int = 5,
    val isAutomationActive: Boolean = true,
    val defaultSessionDurationMinutes: Int = 60,
    val autoDeauthOnExpire: Boolean = true,
    val autoDeauthOnQuota: Boolean = true,
    val autoApproveNewClients: Boolean = true,
    val gatewayName: String = "openNDS"
) {
    fun toDomain(): RouterConfig = RouterConfig(
        id = id,
        name = name,
        host = host,
        port = port,
        protocol = try { RouterProtocol.valueOf(protocol) } catch (e: Exception) { RouterProtocol.BUILTIN_SIMULATOR },
        username = username,
        password = password,
        ubusToken = ubusToken,
        cgiPath = cgiPath,
        pollIntervalSeconds = pollIntervalSeconds,
        isAutomationActive = isAutomationActive,
        defaultSessionDurationMinutes = defaultSessionDurationMinutes,
        autoDeauthOnExpire = autoDeauthOnExpire,
        autoDeauthOnQuota = autoDeauthOnQuota,
        autoApproveNewClients = autoApproveNewClients,
        gatewayName = gatewayName
    )

    companion object {
        fun fromDomain(config: RouterConfig): RouterConfigEntity = RouterConfigEntity(
            id = config.id,
            name = config.name,
            host = config.host,
            port = config.port,
            protocol = config.protocol.name,
            username = config.username,
            password = config.password,
            ubusToken = config.ubusToken,
            cgiPath = config.cgiPath,
            pollIntervalSeconds = config.pollIntervalSeconds,
            isAutomationActive = config.isAutomationActive,
            defaultSessionDurationMinutes = config.defaultSessionDurationMinutes,
            autoDeauthOnExpire = config.autoDeauthOnExpire,
            autoDeauthOnQuota = config.autoDeauthOnQuota,
            autoApproveNewClients = config.autoApproveNewClients,
            gatewayName = config.gatewayName
        )
    }
}

@Entity(tableName = "blocked_clients")
data class BlockedClientEntity(
    @PrimaryKey val mac: String,
    val reason: String = "Manual Block",
    val blockedAt: Long = System.currentTimeMillis()
)
