package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RouterConfig
import com.example.data.model.RouterProtocol
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
import kotlinx.coroutines.launch

@Composable
fun RouterSettingsScreen(
    viewModel: MainViewModel,
    onNavigateToAbout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentConfig by viewModel.routerConfig.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var name by remember(currentConfig) { mutableStateOf(currentConfig.name) }
    var host by remember(currentConfig) { mutableStateOf(currentConfig.host) }
    var portText by remember(currentConfig) { mutableStateOf(currentConfig.port.toString()) }
    var protocol by remember(currentConfig) { mutableStateOf(currentConfig.protocol) }
    var username by remember(currentConfig) { mutableStateOf(currentConfig.username) }
    var password by remember(currentConfig) { mutableStateOf(currentConfig.password) }
    var cgiPath by remember(currentConfig) { mutableStateOf(currentConfig.cgiPath) }
    var pollInterval by remember(currentConfig) { mutableIntStateOf(currentConfig.pollIntervalSeconds) }

    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .testTag("router_settings_screen")
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OpenWrt Gateway Settings",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure how GuestLink interfaces with your Starlink / openNDS portal",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Connection Protocol Selector Card
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
                        text = "INTEGRATION PROTOCOL",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    // Option 1: Starlink Guest Portal v3.7
                    ProtocolOptionRow(
                        title = "Starlink Guest Portal & Policy Dashboard v3.7",
                        subtitle = "Native integration with your v3.7 dashboard (/cgi-bin/requests & live-manager.sh). Reads guest names, phones, and automates requests & cutoffs.",
                        isSelected = protocol == RouterProtocol.STARLINK_PORTAL_V3,
                        icon = Icons.Filled.Router,
                        accentColor = CyanNeon,
                        onClick = {
                            protocol = RouterProtocol.STARLINK_PORTAL_V3
                            if (cgiPath.isBlank() || cgiPath == "/cgi-bin/ndsctl") {
                                cgiPath = "/cgi-bin/requests"
                            }
                        }
                    )

                    // Option 2: Builtin Simulator
                    ProtocolOptionRow(
                        title = "Virtual openNDS Simulator (Interactive Demo)",
                        subtitle = "Simulates authentic OpenWrt openNDS daemon locally. Best for testing automations & rules immediately.",
                        isSelected = protocol == RouterProtocol.BUILTIN_SIMULATOR,
                        icon = Icons.Filled.Science,
                        accentColor = EmeraldActive,
                        onClick = { protocol = RouterProtocol.BUILTIN_SIMULATOR }
                    )

                    // Option 3: OpenWrt Ubus RPC
                    ProtocolOptionRow(
                        title = "OpenWrt Ubus JSON-RPC (/ubus)",
                        subtitle = "Native OpenWrt RPC API. Communicates directly with /usr/bin/ndsctl via session authentication.",
                        isSelected = protocol == RouterProtocol.OPENWRT_UBUS,
                        icon = Icons.Filled.Dns,
                        accentColor = Color(0xFF818CF8),
                        onClick = { protocol = RouterProtocol.OPENWRT_UBUS }
                    )

                    // Option 4: openNDS REST CGI
                    ProtocolOptionRow(
                        title = "Direct openNDS HTTP / CGI (/cgi-bin/ndsctl)",
                        subtitle = "Queries custom CGI endpoint (e.g. /cgi-bin/ndsctl) or web FAS bridge.",
                        isSelected = protocol == RouterProtocol.OPENNDS_REST_CGI,
                        icon = Icons.Filled.Lan,
                        accentColor = TextSecondary,
                        onClick = { protocol = RouterProtocol.OPENNDS_REST_CGI }
                    )
                }
            }
        }

        // Router Details form
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
                        text = "CONNECTION PARAMETERS",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Gateway Profile Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("Router IP / Host") },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Lan, contentDescription = null, tint = CyanNeon) },
                            singleLine = true,
                            modifier = Modifier.weight(2f)
                        )

                        OutlinedTextField(
                            value = portText,
                            onValueChange = { portText = it },
                            label = { Text("Port") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (protocol == RouterProtocol.OPENWRT_UBUS) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("OpenWrt Username (e.g. root)") },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("OpenWrt Password / LuCI token") },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Password, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (protocol == RouterProtocol.OPENNDS_REST_CGI) {
                        OutlinedTextField(
                            value = cgiPath,
                            onValueChange = { cgiPath = it },
                            label = { Text("CGI Script Endpoint Path") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Polling Interval
                    Text(
                        text = "Background Polling Frequency: ${pollInterval}s",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 4, 8, 15, 30).forEach { sec ->
                            FilterChip(
                                selected = pollInterval == sec,
                                onClick = { pollInterval = sec },
                                label = { Text("${sec}s") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Test & Save Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isTesting = true
                                testStatus = null
                                scope.launch {
                                    val testConfig = currentConfig.copy(
                                        name = name,
                                        host = host,
                                        port = portText.toIntOrNull() ?: 80,
                                        protocol = protocol,
                                        username = username,
                                        password = password,
                                        cgiPath = cgiPath
                                    )
                                    val res = viewModel.repository.fetchClients(testConfig)
                                    isTesting = false
                                    testStatus = if (res.isSuccess) {
                                        "✓ Connection successful! Retrieved ${res.getOrNull()?.size ?: 0} openNDS clients"
                                    } else {
                                        "✗ Failed: ${res.exceptionOrNull()?.message}"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("test_connection_button")
                                .weight(1f)
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Filled.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Probe")
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.updateConfig(
                                    currentConfig.copy(
                                        name = name,
                                        host = host,
                                        port = portText.toIntOrNull() ?: 80,
                                        protocol = protocol,
                                        username = username,
                                        password = password,
                                        cgiPath = cgiPath,
                                        pollIntervalSeconds = pollInterval
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("save_config_button")
                                .weight(1f)
                        ) {
                            Icon(imageVector = Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Config")
                        }
                    }

                    testStatus?.let { msg ->
                        Text(
                            text = msg,
                            color = if (msg.startsWith("✓")) EmeraldLight else RedLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Connection Guide & Setup Instructions
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(16.dp))
                    .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanNeon.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lan,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "How to Connect GuestLink to Router",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Setup guide for OpenWrt & Starlink Portal v3.7",
                                color = CyanNeon,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    ConnectionStepItem(
                        step = "1",
                        title = "Connect to Router Wi-Fi / Admin LAN",
                        desc = "Connect your Android phone to the router's Admin Wi-Fi or LAN. By default, allow-client.sh permits clients on subnet 192.168.2.* or 100.* (or any trusted IP you configure on OpenWrt)."
                    )

                    ConnectionStepItem(
                        step = "2",
                        title = "Verify Router IP Address",
                        desc = "Ensure the Router IP / Host field above matches your gateway (default: 192.168.2.1, or 192.168.1.1) and port 80 (or 443)."
                    )

                    ConnectionStepItem(
                        step = "3",
                        title = "Select Starlink Portal v3.7 Protocol",
                        desc = "Select 'Starlink Guest Portal & Policy Dashboard v3.7' above. The CGI path is set to /cgi-bin/requests to interface with live-manager.sh."
                    )

                    ConnectionStepItem(
                        step = "4",
                        title = "Tap 'Test Probe' & Save",
                        desc = "Tap 'Test Probe' to check HTTP reachability. If you are not connected to the router right now, switch to 'Virtual openNDS Simulator' to test and demo the entire app offline."
                    )
                }
            }
        }

        // OpenNDS Architecture & Info Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1726), RoundedCornerShape(14.dp))
                    .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "How openNDS Automation Works",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When new clients connect to your WiFi, openNDS redirects them into Preauthenticated state. GuestLink detects this request within seconds, checks your Auto-Approval rules, and issues 'ndsctl auth' with granted duration and bandwidth limits. When the session timer lapses, GuestLink immediately issues 'ndsctl deauth' to enforce expiration.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
                    .clickable { onNavigateToAbout() }
                    .testTag("gateway_about_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanNeon.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "About GuestLink & Developer",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "By Sadeepa Dilshan (@1tz.5adiy4) · v3.7 Suite",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceDark)
                            .border(0.5.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "View",
                            color = CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProtocolOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SurfaceDark else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) accentColor.copy(alpha = 0.6f) else OutlineDark,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ConnectionStepItem(
    step: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(0.5.dp, OutlineDark, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(CyanNeon.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = CyanNeon,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

