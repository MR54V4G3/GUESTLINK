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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AuditAction
import com.example.data.model.AuditLogEntry
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.RedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditLogsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.recentAuditLogs.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf<AuditAction?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val filteredLogs = if (selectedFilter == null) logs else logs.filter { it.action == selectedFilter }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Audit Logs?") },
            text = { Text("This will remove all session approval and expiration history records.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLogs()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedLight)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .testTag("audit_logs_screen")
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
                        text = "Automation Audit Log",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time records of approvals, expirations, and cutoffs",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Clear Logs", tint = TextSecondary)
                    }
                }
            }
        }

        // Filter chips row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${logs.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == AuditAction.AUTO_APPROVED,
                        onClick = {
                            selectedFilter = if (selectedFilter == AuditAction.AUTO_APPROVED) null else AuditAction.AUTO_APPROVED
                        },
                        label = { Text("Auto-Approved") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == AuditAction.AUTO_EXPIRED_TIME,
                        onClick = {
                            selectedFilter = if (selectedFilter == AuditAction.AUTO_EXPIRED_TIME) null else AuditAction.AUTO_EXPIRED_TIME
                        },
                        label = { Text("Auto-Expired") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == AuditAction.MANUALLY_APPROVED,
                        onClick = {
                            selectedFilter = if (selectedFilter == AuditAction.MANUALLY_APPROVED) null else AuditAction.MANUALLY_APPROVED
                        },
                        label = { Text("Manual Approvals") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == AuditAction.CLIENT_BLOCKED,
                        onClick = {
                            selectedFilter = if (selectedFilter == AuditAction.CLIENT_BLOCKED) null else AuditAction.CLIENT_BLOCKED
                        },
                        label = { Text("Blocked") }
                    )
                }
            }
        }

        if (filteredLogs.isEmpty()) {
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
                        Icon(imageVector = Icons.Filled.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No audit log records found",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                LogItemCard(entry = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LogItemCard(entry: AuditLogEntry) {
    val (icon, color, label) = when (entry.action) {
        AuditAction.AUTO_APPROVED -> Triple(Icons.Filled.AutoMode, CyanNeon, "AUTO APPROVED")
        AuditAction.MANUALLY_APPROVED -> Triple(Icons.Filled.CheckCircle, EmeraldLight, "MANUALLY APPROVED")
        AuditAction.AUTO_EXPIRED_TIME -> Triple(Icons.Filled.TimerOff, RedLight, "AUTO EXPIRED (TIME)")
        AuditAction.AUTO_EXPIRED_QUOTA -> Triple(Icons.Filled.TimerOff, RedLight, "AUTO EXPIRED (QUOTA)")
        AuditAction.MANUALLY_DEAUTHED -> Triple(Icons.Filled.PowerSettingsNew, AmberLight, "DEAUTHENTICATED")
        AuditAction.SESSION_EXTENDED -> Triple(Icons.Filled.Timer, EmeraldActive, "SESSION EXTENDED")
        AuditAction.CLIENT_BLOCKED -> Triple(Icons.Filled.Block, RedLight, "BLOCKED")
        AuditAction.CLIENT_UNBLOCKED -> Triple(Icons.Filled.CheckCircle, CyanNeon, "UNBLOCKED")
        AuditAction.CLIENT_TRUSTED -> Triple(Icons.Filled.CheckCircle, CyanNeon, "TRUSTED")
    }

    val timeFormatted = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss • MMM dd", Locale.getDefault())
        sdf.format(Date(entry.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = timeFormatted,
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entry.details,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${entry.hostname ?: "Client"} • ${entry.ip} • ${entry.mac}",
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}
