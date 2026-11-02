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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.QuotaAccount
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun QuotaEditDialog(
    initialAccount: QuotaAccount?,
    onDismiss: () -> Unit,
    onSave: (mac: String, downMib: Int, upMib: Int) -> Unit
) {
    var mac by remember { mutableStateOf(initialAccount?.mac.orEmpty()) }
    var downMibText by remember { mutableStateOf(initialAccount?.downloadLimitMib?.toString() ?: "1024") }
    var upMibText by remember { mutableStateOf(initialAccount?.uploadLimitMib?.toString() ?: "256") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("quota_edit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (initialAccount == null) "Set New Quota Account" else "Update Quota Limits",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Configure whole-number limits from 1 to 1048576 MiB. Existing data usage will be preserved.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = mac,
                    onValueChange = { mac = it.uppercase() },
                    label = { Text("Device MAC Address") },
                    placeholder = { Text("AA:BB:CC:DD:EE:FF") },
                    enabled = initialAccount == null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = OutlineDark,
                        focusedLabelColor = CyanNeon,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quota_mac_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = downMibText,
                    onValueChange = { downMibText = it.filter { char -> char.isDigit() } },
                    label = { Text("Download Allowance (MiB)") },
                    supportingText = { Text("e.g. 1024 = 1 GB", color = TextTertiary, fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = OutlineDark,
                        focusedLabelColor = CyanNeon,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quota_download_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = upMibText,
                    onValueChange = { upMibText = it.filter { char -> char.isDigit() } },
                    label = { Text("Upload Allowance (MiB)") },
                    supportingText = { Text("e.g. 256 = 256 MB", color = TextTertiary, fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = OutlineDark,
                        focusedLabelColor = CyanNeon,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quota_upload_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("quota_cancel_btn")
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val cleanMac = mac.trim()
                            val down = downMibText.toIntOrNull() ?: 0
                            val up = upMibText.toIntOrNull() ?: 0

                            if (!cleanMac.matches(Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"))) {
                                errorMessage = "Please enter a valid MAC address (XX:XX:XX:XX:XX:XX)"
                                return@Button
                            }
                            if (down !in 1..1048576 || up !in 1..1048576) {
                                errorMessage = "Limits must be between 1 and 1,048,576 MiB"
                                return@Button
                            }

                            onSave(cleanMac, down, up)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                        modifier = Modifier.testTag("quota_save_btn")
                    ) {
                        Text("Save Quota Limits", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
