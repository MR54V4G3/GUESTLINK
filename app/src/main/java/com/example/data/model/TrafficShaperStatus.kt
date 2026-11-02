package com.example.data.model

data class TrafficShaperStatus(
    val interfaceName: String = "br-guest",
    val ifbRedirect: String = "ifb4br-guest",
    val htbParentClass: String = "1:1",
    val filterPreference: Int = 0,
    val isHtbConfigured: Boolean = true,
    val isIngressRedirectActive: Boolean = true,
    val statusText: String = "All HTB parent classes and IFB ingress redirects verified for STARLINK guest portal v3.7",
    val activeShaperClasses: List<String> = listOf(
        "class htb 1:1 root rate 100Mbit ceil 100Mbit (SQM aggregate)",
        "class htb 1:10 parent 1:1 rate 512Kbit ceil 512Kbit (Normal speed)",
        "class htb 1:20 parent 1:1 rate 64Kbit ceil 64Kbit (After-quota throttle)"
    )
)
