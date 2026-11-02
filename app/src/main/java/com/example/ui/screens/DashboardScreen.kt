package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ClientSession
import com.example.data.model.RouterProtocol
import com.example.ui.MainViewModel
import com.example.ui.components.ActiveClientCard
import com.example.ui.components.PendingClientCard
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPending
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
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToActive: () -> Unit,
    onNavigateToRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routerConfig by viewModel.routerConfig.collectAsStateWithLifecycle()
    val pendingClients by viewModel.pendingClients.collectAsStateWithLifecycle()
    val activeClients by viewModel.activeClients.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .testTag("dashboard_screen")
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header & Gateway Banner
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, OutlineDark, RoundedCornerShape(20.dp))
            ) {
                // Banner graphic
                Image(
                    painter = painterResource(id = R.drawable.img_router_banner_1793651948997),
                    contentDescription = "openNDS Network Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.95f))
                            )
                        )
                )

                // Header Content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .align(Alignment.BottomStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (metrics.syncError == null) EmeraldActive else RedLight,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = routerConfig.name,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (routerConfig.protocol == RouterProtocol.BUILTIN_SIMULATOR)
                                "Interactive Simulation Mode (Virtual Router)"
                            else "OpenWrt ${routerConfig.host}:${routerConfig.port} (${routerConfig.protocol.name})",
                            color = CyanNeon,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshNow() },
                        modifier = Modifier
                            .testTag("refresh_status_button")
                            .size(36.dp)
                            .background(SurfaceDark.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Sync Gateway",
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Master Automation Toggle Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (routerConfig.isAutomationActive)
                            Brush.horizontalGradient(listOf(Color(0xFF0D2538), Color(0xFF0F1E33)))
                        else
                            Brush.horizontalGradient(listOf(Color(0xFF1E2129), Color(0xFF14171F))),
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.5.dp,
                        if (routerConfig.isAutomationActive) CyanNeon.copy(alpha = 0.4f) else OutlineDark,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (routerConfig.isAutomationActive) CyanNeon.copy(alpha = 0.15f) else Color.DarkGray,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoMode,
                                contentDescription = null,
                                tint = if (routerConfig.isAutomationActive) CyanNeon else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "AUTOMATION ENGINE",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (routerConfig.isAutomationActive) "Active & Enforcing Rules" else "Automation Paused",
                                color = if (routerConfig.isAutomationActive) EmeraldLight else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto-approves incoming requests & deauths on expiration",
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = routerConfig.isAutomationActive,
                        onCheckedChange = { viewModel.toggleAutomation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyanNeon,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceDark
                        ),
                        modifier = Modifier.testTag("automation_master_switch")
                    )
                }
            }
        }

        // Live Telemetry Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMetricCard(
                        title = "Pending Queue",
                        value = "${metrics.pendingCount}",
                        subtitle = if (metrics.pendingCount > 0) "Requires decision" else "Queue clear",
                        icon = Icons.Filled.HourglassTop,
                        accentColor = AmberPending,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_pending_queue"
                    )

                    StatMetricCard(
                        title = "Active Sessions",
                        value = "${metrics.activeCount}",
                        subtitle = "Online clients",
                        icon = Icons.Filled.CheckCircle,
                        accentColor = EmeraldActive,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_active_sessions"
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMetricCard(
                        title = "Auto-Approved",
                        value = "${metrics.autoApprovedToday}",
                        subtitle = "Via rule matching",
                        icon = Icons.Filled.AutoMode,
                        accentColor = CyanNeon,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_auto_approved"
                    )

                    StatMetricCard(
                        title = "Auto-Expired",
                        value = "${metrics.autoExpiredToday}",
                        subtitle = "Duration/quota cutoff",
                        icon = Icons.Filled.TimerOff,
                        accentColor = RedLight,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_auto_expired"
                    )
                }
            }
        }

        // Quick Simulator Action Bar (shown when simulator is active)
        if (routerConfig.protocol == RouterProtocol.BUILTIN_SIMULATOR) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131D31), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF223554), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🧪 Simulator Controls (Test Automations):",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = { viewModel.resetSimulation() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RestartAlt,
                                    contentDescription = "Reset",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.spawnSimulatedGuest() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("+ Visitor", color = CyanNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.fastForwardSimulation(15) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Filled.FastForward, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+15m", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.fastForwardSimulation(60) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Filled.FastForward, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+60m", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section: Pending Approvals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pending Approvals",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (pendingClients.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(AmberPending, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${pendingClients.size}",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!routerConfig.isAutomationActive) {
                    Text(
                        text = "Manual Mode",
                        color = AmberLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (pendingClients.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(14.dp))
                        .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldActive,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Pending Authorization Requests",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (routerConfig.isAutomationActive)
                                "Automation engine is approving clients matching rules automatically"
                            else "New captive portal requests will appear here for review",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(pendingClients, key = { it.mac }) { client ->
                PendingClientCard(
                    client = client,
                    onQuickApprove = { duration -> viewModel.quickApprove(client, duration) },
                    onCustomApprove = { viewModel.openApprovalDialog(client) },
                    onBlock = { viewModel.blockClient(client) }
                )
            }
        }

        // Section: Active Client Sessions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Active Client Sessions (${activeClients.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (activeClients.isNotEmpty()) {
                    Text(
                        text = "Auto-expiring on timeout",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (activeClients.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(14.dp))
                        .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No clients currently authenticated",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // Show up to 4 in dashboard, or all
            items(activeClients.take(4), key = { it.mac }) { client ->
                ActiveClientCard(
                    client = client,
                    onExtend = { mins -> viewModel.extendSession(client, mins) },
                    onDeauth = { viewModel.deauthenticateClient(client) }
                )
            }

            if (activeClients.size > 4) {
                item {
                    OutlinedButton(
                        onClick = onNavigateToActive,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View All ${activeClients.size} Active Sessions")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
