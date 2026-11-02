package com.example.data.model

enum class RouterProtocol {
    STARLINK_PORTAL_V3, // Starlink Guest Portal & Policy Dashboard v3.7 (/cgi-bin/requests)
    BUILTIN_SIMULATOR,  // Full-fidelity interactive openNDS simulator
    OPENWRT_UBUS,       // OpenWrt ubus JSON-RPC endpoint (/ubus)
    OPENNDS_REST_CGI    // Generic openNDS REST or cgi-bin ndsctl endpoint
}

data class RouterConfig(
    val id: Long = 1L,
    val name: String = "Starlink Guest Gateway",
    val host: String = "192.168.1.1",
    val port: Int = 80,
    val protocol: RouterProtocol = RouterProtocol.STARLINK_PORTAL_V3,
    val username: String = "root",
    val password: String = "",
    val ubusToken: String = "",
    val cgiPath: String = "/cgi-bin/requests",
    val pollIntervalSeconds: Int = 4,
    val isAutomationActive: Boolean = true,
    val defaultSessionDurationMinutes: Int = 60,
    val autoDeauthOnExpire: Boolean = true,
    val autoDeauthOnQuota: Boolean = true,
    val autoApproveNewClients: Boolean = true,
    val gatewayName: String = "openNDS Starlink Portal",
    val lastConnectedTime: Long = 0L,
    val isConnected: Boolean = true
)
