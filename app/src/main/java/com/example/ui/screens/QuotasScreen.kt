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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.QuotaAccount
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
import java.util.Locale

@Composable
fun QuotasScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val quotas by viewModel.quotas.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Active, 2: Exhausted
    var searchQuery by remember { mutableStateOf("") }

    val activeCount = quotas.count { !it.isExhausted() && it.status != "blocked" }
    val exhaustedCount = quotas.count { it.isExhausted() }

    val filteredQuotas = quotas.filter { account ->
        val matchesTab = when (selectedTab) {
            1 -> !account.isExhausted()
            2 -> account.isExhausted()
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                account.mac.contains(searchQuery, ignoreCase = true) ||
                account.hostname.contains(searchQuery, ignoreCase = true) ||
                account.ip.contains(searchQuery, ignoreCase = true)

        matchesTab && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openQuotaEditor(null) },
                containerColor = CyanPrimary,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_quota_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Set Quota Account")
            }
        },
        containerColor = BackgroundDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Metrics Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = "Total Quotas",
                    value = quotas.size.toString(),
                    subtitle = "Accounts",
                    icon = Icons.Default.DataUsage,
                    accentColor = CyanNeon,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Active",
                    value = activeCount.toString(),
                    subtitle = "In quota",
                    icon = Icons.Default.CheckCircle,
                    accentColor = EmeraldActive,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Exhausted",
                    value = exhaustedCount.toString(),
                    subtitle = "Quota full",
                    icon = Icons.Default.Warning,
                    accentColor = AmberPending,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by MAC, IP, or hostname...", color = TextTertiary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = OutlineDark,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quota_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = CyanNeon,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, OutlineDark, RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All (${quotas.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Active ($activeCount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Exhausted ($exhaustedCount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quota Accounts List
            if (filteredQuotas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DataUsage,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching quota accounts found" else "No guest quota accounts configured",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredQuotas, key = { it.mac }) { account ->
                        QuotaAccountCard(
                            account = account,
                            onEdit = { viewModel.openQuotaEditor(account) },
                            onRenew = { viewModel.renewQuota(account.mac) },
                            onAuthorize = { viewModel.authorizeQuotaDevice(account.mac) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaAccountCard(
    account: QuotaAccount,
    onEdit: () -> Unit,
    onRenew: () -> Unit,
    onAuthorize: () -> Unit
) {
    val isExhausted = account.isExhausted()
    val statusColor = when {
        account.status == "blocked" -> RedLight
        isExhausted -> AmberPending
        else -> EmeraldActive
    }
    val statusText = when {
        account.status == "blocked" -> "BLOCKED"
        isExhausted -> "EXHAUSTED"
        else -> "ACTIVE"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, OutlineDark, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("quota_card_${account.mac}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: MAC & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceDark)
                            .border(1.dp, OutlineDark, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = account.mac,
                            color = CyanNeon,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (account.hostname.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = account.hostname,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Download Allowance
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download Limit", color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.1f", account.downloadUsedMb())} / ${account.downloadLimitFormatted()}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { account.downloadUsagePercent() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (account.downloadUsagePercent() >= 1f) RedLight else CyanNeon,
                    trackColor = SurfaceDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Upload Allowance
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload Limit", color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.1f", account.uploadUsedMb())} / ${account.uploadLimitFormatted()}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons matching cgi-bin/quotas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_quota_${account.mac}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanNeon)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limits", color = TextPrimary, fontSize = 11.sp)
                }

                Button(
                    onClick = onRenew,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, OutlineDark, RoundedCornerShape(20.dp))
                        .testTag("renew_quota_${account.mac}")
                ) {
                    Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberLight)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", color = TextPrimary, fontSize = 11.sp)
                }

                Button(
                    onClick = onAuthorize,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("auth_quota_${account.mac}")
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Authorize", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
