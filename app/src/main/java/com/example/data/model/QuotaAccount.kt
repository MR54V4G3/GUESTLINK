package com.example.data.model

data class QuotaAccount(
    val mac: String,
    val ip: String = "",
    val downloadLimitMib: Int = 1024,
    val uploadLimitMib: Int = 256,
    val downloadUsedBytes: Long = 0L,
    val uploadUsedBytes: Long = 0L,
    val status: String = "active", // "active", "exhausted", "blocked", "unassigned"
    val hostname: String = "",
    val lastSeenEpochSec: Long = System.currentTimeMillis() / 1000L
) {
    fun downloadLimitFormatted(): String = if (downloadLimitMib <= 0) "Unlimited" else "$downloadLimitMib MiB"
    fun uploadLimitFormatted(): String = if (uploadLimitMib <= 0) "Unlimited" else "$uploadLimitMib MiB"

    fun downloadUsedMb(): Double = downloadUsedBytes / (1024.0 * 1024.0)
    fun uploadUsedMb(): Double = uploadUsedBytes / (1024.0 * 1024.0)
    fun totalUsedMb(): Double = (downloadUsedBytes + uploadUsedBytes) / (1024.0 * 1024.0)

    fun downloadUsagePercent(): Float {
        if (downloadLimitMib <= 0) return 0f
        return (downloadUsedMb() / downloadLimitMib).toFloat().coerceIn(0f, 1f)
    }

    fun isExhausted(): Boolean {
        if (downloadLimitMib > 0 && downloadUsedMb() >= downloadLimitMib) return true
        if (uploadLimitMib > 0 && uploadUsedMb() >= uploadLimitMib) return true
        return false
    }
}
