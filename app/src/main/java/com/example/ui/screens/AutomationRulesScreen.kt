package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AutoApprovalRule
import com.example.data.model.GuestPortalPolicy
import com.example.data.model.RuleMatchType
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.RedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AutomationRulesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val routerConfig by viewModel.routerConfig.collectAsStateWithLifecycle()
    val portalPolicy by viewModel.portalPolicy.collectAsStateWithLifecycle()
    val rules by viewModel.autoApprovalRules.collectAsStateWithLifecycle()
    val blockedClients by viewModel.blockedClients.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Starlink Policy, 1: Auto-Approval Rules, 2: Blocked Clients

    // Local state for editable policy form
    var autoApprove by remember(portalPolicy) { mutableStateOf(portalPolicy.autoApproveRequests) }
    var autoAuth by remember(portalPolicy) { mutableStateOf(portalPolicy.autoAuthenticate) }
    var macAutoEnroll by remember(portalPolicy) { mutableStateOf(portalPolicy.macAutoEnroll) }
    var unassignedMode by remember(portalPolicy) { mutableStateOf(portalPolicy.unassignedAccessMode) }
    var unassignedRateText by remember(portalPolicy) { mutableStateOf(portalPolicy.unassignedRateKbps.toString()) }
    var unassignedSessionText by remember(portalPolicy) { mutableStateOf(portalPolicy.unassignedSessionMinutes.toString()) }
    var activationMode by remember(portalPolicy) { mutableStateOf(portalPolicy.packageActivationMode) }

    var freeStart by remember(portalPolicy) { mutableStateOf(portalPolicy.freeWindowStart) }
    var freeEnd by remember(portalPolicy) { mutableStateOf(portalPolicy.freeWindowEnd) }

    var defDownMibText by remember(portalPolicy) { mutableStateOf(portalPolicy.defaultDownloadMib.toString()) }
    var defUpMibText by remember(portalPolicy) { mutableStateOf(portalPolicy.defaultUploadMib.toString()) }
    var defValidityText by remember(portalPolicy) { mutableStateOf(portalPolicy.defaultValidityDays.toString()) }
    var defDownKbpsText by remember(portalPolicy) { mutableStateOf(portalPolicy.defaultDownloadKbps.toString()) }
    var defUpKbpsText by remember(portalPolicy) { mutableStateOf(portalPolicy.defaultUploadKbps.toString()) }
    var defAfterDownText by remember(portalPolicy) { mutableStateOf(portalPolicy.defaultAfterDownloadKbps.toString()) }
    var defAfterUpText by remember(portalPolicy) { mutableStateOf(portalPolicy.defaultAfterUploadKbps.toString()) }
    var defAfterAction by remember(portalPolicy) { mutableStateOf(portalPolicy.defaultAfterQuotaAction) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { viewModel.openRuleEditor(null) },
                    containerColor = CyanPrimary,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("add_rule_fab")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Rule")
                }
            }
        },
        containerColor = BackgroundDark,
        modifier = modifier
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .testTag("automation_rules_screen")
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Policy & Automation Management",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage your Starlink Guest Portal policy.conf, auto-approvals, and blocklist",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Tab Row
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceDark,
                    contentColor = CyanNeon,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Portal Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Session Rules (${rules.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Blocked (${blockedClients.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // TAB 0: Starlink Guest Portal v3.7 Policy
            if (selectedTab == 0) {
                // Section 1: Workflow Controls
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.Policy, contentDescription = null, tint = CyanNeon)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "WORKFLOW & ACCESS POLICIES",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Auto-Approve Requests
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto-Approve Requests", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("AUTO_APPROVE_REQUESTS: On automatically approves new guest requests", color = TextSecondary, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = autoApprove,
                                    onCheckedChange = { autoApprove = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = CyanNeon)
                                )
                            }

                            // Auto-Authenticate
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto-Authenticate Approved Guests", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("AUTO_AUTHENTICATE: On applies active package/holding access immediately", color = TextSecondary, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = autoAuth,
                                    onCheckedChange = { autoAuth = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = CyanNeon)
                                )
                            }

                            // MAC Auto-Enroll
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Automatic MAC Enrollment", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("MAC_AUTO_ENROLL: Creates account for new MAC without asking name/mobile", color = TextSecondary, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = macAutoEnroll,
                                    onCheckedChange = { macAutoEnroll = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = CyanNeon)
                                )
                            }

                            // Package Activation Mode
                            Column {
                                Text("Package Activation Mode", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = activationMode == "immediate",
                                        onClick = { activationMode = "immediate" },
                                        label = { Text("Immediate (Starts at assignment)") }
                                    )
                                    FilterChip(
                                        selected = activationMode == "manual",
                                        onClick = { activationMode = "manual" },
                                        label = { Text("Manual (Waits for Activate)") }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Unassigned Device Access (Holding Policy)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "UNASSIGNED DEVICE ACCESS (HOLDING POLICY)",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = unassignedMode == "throttle",
                                    onClick = { unassignedMode = "throttle" },
                                    label = { Text("Throttle (Temporary Speed)") }
                                )
                                FilterChip(
                                    selected = unassignedMode == "block",
                                    onClick = { unassignedMode = "block" },
                                    label = { Text("Block (Captive Portal Only)") }
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = unassignedRateText,
                                    onValueChange = { unassignedRateText = it },
                                    label = { Text("Holding Rate (kbps)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = unassignedSessionText,
                                    onValueChange = { unassignedSessionText = it },
                                    label = { Text("Holding Session (Minutes)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section 3: Free-Data Window Schedule
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.Schedule, contentDescription = null, tint = CyanNeon)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FREE-DATA WINDOW SCHEDULE",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "Traffic during this schedule is excluded from allowances for packages with Free Data On",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = freeStart,
                                    onValueChange = { freeStart = it },
                                    label = { Text("Start Time (HH:MM)") },
                                    placeholder = { Text("00:00") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = freeEnd,
                                    onValueChange = { freeEnd = it },
                                    label = { Text("End Time (HH:MM)") },
                                    placeholder = { Text("07:00") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section 4: New-Package Defaults
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "NEW-PACKAGE POLICY DEFAULTS",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = defDownMibText,
                                    onValueChange = { defDownMibText = it },
                                    label = { Text("Download (MiB)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = defUpMibText,
                                    onValueChange = { defUpMibText = it },
                                    label = { Text("Upload (MiB)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = defValidityText,
                                    onValueChange = { defValidityText = it },
                                    label = { Text("Validity (Days)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = defDownKbpsText,
                                    onValueChange = { defDownKbpsText = it },
                                    label = { Text("Normal Down (kbps)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = defUpKbpsText,
                                    onValueChange = { defUpKbpsText = it },
                                    label = { Text("Normal Up (kbps)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = defAfterDownText,
                                    onValueChange = { defAfterDownText = it },
                                    label = { Text("After-Down (kbps)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = defAfterUpText,
                                    onValueChange = { defAfterUpText = it },
                                    label = { Text("After-Up (kbps)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = defAfterAction == "throttle",
                                    onClick = { defAfterAction = "throttle" },
                                    label = { Text("After-Quota: Throttle") }
                                )
                                FilterChip(
                                    selected = defAfterAction == "disconnect",
                                    onClick = { defAfterAction = "disconnect" },
                                    label = { Text("After-Quota: Disconnect") }
                                )
                            }
                        }
                    }
                }

                // Save Policy Button
                item {
                    Button(
                        onClick = {
                            viewModel.savePortalPolicy(
                                GuestPortalPolicy(
                                    defaultDownloadMib = defDownMibText.toIntOrNull() ?: 1024,
                                    defaultUploadMib = defUpMibText.toIntOrNull() ?: 256,
                                    defaultValidityDays = defValidityText.toIntOrNull() ?: 7,
                                    defaultDownloadKbps = defDownKbpsText.toIntOrNull() ?: 512,
                                    defaultUploadKbps = defUpKbpsText.toIntOrNull() ?: 64,
                                    defaultAfterDownloadKbps = defAfterDownText.toIntOrNull() ?: 64,
                                    defaultAfterUploadKbps = defAfterUpText.toIntOrNull() ?: 32,
                                    defaultAfterQuotaAction = defAfterAction,
                                    defaultFreeEnabled = portalPolicy.defaultFreeEnabled,
                                    freeWindowStart = freeStart,
                                    freeWindowEnd = freeEnd,
                                    autoApproveRequests = autoApprove,
                                    autoAuthenticate = autoAuth,
                                    unassignedAccessMode = unassignedMode,
                                    unassignedRateKbps = unassignedRateText.toIntOrNull() ?: 1,
                                    unassignedSessionMinutes = unassignedSessionText.toIntOrNull() ?: 1440,
                                    packageActivationMode = activationMode,
                                    macAutoEnroll = macAutoEnroll
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("save_portal_policy_button")
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Policy to Router (/cgi-bin/policy)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // TAB 1: Auto-Approval Rules
            else if (selectedTab == 1) {
                if (rules.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(14.dp))
                                .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No rules configured. Click + to add an auto-approval rule.", color = TextSecondary)
                        }
                    }
                } else {
                    items(rules, key = { it.id }) { rule ->
                        RuleCard(
                            rule = rule,
                            onToggle = { isEnabled -> viewModel.toggleRule(rule.id, isEnabled) },
                            onEdit = { viewModel.openRuleEditor(rule) },
                            onDelete = { viewModel.deleteRule(rule.id) }
                        )
                    }
                }
            }

            // TAB 2: Blocked Clients
            else {
                if (blockedClients.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(14.dp))
                                .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No clients currently blacklisted or blocked.", color = TextSecondary)
                        }
                    }
                } else {
                    items(blockedClients, key = { it.mac }) { blocked ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceCard, RoundedCornerShape(14.dp))
                                .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = blocked.mac, color = RedLight, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Reason: ${blocked.reason}", color = TextSecondary, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.unblockClient(blocked.mac) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Unblock", color = CyanNeon, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: AutoApprovalRule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (rule.isEnabled) CyanNeon.copy(alpha = 0.3f) else OutlineDark,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (rule.isEnabled) EmeraldActive else Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rule.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CyanNeon
                    )
                )
            }

            if (rule.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rule.description,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val label = when (rule.matchType) {
                        RuleMatchType.ALL_CLIENTS -> "All Clients"
                        RuleMatchType.MAC_PREFIX -> "MAC: ${rule.matchPattern}"
                        RuleMatchType.HOSTNAME_CONTAINS -> "Host: ${rule.matchPattern}"
                        RuleMatchType.VENDOR_MATCH -> "Vendor: ${rule.matchPattern}"
                        RuleMatchType.SCHEDULE_WINDOW -> "Hours: ${rule.matchPattern}"
                    }
                    Text(text = label, color = CyanNeon, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Timer, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${rule.grantDurationMinutes}m access", color = EmeraldLight, fontSize = 11.sp)
                    }
                }

                if (rule.downloadRateLimitKbps > 0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "${rule.downloadRateLimitKbps / 1000} Mbps", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Rule", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete Rule", tint = RedLight, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
