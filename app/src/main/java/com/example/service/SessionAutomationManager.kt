package com.example.service

import com.example.data.model.ClientSession
import com.example.data.model.ClientState
import com.example.data.model.RouterConfig
import com.example.data.model.RouterProtocol
import com.example.data.network.OpenNDSSimulator
import com.example.data.repository.RouterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AutomationMetrics(
    val pendingCount: Int = 0,
    val activeCount: Int = 0,
    val autoApprovedToday: Int = 0,
    val autoExpiredToday: Int = 0,
    val totalDataRxMb: Double = 0.0,
    val totalDataTxMb: Double = 0.0,
    val lastSyncEpochMs: Long = 0L,
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

class SessionAutomationManager(
    private val repository: RouterRepository,
    private val scope: CoroutineScope
) {
    private var automationJob: Job? = null

    private val _pendingClients = MutableStateFlow<List<ClientSession>>(emptyMap<String, ClientSession>().values.toList())
    val pendingClients: StateFlow<List<ClientSession>> = _pendingClients.asStateFlow()

    private val _activeClients = MutableStateFlow<List<ClientSession>>(emptyList())
    val activeClients: StateFlow<List<ClientSession>> = _activeClients.asStateFlow()

    private val _metrics = MutableStateFlow(AutomationMetrics())
    val metrics: StateFlow<AutomationMetrics> = _metrics.asStateFlow()

    private var autoApprovedCount = 0
    private var autoExpiredCount = 0

    fun start() {
        if (automationJob?.isActive == true) return

        automationJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    runAutomationCycle()
                } catch (e: Exception) {
                    _metrics.value = _metrics.value.copy(
                        isSyncing = false,
                        syncError = e.message ?: "Sync error"
                    )
                }

                val config = repository.getRouterConfigSync()
                val intervalMs = (config.pollIntervalSeconds.coerceAtLeast(2) * 1000L)
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        automationJob?.cancel()
        automationJob = null
    }

    suspend fun triggerManualSync() {
        runAutomationCycle()
    }

    private suspend fun runAutomationCycle() {
        _metrics.value = _metrics.value.copy(isSyncing = true, syncError = null)
        val config = repository.getRouterConfigSync()

        // If in simulator mode, advance simulation tick
        if (config.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            OpenNDSSimulator.tick(config.pollIntervalSeconds.toLong())
        }

        val fetchResult = repository.fetchClients(config)
        if (fetchResult.isFailure) {
            _metrics.value = _metrics.value.copy(
                isSyncing = false,
                syncError = fetchResult.exceptionOrNull()?.message ?: "Failed to reach openNDS gateway"
            )
            return
        }

        val allClients = fetchResult.getOrDefault(emptyList())
        val nowSec = System.currentTimeMillis() / 1000L

        val pending = mutableListOf<ClientSession>()
        val active = mutableListOf<ClientSession>()

        var totalRxBytes = 0L
        var totalTxBytes = 0L

        for (client in allClients) {
            when (client.state) {
                ClientState.PREAUTHENTICATED -> {
                    // Check if automation engine should auto-approve
                    if (config.isAutomationActive && config.autoApproveNewClients) {
                        val matchingRule = repository.findMatchingRule(client)
                        if (matchingRule != null) {
                            // Automatically authorize!
                            val approved = repository.approveClient(
                                client = client,
                                durationMinutes = matchingRule.grantDurationMinutes,
                                downRateKbps = matchingRule.downloadRateLimitKbps,
                                upRateKbps = matchingRule.uploadRateLimitKbps,
                                downQuotaMb = matchingRule.downloadQuotaMb,
                                isAuto = true,
                                ruleName = matchingRule.name
                            )
                            if (approved.getOrDefault(false)) {
                                autoApprovedCount++
                                continue
                            }
                        }
                    }
                    pending.add(client)
                }

                ClientState.AUTHENTICATED -> {
                    totalRxBytes += client.bytesDownloaded
                    totalTxBytes += client.bytesUploaded

                    // Check for automated expiration
                    if (config.isAutomationActive) {
                        // 1. Time-based expiration
                        if (config.autoDeauthOnExpire && client.isExpired(nowSec)) {
                            repository.deauthenticateClient(
                                client = client,
                                reason = "Session duration expired (${client.durationMinutes}m completed)",
                                isAuto = true,
                                isQuota = false
                            )
                            autoExpiredCount++
                            continue
                        }

                        // 2. Quota-based expiration
                        if (config.autoDeauthOnQuota && client.isQuotaExceeded()) {
                            repository.deauthenticateClient(
                                client = client,
                                reason = "Bandwidth quota exceeded",
                                isAuto = true,
                                isQuota = true
                            )
                            autoExpiredCount++
                            continue
                        }
                    }

                    active.add(client)
                }

                else -> {
                    // Blocked or deauthenticated
                }
            }
        }

        // Sort active by remaining time (most urgent first)
        active.sortBy { it.remainingSeconds(nowSec) }
        // Sort pending by first seen
        pending.sortByDescending { it.firstSeenEpochSec }

        _pendingClients.value = pending
        _activeClients.value = active

        _metrics.value = AutomationMetrics(
            pendingCount = pending.size,
            activeCount = active.size,
            autoApprovedToday = autoApprovedCount,
            autoExpiredToday = autoExpiredCount,
            totalDataRxMb = totalRxBytes / (1024.0 * 1024.0),
            totalDataTxMb = totalTxBytes / (1024.0 * 1024.0),
            lastSyncEpochMs = System.currentTimeMillis(),
            isSyncing = false,
            syncError = null
        )
    }
}
