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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.example.data.model.GuestPackage
import com.example.ui.MainViewModel
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
fun PackagesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val packages by viewModel.packages.collectAsStateWithLifecycle()
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Active, 2: Pending, 3: Expired

    val activeCount = packages.count { it.status == "active" }
    val pendingCount = packages.count { it.status == "pending" }
    val expiredCount = packages.count { it.status == "expired" }

    val filtered = when (selectedFilterTab) {
        1 -> packages.filter { it.status == "active" }
        2 -> packages.filter { it.status == "pending" }
        3 -> packages.filter { it.status == "expired" }
        else -> packages
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openPackageEditor(null) },
                containerColor = CyanPrimary,
                contentColor = Color.Black,
                modifier = Modifier.testTag("assign_package_fab")
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Assign Package")
            }
        },
        containerColor = BackgroundDark,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .testTag("packages_screen")
                .fillMaxSize()
                .padding(innerPadding)
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
                            text = "Guest Portal Packages",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage bandwidth allocations, 4-tier speeds, and after-quota policies",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Summary metrics
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMetricCard(
                        title = "Active Packages",
                        value = "$activeCount",
                        subtitle = "Online allowances",
                        icon = Icons.Filled.Inventory2,
                        accentColor = EmeraldActive,
                        modifier = Modifier.weight(1f)
                    )

                    StatMetricCard(
                        title = "Pending Activation",
                        value = "$pendingCount",
                        subtitle = "Awaiting activate",
                        icon = Icons.Filled.HourglassTop,
                        accentColor = AmberPending,
                        modifier = Modifier.weight(1f)
                    )

                    StatMetricCard(
                        title = "Expired",
                        value = "$expiredCount",
                        subtitle = "Awaiting renewal",
                        icon = Icons.Filled.Timer,
                        accentColor = RedLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Traffic Shaper Diagnostics (SQM / HTB / IFB - package-shaper.sh check)
            item {
                val shaperStatus by viewModel.shaperStatus.collectAsStateWithLifecycle()
                var showShaperDetails by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, OutlineDark, RoundedCornerShape(12.dp))
                        .padding(12.dp)
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (shaperStatus.isHtbConfigured) EmeraldActive else AmberPending)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "SQM TRAFFIC SHAPER (HTB / IFB)",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "br-guest (1:1) • ifb4br-guest • Pref 0 Verified",
                                        color = if (shaperStatus.isHtbConfigured) EmeraldLight else AmberLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { viewModel.checkTrafficShaper() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Autorenew,
                                        contentDescription = "Re-check shaper",
                                        tint = CyanNeon,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showShaperDetails = !showShaperDetails },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (showShaperDetails) "Hide" else "Inspect",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        if (showShaperDetails) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BackgroundDark)
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = shaperStatus.statusText,
                                        color = TextPrimary,
                                        fontSize = 11.sp
                                    )
                                    shaperStatus.activeShaperClasses.forEach { cls ->
                                        Text(
                                            text = "• $cls",
                                            color = CyanNeon,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedFilterTab,
                    containerColor = SurfaceDark,
                    contentColor = CyanNeon,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedFilterTab == 0,
                        onClick = { selectedFilterTab = 0 },
                        text = { Text("All (${packages.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedFilterTab == 1,
                        onClick = { selectedFilterTab = 1 },
                        text = { Text("Active ($activeCount)", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedFilterTab == 2,
                        onClick = { selectedFilterTab = 2 },
                        text = { Text("Pending ($pendingCount)", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedFilterTab == 3,
                        onClick = { selectedFilterTab = 3 },
                        text = { Text("Expired ($expiredCount)", fontSize = 12.sp) }
                    )
                }
            }

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
                            Icon(imageVector = Icons.Filled.Inventory2, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No packages in this category",
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.mac }) { pkg ->
                    PackageCard(
                        pkg = pkg,
                        onActivate = { viewModel.activatePackage(pkg.mac) },
                        onRenew = { viewModel.renewPackage(pkg.mac) },
                        onEdit = { viewModel.openPackageEditor(pkg) },
                        onRemove = { viewModel.removePackage(pkg.mac) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun PackageCard(
    pkg: GuestPackage,
    onActivate: () -> Unit,
    onRenew: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val statusColor = when (pkg.status) {
        "active" -> EmeraldLight
        "pending" -> AmberLight
        else -> RedLight
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header Row: MAC, Phone, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = pkg.mac,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (pkg.phone.isNotBlank()) {
                        Text(
                            text = "Phone: ${pkg.phone}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = pkg.status.uppercase(),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quotas & Validity Info Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Download Allowance", color = TextSecondary, fontSize = 11.sp)
                    Text(pkg.downloadLimitFormatted(), color = CyanNeon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Upload Allowance", color = TextSecondary, fontSize = 11.sp)
                    Text(pkg.uploadLimitFormatted(), color = EmeraldActive, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Validity", color = TextSecondary, fontSize = 11.sp)
                    Text("${pkg.validityDays} Days", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Speeds & After-Quota Action
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
                    Text(
                        text = "Normal: ${pkg.downloadKbps}/${pkg.uploadKbps} kbps",
                        color = TextPrimary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "After-Quota: ${pkg.afterQuotaDownloadKbps}/${pkg.afterQuotaUploadKbps} (${pkg.afterQuotaAction.replaceFirstChar { it.uppercase() }})",
                        color = AmberLight,
                        fontSize = 11.sp
                    )
                }

                if (pkg.freeDataEnabled) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Free Window: On", color = CyanNeon, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions row: Activate, Renew, Edit, Remove
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pkg.status == "pending") {
                    Button(
                        onClick = onActivate,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldActive),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activate", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onRenew,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Renew", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 11.sp)
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Remove Package", tint = RedLight, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
