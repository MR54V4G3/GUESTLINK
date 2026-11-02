package com.example.ui.components

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientSession
import com.example.data.model.ClientState
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPending
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.RedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun PendingClientCard(
    client: ClientSession,
    onQuickApprove: (durationMinutes: Int) -> Unit,
    onCustomApprove: () -> Unit,
    onBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            .testTag("pending_client_${client.mac.replace(":", "_")}")
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.5.dp, AmberPending.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header Row: Icon, Title, Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(AmberPending.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = deviceIcon,
                        contentDescription = null,
                        tint = AmberLight,
                        modifier = Modifier.size(22.dp)
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
                        fontSize = 12.sp
                    )
                }

                StatusBadge(state = ClientState.PREAUTHENTICATED)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Details banner: Starlink portal guest info & token
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1A2C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!client.mobile.isNullOrBlank() && client.mobile != "—") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mobile: ${client.mobile}",
                            color = EmeraldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!client.requestId.isNullOrBlank()) {
                            Text(text = " • ", color = TextTertiary, fontSize = 12.sp)
                            Text(
                                text = client.requestId,
                                color = AmberLight,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (!client.browserHint.isNullOrBlank() && client.browserHint != "Unknown") {
                    Text(
                        text = "Device/Browser: ${client.browserHint}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.HourglassBottom,
                        contentDescription = null,
                        tint = AmberLight,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Awaiting authorization (Token: ${client.token ?: client.requestId ?: "portal"})",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Quick Approvals
            Text(
                text = "Instant Authorization:",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onQuickApprove(30) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldActive.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("30m", color = EmeraldActive, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onQuickApprove(60) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldActive),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("1h", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onQuickApprove(240) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldActive.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("4h", color = EmeraldActive, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCustomApprove,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Custom Approval",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onBlock,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = "Block Device",
                        tint = RedLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
