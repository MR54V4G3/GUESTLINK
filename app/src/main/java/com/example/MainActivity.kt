package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.ApprovalDialog
import com.example.ui.components.PackageEditDialog
import com.example.ui.components.QuotaEditDialog
import com.example.ui.components.RuleEditDialog
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ActiveSessionsScreen
import com.example.ui.screens.AutomationRulesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PackagesScreen
import com.example.ui.screens.QuotasScreen
import com.example.ui.screens.RouterSettingsScreen
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.flow.collectLatest

enum class Screen {
    REQUESTS,       // 01 Requests (Dashboard, Pending, Approved, Denied, History)
    PACKAGES,       // 02 Packages (Active, Pending, Expired, SQM Traffic Shaper)
    QUOTAS,         // 03 Quotas (Device accounts, MiB Limits, Reset, Authorize)
    POLICY,         // 04 Policy (Portal Policy v3.7, New Defaults, Free Schedule, Session Rules)
    GATEWAY,        // Router connection, Audit Logs, Simulator, Live Sessions
    ABOUT           // In-app About Page: Developer details, GitHub link, System Versions
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.REQUESTS) }
    val snackbarHostState = remember { SnackbarHostState() }

    val pendingClients by viewModel.pendingClients.collectAsStateWithLifecycle()
    val activeClients by viewModel.activeClients.collectAsStateWithLifecycle()
    val packages by viewModel.packages.collectAsStateWithLifecycle()
    val quotas by viewModel.quotas.collectAsStateWithLifecycle()
    val portalPolicy by viewModel.portalPolicy.collectAsStateWithLifecycle()

    val selectedClientForApproval by viewModel.selectedClientForApproval.collectAsStateWithLifecycle()
    val editingRule by viewModel.editingRule.collectAsStateWithLifecycle()
    val editingPackage by viewModel.editingPackage.collectAsStateWithLifecycle()
    val editingQuota by viewModel.editingQuota.collectAsStateWithLifecycle()

    // Handle back button
    BackHandler(enabled = currentScreen != Screen.REQUESTS) {
        currentScreen = Screen.REQUESTS
    }

    // Listen for transient user feedback
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Starlink Guest Portal Header with Creator Signature matching dashboard v3.7
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .border(0.5.dp, OutlineDark, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanNeon.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GUESTLINK",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Starlink Portal · By Sadeepa Dilshan (@1tz.5adiy4)",
                                color = CyanNeon,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Free Data Window Indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (portalPolicy.defaultFreeEnabled) EmeraldActive.copy(alpha = 0.15f) else SurfaceDark)
                                .border(1.dp, if (portalPolicy.defaultFreeEnabled) EmeraldActive.copy(alpha = 0.4f) else OutlineDark, RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (portalPolicy.defaultFreeEnabled) "Free ${portalPolicy.freeWindowStart}–${portalPolicy.freeWindowEnd}" else "v3.7 Policy",
                                color = if (portalPolicy.defaultFreeEnabled) EmeraldLight else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // In-app About Page icon button
                        IconButton(
                            onClick = {
                                currentScreen = if (currentScreen == Screen.ABOUT) Screen.REQUESTS else Screen.ABOUT
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (currentScreen == Screen.ABOUT) CyanNeon.copy(alpha = 0.2f) else Color.Transparent)
                                .testTag("btn_about_page")
                        ) {
                            Icon(
                                imageVector = if (currentScreen == Screen.ABOUT) Icons.Default.Close else Icons.Default.Info,
                                contentDescription = "About Developer & Versions",
                                tint = if (currentScreen == Screen.ABOUT) CyanNeon else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // 01 Requests
                NavigationBarItem(
                    selected = currentScreen == Screen.REQUESTS,
                    onClick = { currentScreen = Screen.REQUESTS },
                    icon = {
                        if (pendingClients.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = AmberPending) {
                                        Text("${pendingClients.size}", color = Color.Black)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Devices,
                                    contentDescription = "Requests"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = "Requests"
                            )
                        }
                    },
                    label = { Text("01 Requests", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanNeon,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_requests")
                )

                // 02 Packages
                NavigationBarItem(
                    selected = currentScreen == Screen.PACKAGES,
                    onClick = { currentScreen = Screen.PACKAGES },
                    icon = {
                        if (packages.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = CyanNeon) {
                                        Text("${packages.size}", color = Color.Black)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = "Packages"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = "Packages"
                            )
                        }
                    },
                    label = { Text("02 Packages", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanNeon,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_packages")
                )

                // 03 Quotas
                NavigationBarItem(
                    selected = currentScreen == Screen.QUOTAS,
                    onClick = { currentScreen = Screen.QUOTAS },
                    icon = {
                        if (quotas.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = EmeraldActive) {
                                        Text("${quotas.size}", color = Color.Black)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DataUsage,
                                    contentDescription = "Quotas"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.DataUsage,
                                contentDescription = "Quotas"
                            )
                        }
                    },
                    label = { Text("03 Quotas", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanNeon,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_quotas")
                )

                // 04 Policy
                NavigationBarItem(
                    selected = currentScreen == Screen.POLICY,
                    onClick = { currentScreen = Screen.POLICY },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = "Policy"
                        )
                    },
                    label = { Text("04 Policy", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanNeon,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_policy")
                )

                // Gateway
                NavigationBarItem(
                    selected = currentScreen == Screen.GATEWAY,
                    onClick = { currentScreen = Screen.GATEWAY },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = "Gateway"
                        )
                    },
                    label = { Text("Gateway", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanNeon,
                        selectedTextColor = CyanNeon,
                        indicatorColor = CyanNeon.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_gateway")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { target ->
                when (target) {
                    Screen.REQUESTS -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToActive = { currentScreen = Screen.GATEWAY },
                        onNavigateToRules = { currentScreen = Screen.POLICY }
                    )
                    Screen.PACKAGES -> PackagesScreen(viewModel = viewModel)
                    Screen.QUOTAS -> QuotasScreen(viewModel = viewModel)
                    Screen.POLICY -> AutomationRulesScreen(viewModel = viewModel)
                    Screen.GATEWAY -> RouterSettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAbout = { currentScreen = Screen.ABOUT }
                    )
                    Screen.ABOUT -> AboutScreen(
                        onBackClick = { currentScreen = Screen.REQUESTS }
                    )
                }
            }

            // Custom approval dialog
            selectedClientForApproval?.let { client ->
                ApprovalDialog(
                    client = client,
                    onDismiss = { viewModel.closeApprovalDialog() },
                    onConfirm = { durationMins, downKbps, upKbps, quotaMb ->
                        viewModel.manualApprove(client, durationMins, downKbps, upKbps, quotaMb)
                    }
                )
            }

            // Rule edit dialog
            editingRule?.let { rule ->
                RuleEditDialog(
                    initialRule = if (rule.id == 0L && rule.name.isBlank()) null else rule,
                    onDismiss = { viewModel.closeRuleEditor() },
                    onSave = { updatedRule -> viewModel.saveRule(updatedRule) }
                )
            }

            // Package edit/assign dialog
            editingPackage?.let { pkg ->
                PackageEditDialog(
                    initialPackage = if (pkg.mac.isBlank()) null else pkg,
                    onDismiss = { viewModel.closePackageEditor() },
                    onSave = { updatedPkg -> viewModel.assignPackage(updatedPkg) }
                )
            }

            // Quota edit dialog
            editingQuota?.let { account ->
                QuotaEditDialog(
                    initialAccount = if (account.mac.isBlank()) null else account,
                    onDismiss = { viewModel.closeQuotaEditor() },
                    onSave = { mac, downMib, upMib -> viewModel.saveQuotaLimits(mac, downMib, upMib) }
                )
            }
        }
    }
}
