package com.example.data.util

object MacVendorHelper {
    private val vendorPrefixes = mapOf(
        "F0:99:B6" to "Apple",
        "A4:83:E7" to "Apple",
        "BC:D0:74" to "Apple",
        "3C:06:30" to "Apple",
        "F4:34:F0" to "Apple",
        "AC:BC:32" to "Apple",
        "DC:2B:61" to "Apple",
        "98:01:A7" to "Apple",
        "50:77:05" to "Apple",
        "7C:04:D0" to "Apple",
        "8C:85:90" to "Apple",
        "E8:8D:28" to "Apple",
        "44:D8:84" to "Apple",

        "B8:27:EB" to "Raspberry Pi",
        "DC:A6:32" to "Raspberry Pi",
        "E4:5F:01" to "Raspberry Pi",
        "28:CD:C1" to "Raspberry Pi",

        "50:01:D9" to "Samsung",
        "30:07:4D" to "Samsung",
        "A8:7C:01" to "Samsung",
        "84:25:DB" to "Samsung",
        "68:27:37" to "Samsung",
        "D0:22:BE" to "Samsung",
        "B4:07:F6" to "Samsung",

        "70:3E:AC" to "Google",
        "54:60:09" to "Google",
        "F4:F5:D8" to "Google",
        "D8:38:FC" to "Google",
        "E4:A4:71" to "Google",

        "00:50:56" to "VMware",
        "08:00:27" to "VirtualBox",
        "00:15:5D" to "Microsoft",

        "34:80:62" to "Xiaomi",
        "64:CC:2E" to "Xiaomi",
        "78:11:DC" to "Xiaomi",

        "5C:51:88" to "Dell",
        "F8:DB:88" to "Dell",
        "8C:EC:4B" to "Intel",
        "A0:C5:89" to "Intel",
        "3C:6A:9D" to "HP",
        "00:E0:4C" to "Realtek"
    )

    fun resolveVendor(mac: String): String {
        val clean = mac.uppercase().replace("-", ":")
        val parts = clean.split(":")
        if (parts.size >= 3) {
            val prefix = "${parts[0]}:${parts[1]}:${parts[2]}"
            vendorPrefixes[prefix]?.let { return it }
        }
        return "Unknown Device"
    }
}
