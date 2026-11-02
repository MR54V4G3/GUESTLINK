package com.example.data.model

enum class RuleMatchType {
    ALL_CLIENTS,          // Matches any client connecting
    MAC_PREFIX,           // Matches OUI prefix, e.g. "AA:BB:CC" or specific MAC
    HOSTNAME_CONTAINS,    // Matches hostname string e.g. "iPhone", "Staff"
    VENDOR_MATCH,         // Matches device manufacturer e.g. "Apple", "Samsung"
    SCHEDULE_WINDOW       // Matches current time range e.g. "08:00-20:00"
}

data class AutoApprovalRule(
    val id: Long = 0L,
    val name: String,
    val isEnabled: Boolean = true,
    val matchType: RuleMatchType = RuleMatchType.ALL_CLIENTS,
    val matchPattern: String = "",
    val grantDurationMinutes: Int = 60,
    val downloadRateLimitKbps: Int = 0, // 0 = unlimited
    val uploadRateLimitKbps: Int = 0,   // 0 = unlimited
    val downloadQuotaMb: Int = 0,       // 0 = unlimited
    val priority: Int = 0,
    val description: String = ""
)
