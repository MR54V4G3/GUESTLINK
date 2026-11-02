package com.example.data.model

data class GuestPackage(
    val mac: String,
    val phone: String = "",
    val downloadLimitMib: String = "1024", // or "unlimited"
    val uploadLimitMib: String = "256",    // or "unlimited"
    val downloadUsedBytes: Long = 0L,
    val uploadUsedBytes: Long = 0L,
    val validityDays: Int = 7,
    val uploadKbps: Int = 64,
    val downloadKbps: Int = 512,
    val afterQuotaUploadKbps: Int = 32,
    val afterQuotaDownloadKbps: Int = 64,
    val afterQuotaAction: String = "throttle", // "throttle" or "disconnect"
    val freeDataEnabled: Boolean = false,
    val renewalMode: String = "one_time",      // "one_time" or "auto_renew"
    val status: String = "active",             // "active", "pending", "expired"
    val assignedEpochSec: Long = 0L,
    val expiresEpochSec: Long = 0L
) {
    fun downloadLimitFormatted(): String = if (downloadLimitMib.equals("unlimited", ignoreCase = true)) "Unlimited" else "$downloadLimitMib MiB"
    fun uploadLimitFormatted(): String = if (uploadLimitMib.equals("unlimited", ignoreCase = true)) "Unlimited" else "$uploadLimitMib MiB"
}

data class GuestPortalPolicy(
    val defaultDownloadMib: Int = 1024,
    val defaultUploadMib: Int = 256,
    val defaultValidityDays: Int = 7,
    val defaultUploadKbps: Int = 64,
    val defaultDownloadKbps: Int = 512,
    val defaultAfterUploadKbps: Int = 32,
    val defaultAfterDownloadKbps: Int = 64,
    val defaultAfterQuotaAction: String = "throttle", // "throttle" or "disconnect"
    val defaultFreeEnabled: Boolean = false,
    val freeWindowStart: String = "00:00",
    val freeWindowEnd: String = "07:00",
    val defaultRenewalMode: String = "one_time", // "one_time" or "auto_renew"
    val expiredAccountAction: String = "block_until_renewed",
    val autoApproveRequests: Boolean = true,
    val autoAuthenticate: Boolean = true,
    val unassignedAccessMode: String = "throttle", // "throttle" or "block"
    val unassignedRateKbps: Int = 1,
    val unassignedSessionMinutes: Int = 1440,
    val packageActivationMode: String = "immediate", // "immediate" or "manual"
    val macAutoEnroll: Boolean = false
) {
    fun isFreeWindowActive(hourOfDay: Int, minute: Int): Boolean {
        if (!defaultFreeEnabled) return false
        val startParts = freeWindowStart.split(":").mapNotNull { it.toIntOrNull() }
        val endParts = freeWindowEnd.split(":").mapNotNull { it.toIntOrNull() }
        if (startParts.size < 2 || endParts.size < 2) return false
        val startMin = startParts[0] * 60 + startParts[1]
        val endMin = endParts[0] * 60 + endParts[1]
        val currentMin = hourOfDay * 60 + minute
        return if (startMin < endMin) {
            currentMin in startMin until endMin
        } else if (startMin > endMin) {
            // Crosses midnight, e.g. 22:00 to 06:00
            currentMin >= startMin || currentMin < endMin
        } else {
            false
        }
    }
}

