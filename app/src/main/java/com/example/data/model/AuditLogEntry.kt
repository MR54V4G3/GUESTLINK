package com.example.data.model

enum class AuditAction {
    AUTO_APPROVED,
    MANUALLY_APPROVED,
    AUTO_EXPIRED_TIME,
    AUTO_EXPIRED_QUOTA,
    MANUALLY_DEAUTHED,
    SESSION_EXTENDED,
    CLIENT_BLOCKED,
    CLIENT_UNBLOCKED,
    CLIENT_TRUSTED
}

data class AuditLogEntry(
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val mac: String,
    val ip: String,
    val hostname: String? = null,
    val action: AuditAction,
    val durationMinutes: Int = 0,
    val ruleName: String? = null,
    val details: String = ""
)
