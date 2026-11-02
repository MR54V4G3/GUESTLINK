package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientSession
import com.example.data.model.ClientState
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.RedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun ActiveClientCard(
    client: ClientSession,
    onExtend: (extraMinutes: Int) -> Unit,
    onDeauth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingSec = client.remainingSeconds()
    val remainingMinutes = remainingSec / 60
    val remainingSecMod = remainingSec % 60

    val timeText = if (remainingMinutes >= 60) {
        val hours = remainingMinutes / 60
        val mins = remainingMinutes % 60
        "${hours}h ${mins}m left"
    } else {
        String.format(Locale.US, "%02d:%02d left", remainingMinutes, remainingSecMod)
    }

    val progress = client.progressFraction()
    val progressRemaining = 1f - progress

    val urgent = remainingMinutes < 5
    val warning = remainingMinutes < 15

    val timerColor by animateColorAsState(
        targetValue = when {
            urgent -> RedLight
            warning -> AmberLight
            else -> EmeraldLight
        },
        label = "timerColor"
    )

    val deviceIcon = when {
        client.hostname?.contains("iphone", ignoreCase = true) == true ||
        client.vendor?.contains("apple", ignoreCase = true) == true -> Icons.Filled.PhoneIphone
        client.hostname?.contains("android", ignoreCase = true) == true ||
        client.hostname?.contains("pixel", ignoreCase = true) == true ||
        client.hostname?.contains("galaxy", ignoreCase = true) == true -> Icons.Filled.PhoneAndroid
        client.hostname?.contains("mac", ignoreCase = true) == true ||
        client.hostname?.contains("laptop", ignoreCase = true) == true ||
        client.hostname?.contains("pc", ignoreCase = true) == true -> Icons.Filled.Computer
        else -> Icons.Filled.Wifi
    }

    Box(
        modifier = modifier
            .testTag("active_client_${client.mac.replace(":", "_")}")
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header Row: Device icon, name, countdown timer badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(timerColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = deviceIcon,
                        contentDescription = null,
                        tint = timerColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = client.displayName(),
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${client.ip} • ${client.mac}",
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }

                // Countdown badge
                Box(
                    modifier = Modifier
                        .background(timerColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = timerColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeText,
                            color = timerColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar showing time remaining
            LinearProgressIndicator(
                progress = { progressRemaining.coerceIn(0f, 1f) },
                color = timerColor,
                trackColor = Color(0xFF1E293B),
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bandwidth & Limits telemetry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Downloader
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", client.bytesDownloaded / (1024.0 * 1024.0))} MB",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Uploader
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = EmeraldActive,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", client.bytesUploaded / (1024.0 * 1024.0))} MB",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Rate limit
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val rateDesc = if (client.downloadRateLimitKbps > 0) {
                        "${client.downloadRateLimitKbps / 1000} Mbps"
                    } else "Uncapped"
                    Text(
                        text = rateDesc,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Session Extension & Termination Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onExtend(30) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("+30m", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { onExtend(60) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("+1h", fontSize = 11.sp)
                }

                Button(
                    onClick = onDeauth,
                    colors = ButtonDefaults.buttonColors(containerColor = RedLight.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = "Terminate Session",
                        tint = RedLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deauth", color = RedLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
