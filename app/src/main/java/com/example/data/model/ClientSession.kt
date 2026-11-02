package com.example.data.model

enum class ClientState {
    PREAUTHENTICATED,
    AUTHENTICATED,
    BLOCKED,
    TRUSTED,
    DEAUTHENTICATED
}

data class ClientSession(
    val mac: String,
    val ip: String,
    val state: ClientState = ClientState.PREAUTHENTICATED,
    val token: String? = null,
    val sessionStartEpochSec: Long = 0L,
    val sessionEndEpochSec: Long = 0L,
    val durationMinutes: Int = 60,
    val uploadRateLimitKbps: Int = 0,
    val downloadRateLimitKbps: Int = 0,
    val uploadQuotaMb: Int = 0,
    val downloadQuotaMb: Int = 0,
    val bytesUploaded: Long = 0L,
    val bytesDownloaded: Long = 0L,
    val hostname: String? = null,
    val vendor: String? = null,
    val firstSeenEpochSec: Long = System.currentTimeMillis() / 1000L,
    val lastSeenEpochSec: Long = System.currentTimeMillis() / 1000L,
    // Starlink Guest Portal v3.7 integration fields:
    val requestId: String? = null,      // e.g. REQ-20261102-1201
    val guestName: String? = null,      // e.g. "Dilshan"
    val mobile: String? = null,         // e.g. "+94 77 123 4567"
    val browserHint: String? = null,    // e.g. "Safari / iOS"
    val assignedPackage: String? = null, // e.g. "Weekly 5GB" or "Unassigned"
    val requestStatus: String = "PENDING" // "PENDING", "APPROVED", "REJECTED", "REVOKED", "DELETED"
) {
    fun remainingSeconds(nowEpochSec: Long = System.currentTimeMillis() / 1000L): Long {
        if (state != ClientState.AUTHENTICATED) return 0L
        if (sessionEndEpochSec <= 0L) return 0L
        return (sessionEndEpochSec - nowEpochSec).coerceAtLeast(0L)
    }

    fun isExpired(nowEpochSec: Long = System.currentTimeMillis() / 1000L): Boolean {
        if (state != ClientState.AUTHENTICATED) return false
        return sessionEndEpochSec in 1..nowEpochSec
    }

    fun totalBytesUsed(): Long = bytesUploaded + bytesDownloaded

    fun usedMb(): Double = (totalBytesUsed() / (1024.0 * 1024.0))

    fun isQuotaExceeded(): Boolean {
        if (downloadQuotaMb > 0 && (bytesDownloaded / (1024L * 1024L)) >= downloadQuotaMb) return true
        if (uploadQuotaMb > 0 && (bytesUploaded / (1024L * 1024L)) >= uploadQuotaMb) return true
        return false
    }

    fun progressFraction(nowEpochSec: Long = System.currentTimeMillis() / 1000L): Float {
        if (state != ClientState.AUTHENTICATED || sessionStartEpochSec >= sessionEndEpochSec) return 1f
        val total = (sessionEndEpochSec - sessionStartEpochSec).coerceAtLeast(1L)
        val elapsed = (nowEpochSec - sessionStartEpochSec).coerceAtLeast(0L)
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

    fun displayName(): String {
        return when {
            !guestName.isNullOrBlank() && guestName != "Unknown" -> guestName
            !hostname.isNullOrBlank() && hostname != "Unknown" -> hostname
            !vendor.isNullOrBlank() && vendor != "Unknown Device" -> "$vendor Device ($ip)"
            else -> "Client ($ip)"
        }
    }
}
