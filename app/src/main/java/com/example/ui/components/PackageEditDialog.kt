package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.GuestPackage
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PackageEditDialog(
    initialPackage: GuestPackage?,
    onDismiss: () -> Unit,
    onSave: (GuestPackage) -> Unit
) {
    var mac by remember { mutableStateOf(initialPackage?.mac ?: "") }
    var phone by remember { mutableStateOf(initialPackage?.phone ?: "") }
    var downloadMibText by remember { mutableStateOf(initialPackage?.downloadLimitMib ?: "1024") }
    var uploadMibText by remember { mutableStateOf(initialPackage?.uploadLimitMib ?: "256") }
    var validityDaysText by remember { mutableStateOf((initialPackage?.validityDays ?: 7).toString()) }

    var downloadKbpsText by remember { mutableStateOf((initialPackage?.downloadKbps ?: 512).toString()) }
    var uploadKbpsText by remember { mutableStateOf((initialPackage?.uploadKbps ?: 64).toString()) }

    var afterQuotaDownText by remember { mutableStateOf((initialPackage?.afterQuotaDownloadKbps ?: 64).toString()) }
    var afterQuotaUpText by remember { mutableStateOf((initialPackage?.afterQuotaUploadKbps ?: 32).toString()) }
    var afterQuotaAction by remember { mutableStateOf(initialPackage?.afterQuotaAction ?: "throttle") }

    var freeDataEnabled by remember { mutableStateOf(initialPackage?.freeDataEnabled ?: false) }
    var renewalMode by remember { mutableStateOf(initialPackage?.renewalMode ?: "one_time") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .testTag("package_edit_dialog")
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Inventory2, contentDescription = null, tint = CyanNeon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (initialPackage == null) "Assign New Package" else "Edit Assigned Package",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Device MAC
                OutlinedTextField(
                    value = mac,
                    onValueChange = { mac = it.uppercase() },
                    label = { Text("Device MAC Address") },
                    placeholder = { Text("AA:BB:CC:DD:EE:FF") },
                    singleLine = true,
                    enabled = initialPackage == null,
                    modifier = Modifier
                        .testTag("package_mac_input")
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Guest Phone Number (Digits only)") },
                    placeholder = { Text("0771234567") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Allowance & Validity
                Text(
                    text = "DATA ALLOWANCES & VALIDITY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = downloadMibText,
                        onValueChange = { downloadMibText = it },
                        label = { Text("Download (MiB)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uploadMibText,
                        onValueChange = { uploadMibText = it },
                        label = { Text("Upload (MiB)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = validityDaysText,
                    onValueChange = { validityDaysText = it },
                    label = { Text("Validity (Days)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Normal Speeds
                Text(
                    text = "NORMAL PACKAGE SPEEDS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = downloadKbpsText,
                        onValueChange = { downloadKbpsText = it },
                        label = { Text("Down (kbps)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uploadKbpsText,
                        onValueChange = { uploadKbpsText = it },
                        label = { Text("Up (kbps)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // After-Quota Speeds & Action
                Text(
                    text = "AFTER-QUOTA EXHAUSTION POLICY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = afterQuotaAction == "throttle",
                        onClick = { afterQuotaAction = "throttle" },
                        label = { Text("Throttle (Keep Online)") }
                    )
                    FilterChip(
                        selected = afterQuotaAction == "disconnect",
                        onClick = { afterQuotaAction = "disconnect" },
                        label = { Text("Disconnect (Cutoff)") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = afterQuotaDownText,
                        onValueChange = { afterQuotaDownText = it },
                        label = { Text("After-Down (kbps)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = afterQuotaUpText,
                        onValueChange = { afterQuotaUpText = it },
                        label = { Text("After-Up (kbps)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Free Data Window & Renewal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Free Data Window", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Exclude traffic during shared free window hours", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = freeDataEnabled,
                        onCheckedChange = { freeDataEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = CyanNeon)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = renewalMode == "one_time",
                        onClick = { renewalMode = "one_time" },
                        label = { Text("One-Time Pass") }
                    )
                    FilterChip(
                        selected = renewalMode == "auto_renew",
                        onClick = { renewalMode = "auto_renew" },
                        label = { Text("Auto-Recurring") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
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
                            if (mac.isNotBlank()) {
                                onSave(
                                    GuestPackage(
                                        mac = mac,
                                        phone = phone,
                                        downloadLimitMib = downloadMibText,
                                        uploadLimitMib = uploadMibText,
                                        validityDays = validityDaysText.toIntOrNull() ?: 7,
                                        downloadKbps = downloadKbpsText.toIntOrNull() ?: 512,
                                        uploadKbps = uploadKbpsText.toIntOrNull() ?: 64,
                                        afterQuotaDownloadKbps = afterQuotaDownText.toIntOrNull() ?: 64,
                                        afterQuotaUploadKbps = afterQuotaUpText.toIntOrNull() ?: 32,
                                        afterQuotaAction = afterQuotaAction,
                                        freeDataEnabled = freeDataEnabled,
                                        renewalMode = renewalMode,
                                        status = initialPackage?.status ?: "active"
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier
                            .testTag("confirm_package_button")
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Package")
                    }
                }
            }
        }
    }
}
