package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BlockedClientEntity
import com.example.data.db.NdsDatabase
import com.example.data.model.AuditLogEntry
import com.example.data.model.AutoApprovalRule
import com.example.data.model.ClientSession
import com.example.data.model.GuestPackage
import com.example.data.model.GuestPortalPolicy
import com.example.data.model.RouterConfig
import com.example.data.model.RouterProtocol
import com.example.data.network.OpenNDSSimulator
import com.example.data.repository.RouterRepository
import com.example.service.AutomationMetrics
import com.example.service.SessionAutomationManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NdsDatabase.getDatabase(application)
    val repository = RouterRepository(database)
    val automationManager = SessionAutomationManager(repository, viewModelScope)

    val routerConfig: StateFlow<RouterConfig> = repository.routerConfig.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        RouterConfig()
    )

    val autoApprovalRules: StateFlow<List<AutoApprovalRule>> = repository.autoApprovalRules.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val recentAuditLogs: StateFlow<List<AuditLogEntry>> = repository.recentAuditLogs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val blockedClients: StateFlow<List<BlockedClientEntity>> = repository.blockedClients.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val pendingClients: StateFlow<List<ClientSession>> = automationManager.pendingClients
    val activeClients: StateFlow<List<ClientSession>> = automationManager.activeClients
    val metrics: StateFlow<AutomationMetrics> = automationManager.metrics

    // UI Feedback events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Dialog & Interactive State
    private val _selectedClientForApproval = MutableStateFlow<ClientSession?>(null)
    val selectedClientForApproval: StateFlow<ClientSession?> = _selectedClientForApproval.asStateFlow()

    private val _editingRule = MutableStateFlow<AutoApprovalRule?>(null)
    val editingRule: StateFlow<AutoApprovalRule?> = _editingRule.asStateFlow()

    // Packages & Policy State (Starlink v3.7 Dashboard)
    private val _packages = MutableStateFlow<List<GuestPackage>>(emptyList())
    val packages: StateFlow<List<GuestPackage>> = _packages.asStateFlow()

    private val _portalPolicy = MutableStateFlow(GuestPortalPolicy())
    val portalPolicy: StateFlow<GuestPortalPolicy> = _portalPolicy.asStateFlow()

    private val _editingPackage = MutableStateFlow<GuestPackage?>(null)
    val editingPackage: StateFlow<GuestPackage?> = _editingPackage.asStateFlow()

    // Quotas State (cgi-bin/quotas)
    private val _quotas = MutableStateFlow<List<com.example.data.model.QuotaAccount>>(emptyList())
    val quotas: StateFlow<List<com.example.data.model.QuotaAccount>> = _quotas.asStateFlow()

    private val _editingQuota = MutableStateFlow<com.example.data.model.QuotaAccount?>(null)
    val editingQuota: StateFlow<com.example.data.model.QuotaAccount?> = _editingQuota.asStateFlow()

    // Traffic Shaper Status (package-shaper.sh check)
    private val _shaperStatus = MutableStateFlow(com.example.data.model.TrafficShaperStatus())
    val shaperStatus: StateFlow<com.example.data.model.TrafficShaperStatus> = _shaperStatus.asStateFlow()

    init {
        automationManager.start()
        loadPackagesAndPolicy()
        loadQuotas()
        checkTrafficShaper()
    }

    override fun onCleared() {
        super.onCleared()
        automationManager.stop()
    }

    fun openApprovalDialog(client: ClientSession) {
        _selectedClientForApproval.value = client
    }

    fun closeApprovalDialog() {
        _selectedClientForApproval.value = null
    }

    fun openRuleEditor(rule: AutoApprovalRule?) {
        _editingRule.value = rule
    }

    fun closeRuleEditor() {
        _editingRule.value = null
    }

    fun manualApprove(
        client: ClientSession,
        durationMinutes: Int,
        downRateKbps: Int = 0,
        upRateKbps: Int = 0,
        downQuotaMb: Int = 0
    ) {
        viewModelScope.launch {
            val res = repository.approveClient(
                client = client,
                durationMinutes = durationMinutes,
                downRateKbps = downRateKbps,
                upRateKbps = upRateKbps,
                downQuotaMb = downQuotaMb,
                isAuto = false
            )
            if (res.isSuccess) {
                _userMessage.emit("Authorized ${client.displayName()} for ${durationMinutes}m")
                closeApprovalDialog()
                automationManager.triggerManualSync()
            } else {
                _userMessage.emit("Failed to authorize client: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun quickApprove(client: ClientSession, durationMinutes: Int) {
        manualApprove(client, durationMinutes)
    }

    fun deauthenticateClient(client: ClientSession) {
        viewModelScope.launch {
            val res = repository.deauthenticateClient(
                client = client,
                reason = "Manual termination by administrator",
                isAuto = false
            )
            if (res.isSuccess) {
                _userMessage.emit("Disconnected ${client.displayName()}")
                automationManager.triggerManualSync()
            } else {
                _userMessage.emit("Deauth failed: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun extendSession(client: ClientSession, extraMinutes: Int) {
        viewModelScope.launch {
            val res = repository.extendSession(client, extraMinutes)
            if (res.isSuccess) {
                _userMessage.emit("Extended session by +${extraMinutes}m for ${client.displayName()}")
                automationManager.triggerManualSync()
            } else {
                _userMessage.emit("Failed to extend session")
            }
        }
    }

    fun blockClient(client: ClientSession, reason: String = "Manual Block") {
        viewModelScope.launch {
            repository.blockClient(client, reason)
            _userMessage.emit("Blocked MAC ${client.mac}")
            automationManager.triggerManualSync()
        }
    }

    fun unblockClient(mac: String) {
        viewModelScope.launch {
            repository.unblockClient(mac)
            _userMessage.emit("Unblocked MAC $mac")
            automationManager.triggerManualSync()
        }
    }

    fun toggleAutomation(active: Boolean) {
        viewModelScope.launch {
            val current = routerConfig.value
            repository.saveRouterConfig(current.copy(isAutomationActive = active))
            _userMessage.emit(if (active) "Automation Engine Activated" else "Automation Engine Paused")
            automationManager.triggerManualSync()
        }
    }

    fun updateConfig(config: RouterConfig) {
        viewModelScope.launch {
            repository.saveRouterConfig(config)
            _userMessage.emit("Router configuration updated")
            automationManager.triggerManualSync()
        }
    }

    fun saveRule(rule: AutoApprovalRule) {
        viewModelScope.launch {
            if (rule.id == 0L) {
                repository.addRule(rule)
                _userMessage.emit("Added rule '${rule.name}'")
            } else {
                repository.updateRule(rule)
                _userMessage.emit("Updated rule '${rule.name}'")
            }
            closeRuleEditor()
        }
    }

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            repository.deleteRule(ruleId)
            _userMessage.emit("Rule deleted")
        }
    }

    fun toggleRule(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleRule(ruleId, isEnabled)
            _userMessage.emit(if (isEnabled) "Rule enabled" else "Rule disabled")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAuditLogs()
            _userMessage.emit("Audit logs cleared")
        }
    }

    fun refreshNow() {
        viewModelScope.launch {
            automationManager.triggerManualSync()
            _userMessage.emit("Synced status with openNDS gateway")
        }
    }

    // Interactive simulator helpers
    fun spawnSimulatedGuest() {
        val client = OpenNDSSimulator.spawnSimulatedGuestClient()
        viewModelScope.launch {
            _userMessage.emit("Simulated client joined captive portal: ${client.displayName()}")
            automationManager.triggerManualSync()
        }
    }

    fun fastForwardSimulation(minutes: Int) {
        OpenNDSSimulator.fastForwardTime(minutes)
        viewModelScope.launch {
            _userMessage.emit("Advanced virtual time by +$minutes minutes (checking expirations...)")
            automationManager.triggerManualSync()
        }
    }

    fun resetSimulation() {
        OpenNDSSimulator.reset()
        viewModelScope.launch {
            _userMessage.emit("Reset simulator state to default clients")
            automationManager.triggerManualSync()
            loadPackagesAndPolicy()
        }
    }

    // Starlink v3.7 Packages and Policy Management
    fun openPackageEditor(pkg: GuestPackage?) {
        _editingPackage.value = pkg
    }

    fun closePackageEditor() {
        _editingPackage.value = null
    }

    fun loadPackagesAndPolicy() {
        viewModelScope.launch {
            val pkgResult = repository.fetchPackages()
            if (pkgResult.isSuccess) {
                _packages.value = pkgResult.getOrDefault(emptyList())
            }
            val polResult = repository.fetchPolicy()
            if (polResult.isSuccess) {
                _portalPolicy.value = polResult.getOrDefault(GuestPortalPolicy())
            }
        }
    }

    fun assignPackage(pkg: GuestPackage) {
        viewModelScope.launch {
            val res = repository.assignPackage(pkg)
            if (res.isSuccess) {
                _userMessage.emit("Package successfully assigned to ${pkg.mac}")
                closePackageEditor()
                loadPackagesAndPolicy()
            } else {
                _userMessage.emit("Failed to assign package: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun activatePackage(mac: String) {
        viewModelScope.launch {
            val res = repository.activatePackage(mac)
            if (res.isSuccess) {
                _userMessage.emit("Package activated for $mac")
                loadPackagesAndPolicy()
                automationManager.triggerManualSync()
            } else {
                _userMessage.emit("Failed to activate package")
            }
        }
    }

    fun renewPackage(mac: String) {
        viewModelScope.launch {
            val res = repository.renewPackage(mac)
            if (res.isSuccess) {
                _userMessage.emit("Package renewed for $mac (allowance reset)")
                loadPackagesAndPolicy()
            } else {
                _userMessage.emit("Failed to renew package")
            }
        }
    }

    fun removePackage(mac: String) {
        viewModelScope.launch {
            val res = repository.removePackage(mac)
            if (res.isSuccess) {
                _userMessage.emit("Package removed for $mac")
                loadPackagesAndPolicy()
            } else {
                _userMessage.emit("Failed to remove package")
            }
        }
    }

    fun savePortalPolicy(newPolicy: GuestPortalPolicy) {
        viewModelScope.launch {
            val res = repository.savePolicy(newPolicy)
            if (res.isSuccess) {
                _portalPolicy.value = newPolicy
                _userMessage.emit("Portal policy settings saved successfully")
            } else {
                _userMessage.emit("Failed to save policy: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    // Quotas Management (cgi-bin/quotas)
    fun loadQuotas() {
        viewModelScope.launch {
            val res = repository.fetchQuotas()
            if (res.isSuccess) {
                _quotas.value = res.getOrDefault(emptyList())
            }
        }
    }

    fun openQuotaEditor(account: com.example.data.model.QuotaAccount?) {
        _editingQuota.value = account
    }

    fun closeQuotaEditor() {
        _editingQuota.value = null
    }

    fun saveQuotaLimits(mac: String, downloadMib: Int, uploadMib: Int) {
        viewModelScope.launch {
            val res = repository.setQuotaLimits(mac, downloadMib, uploadMib)
            if (res.isSuccess) {
                _userMessage.emit("Quota limits updated for $mac (Usage preserved)")
                closeQuotaEditor()
                loadQuotas()
            } else {
                _userMessage.emit("Failed to update quota limits")
            }
        }
    }

    fun renewQuota(mac: String) {
        viewModelScope.launch {
            val res = repository.renewQuota(mac)
            if (res.isSuccess) {
                _userMessage.emit("Quota renewed for $mac (Used allowance reset to 0)")
                loadQuotas()
                loadPackagesAndPolicy()
            } else {
                _userMessage.emit("Failed to renew quota")
            }
        }
    }

    fun authorizeQuotaDevice(mac: String) {
        viewModelScope.launch {
            val res = repository.authorizeQuotaDevice(mac)
            if (res.isSuccess) {
                _userMessage.emit("Device $mac authorized through session policy")
                loadQuotas()
                automationManager.triggerManualSync()
            } else {
                _userMessage.emit("Failed to authorize device")
            }
        }
    }

    // Starlink v3.7 Device Actions
    fun revokeDevice(requestId: String, mac: String) {
        viewModelScope.launch {
            val res = repository.revokeDevice(requestId, mac)
            if (res.isSuccess) {
                _userMessage.emit("Access revoked for $mac (History and quota records preserved)")
                automationManager.triggerManualSync()
                loadPackagesAndPolicy()
            } else {
                _userMessage.emit("Failed to revoke access")
            }
        }
    }

    fun restoreDevice(requestId: String, mac: String) {
        viewModelScope.launch {
            val res = repository.restoreDevice(requestId, mac)
            if (res.isSuccess) {
                _userMessage.emit("Approval restored for $mac")
                automationManager.triggerManualSync()
                loadPackagesAndPolicy()
            } else {
                _userMessage.emit("Failed to restore approval")
            }
        }
    }

    fun deleteDevicePermanently(requestId: String, mac: String) {
        viewModelScope.launch {
            val res = repository.deleteDevice(requestId, mac)
            if (res.isSuccess) {
                _userMessage.emit("Permanently deleted portal records for $mac")
                automationManager.triggerManualSync()
                loadPackagesAndPolicy()
                loadQuotas()
            } else {
                _userMessage.emit("Failed to delete device records")
            }
        }
    }

    fun clearReturnBlock(mac: String) {
        viewModelScope.launch {
            val res = repository.clearReturnBlock(mac)
            if (res.isSuccess) {
                _userMessage.emit("Cleared return-auth block for $mac")
                automationManager.triggerManualSync()
            } else {
                _userMessage.emit("Failed to clear return block")
            }
        }
    }

    fun forgetDevice(mac: String) {
        viewModelScope.launch {
            val res = repository.forgetDevice(mac)
            if (res.isSuccess) {
                _userMessage.emit("Forgot device $mac")
                automationManager.triggerManualSync()
                loadPackagesAndPolicy()
            } else {
                _userMessage.emit("Failed to forget device")
            }
        }
    }

    fun clearDecisionHistory() {
        viewModelScope.launch {
            val res = repository.clearDecisionHistory()
            if (res.isSuccess) {
                _userMessage.emit("Decision history cleared")
                automationManager.triggerManualSync()
            } else {
                _userMessage.emit("Failed to clear history")
            }
        }
    }

    fun checkTrafficShaper() {
        viewModelScope.launch {
            val res = repository.fetchTrafficShaperStatus()
            if (res.isSuccess) {
                _shaperStatus.value = res.getOrDefault(com.example.data.model.TrafficShaperStatus())
            }
        }
    }
}
