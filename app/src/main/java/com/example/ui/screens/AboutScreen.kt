package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.OutlineLightDark
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/MR54V4G3/GUESTLINK"

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open browser. Link copied instead.", Toast.LENGTH_SHORT).show()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("GitHub Link", url))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Navigation Back & Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("about_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CyanNeon
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "About GuestLink",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Versions, Architecture & Repository",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
                    .testTag("about_hero_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CyanNeon.copy(alpha = 0.15f))
                                .border(1.5.dp, CyanNeon.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "GuestLink",
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldActive.copy(alpha = 0.2f))
                                        .border(0.5.dp, EmeraldActive, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "v3.7 PRO",
                                        color = EmeraldLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Starlink OpenWrt / openNDS Captive Portal Manager",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Native Android control center for automating client session approvals, multi-tier bandwidth shaping (SQM), whole-number quota accounting, and preemptive return-device authentication on Starlink routers.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { openUrl(githubUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("about_github_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GitHub",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Versions & System Stack Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
                    .testTag("about_versions_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM & ENGINE VERSIONS",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    VersionRow(
                        label = "Android Application",
                        version = "GuestLink v1.0.0",
                        subtext = "Build 1 · Jetpack Compose / M3",
                        icon = Icons.Default.Wifi,
                        badgeColor = EmeraldActive
                    )
                    VersionRow(
                        label = "Guest Portal Dashboard",
                        version = "v3.7 (November 2026)",
                        subtext = "By Sadeepa Dilshan (@1tz.5adiy4)",
                        icon = Icons.Default.Router,
                        badgeColor = CyanNeon
                    )
                    VersionRow(
                        label = "openNDS Captive Core",
                        version = "v10.3.1+ (API 4)",
                        subtext = "Automated ndsctl auth/deauth retry",
                        icon = Icons.Default.Security,
                        badgeColor = IndigoAccent
                    )
                    VersionRow(
                        label = "Traffic Control (SQM)",
                        version = "package-shaper.sh",
                        subtext = "Linux tc + ifb4br-guest multi-speed",
                        icon = Icons.Default.Speed,
                        badgeColor = AmberPending
                    )
                    VersionRow(
                        label = "Returning Device Watcher",
                        version = "guest-return-auth v3.7",
                        subtext = "Preemptive MAC background daemon",
                        icon = Icons.Default.Terminal,
                        badgeColor = PurpleAccent
                    )
                    VersionRow(
                        label = "Host Operating System",
                        version = "OpenWrt 23.05.x",
                        subtext = "Linux 5.15 / 6.1 on Starlink Gateway",
                        icon = Icons.Default.Layers,
                        badgeColor = TextSecondary,
                        isLast = true
                    )
                }
            }
        }

        // Version Evolution / Changelog Highlights
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
                    .testTag("about_changelog_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = AmberLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VERSION EVOLUTION & CHANGELOG",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ChangelogItem(
                        version = "v3.7 — Preauthenticated State Bypass & Bounded Retry",
                        date = "November 2026",
                        points = listOf(
                            "Avoids redundant deauth calls on unauthenticated Preauthenticated clients",
                            "Cleans returning-auth attempt-cache and enforces return-deny markers",
                            "Bounded ndsctl error 4 retry logic for busy control sockets",
                            "Single-shot poll accounting lock ordering protection"
                        ),
                        isCurrent = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ChangelogItem(
                        version = "v3.6 — Preemptive Return-Auth & Deny Markers",
                        date = "November 2026",
                        points = listOf(
                            "Preemptive MAC watcher authorises known devices before captive popup",
                            "Portal-scoped return-deny markers to prevent watcher bypass on revoked accounts"
                        ),
                        isCurrent = false
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ChangelogItem(
                        version = "v3.5 — 4-Speed Traffic Shaping & Whole Quotas",
                        date = "November 2026",
                        points = listOf(
                            "4-speed profiles (normal + after-quota upload and download rates)",
                            "Per-package Throttle vs. Disconnect action",
                            "Whole-number MiB quotas (1 to 1,048,576 MiB) with real-time reset",
                            "Free data time window schedule exclusion"
                        ),
                        isCurrent = false
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun VersionRow(
    label: String,
    version: String,
    subtext: String,
    icon: ImageVector,
    badgeColor: Color,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = label,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtext,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceDark)
                    .border(0.5.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = version,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!isLast) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = OutlineDark, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ChangelogItem(
    version: String,
    date: String,
    points: List<String>,
    isCurrent: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCurrent) CyanNeon.copy(alpha = 0.05f) else SurfaceDark)
            .border(
                1.dp,
                if (isCurrent) CyanNeon.copy(alpha = 0.3f) else OutlineDark,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = version,
                    color = if (isCurrent) CyanNeon else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = date,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            points.forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = if (isCurrent) CyanNeon else TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = point,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
