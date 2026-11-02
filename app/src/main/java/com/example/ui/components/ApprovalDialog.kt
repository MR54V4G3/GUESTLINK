package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ClientSession
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ApprovalDialog(
    client: ClientSession,
    onDismiss: () -> Unit,
    onConfirm: (durationMinutes: Int, downRateKbps: Int, upRateKbps: Int, downQuotaMb: Int) -> Unit
) {
    var durationMinutes by remember { mutableIntStateOf(60) }
    var downloadSpeedMbps by remember { mutableFloatStateOf(10f) }
    var uploadSpeedMbps by remember { mutableFloatStateOf(3f) }
    var quotaMbText by remember { mutableStateOf("1000") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .testTag("custom_approval_dialog")
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Authorize Client",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${client.displayName()} (${client.ip})",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Session Duration Preset Chips
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Timer, contentDescription = null, tint = CyanNeon, modifier = Modifier.padding(end = 6.dp))
                    Text(
                        text = "Session Duration: $durationMinutes minutes",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(30, 60, 120, 240, 1440).forEach { mins ->
                        val label = when (mins) {
                            1440 -> "24h"
                            240 -> "4h"
                            120 -> "2h"
                            60 -> "1h"
                            else -> "${mins}m"
                        }
                        FilterChip(
                            selected = durationMinutes == mins,
                            onClick = { durationMinutes = mins },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Download Rate Limit
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Speed, contentDescription = null, tint = CyanNeon, modifier = Modifier.padding(end = 6.dp))
                    Text(
                        text = "Download Limit: ${downloadSpeedMbps.toInt()} Mbps",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Slider(
                    value = downloadSpeedMbps,
                    onValueChange = { downloadSpeedMbps = it },
                    valueRange = 1f..100f,
                    steps = 19
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Upload Rate Limit
                Text(
                    text = "Upload Limit: ${uploadSpeedMbps.toInt()} Mbps",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 24.dp)
                )

                Slider(
                    value = uploadSpeedMbps,
                    onValueChange = { uploadSpeedMbps = it },
                    valueRange = 1f..50f,
                    steps = 9
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quota input
                OutlinedTextField(
                    value = quotaMbText,
                    onValueChange = { quotaMbText = it },
                    label = { Text("Transfer Quota (MB, 0 = Unlimited)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.DataUsage, contentDescription = null, tint = CyanNeon)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val quota = quotaMbText.toIntOrNull() ?: 0
                            val downKbps = (downloadSpeedMbps * 1000).toInt()
                            val upKbps = (uploadSpeedMbps * 1000).toInt()
                            onConfirm(durationMinutes, downKbps, upKbps, quota)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldActive),
                        modifier = Modifier
                            .testTag("confirm_approval_button")
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Authorize")
                    }
                }
            }
        }
    }
}
