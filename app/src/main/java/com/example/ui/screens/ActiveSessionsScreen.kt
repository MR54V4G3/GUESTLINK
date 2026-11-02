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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClientSession
import com.example.ui.MainViewModel
import com.example.ui.components.ActiveClientCard
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.RedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ActiveSessionsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeClients by viewModel.activeClients.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showDisconnectAllDialog by remember { mutableStateOf(false) }

    val filtered = activeClients.filter {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) true
        else {
            it.mac.lowercase().contains(q) ||
            it.ip.contains(q) ||
            (it.hostname?.lowercase()?.contains(q) == true) ||
            (it.vendor?.lowercase()?.contains(q) == true)
        }
    }

    if (showDisconnectAllDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectAllDialog = false },
            title = { Text("Disconnect All Active Clients?") },
            text = { Text("This will send an immediate ndsctl deauth signal to all ${activeClients.size} authenticated devices. They will be redirected back to the captive portal.") },
            confirmButton = {
                Button(
                    onClick = {
                        activeClients.forEach { viewModel.deauthenticateClient(it) }
                        showDisconnectAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedLight)
                ) {
                    Text("Deauth All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDisconnectAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .testTag("active_sessions_screen")
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Active Client Sessions",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${activeClients.size} authenticated devices currently online",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (activeClients.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showDisconnectAllDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.DeleteSweep, contentDescription = null, tint = RedLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deauth All", color = RedLight, fontSize = 11.sp)
                    }
                }
            }
        }

        // Bandwidth Total Meter
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(14.dp))
                    .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", metrics.totalDataRxMb)} MB",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("Total Downloaded", color = TextSecondary, fontSize = 11.sp)
                    }

                    Box(modifier = Modifier.size(1.dp, 28.dp).background(OutlineDark))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = null, tint = EmeraldActive, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", metrics.totalDataTxMb)} MB",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("Total Uploaded", color = TextSecondary, fontSize = 11.sp)
                    }

                    Box(modifier = Modifier.size(1.dp, 28.dp).background(OutlineDark))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Devices, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${activeClients.size}",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("Online Devices", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by IP, MAC, Hostname, Vendor...") },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .testTag("sessions_search_bar")
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // List of Active Clients
        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(14.dp))
                        .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Filled.Wifi, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No sessions match '$searchQuery'" else "No active sessions",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.mac }) { client ->
                ActiveClientCard(
                    client = client,
                    onExtend = { extraMins -> viewModel.extendSession(client, extraMins) },
                    onDeauth = { viewModel.deauthenticateClient(client) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
