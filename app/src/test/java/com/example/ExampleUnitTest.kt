package com.example

import com.example.data.model.ClientSession
import com.example.data.model.ClientState
import com.example.data.util.MacVendorHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun clientSession_expirationCalculation() {
        val now = 1000L
        val activeClient = ClientSession(
            mac = "00:11:22:33:44:55",
            ip = "192.168.1.50",
            state = ClientState.AUTHENTICATED,
            sessionStartEpochSec = now - 1800,
            sessionEndEpochSec = now + 1800
        )
        assertFalse(activeClient.isExpired(now))
        assertEquals(1800L, activeClient.remainingSeconds(now))

        val expiredClient = ClientSession(
            mac = "00:11:22:33:44:66",
            ip = "192.168.1.51",
            state = ClientState.AUTHENTICATED,
            sessionStartEpochSec = now - 3700,
            sessionEndEpochSec = now - 100
        )
        assertTrue(expiredClient.isExpired(now))
        assertEquals(0L, expiredClient.remainingSeconds(now))
    }

    @Test
    fun clientSession_quotaCalculation() {
        val client = ClientSession(
            mac = "00:11:22:33:44:55",
            ip = "192.168.1.50",
            downloadQuotaMb = 500,
            bytesDownloaded = 501L * 1024L * 1024L
        )
        assertTrue(client.isQuotaExceeded())

        val underQuota = ClientSession(
            mac = "00:11:22:33:44:55",
            ip = "192.168.1.50",
            downloadQuotaMb = 500,
            bytesDownloaded = 200L * 1024L * 1024L
        )
        assertFalse(underQuota.isQuotaExceeded())
    }

    @Test
    fun macVendorHelper_resolvesKnownVendors() {
        assertEquals("Apple", MacVendorHelper.resolveVendor("F0:99:B6:11:22:33"))
        assertEquals("Samsung", MacVendorHelper.resolveVendor("50:01:D9:AA:BB:CC"))
        assertEquals("Unknown Device", MacVendorHelper.resolveVendor("02:00:00:00:00:00"))
    }
}
