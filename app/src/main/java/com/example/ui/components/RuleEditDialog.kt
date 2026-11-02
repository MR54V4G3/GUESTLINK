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
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AutoApprovalRule
import com.example.data.model.RuleMatchType
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditDialog(
    initialRule: AutoApprovalRule?,
    onDismiss: () -> Unit,
    onSave: (AutoApprovalRule) -> Unit
) {
    var name by remember { mutableStateOf(initialRule?.name ?: "") }
    var matchType by remember { mutableStateOf(initialRule?.matchType ?: RuleMatchType.ALL_CLIENTS) }
    var matchPattern by remember { mutableStateOf(initialRule?.matchPattern ?: "") }
    var durationMinutesText by remember { mutableStateOf((initialRule?.grantDurationMinutes ?: 60).toString()) }
    var downLimitKbpsText by remember { mutableStateOf((initialRule?.downloadRateLimitKbps ?: 10000).toString()) }
    var upLimitKbpsText by remember { mutableStateOf((initialRule?.uploadRateLimitKbps ?: 3000).toString()) }
    var quotaMbText by remember { mutableStateOf((initialRule?.downloadQuotaMb ?: 1000).toString()) }
    var description by remember { mutableStateOf(initialRule?.description ?: "") }

    var expandedTypeDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .testTag("rule_edit_dialog")
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
                        Icon(imageVector = Icons.Filled.Rule, contentDescription = null, tint = CyanNeon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (initialRule == null) "New Approval Rule" else "Edit Approval Rule",
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

                // Rule Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule Name") },
                    singleLine = true,
                    modifier = Modifier
                        .testTag("rule_name_input")
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Match Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = matchType.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Client Match Condition") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        RuleMatchType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.replace("_", " ")) },
                                onClick = {
                                    matchType = type
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Match Pattern (hidden or shown based on type)
                if (matchType != RuleMatchType.ALL_CLIENTS) {
                    val labelText = when (matchType) {
                        RuleMatchType.MAC_PREFIX -> "MAC Prefix / OUI (e.g. F0:99:B6 or AA:BB:CC)"
                        RuleMatchType.HOSTNAME_CONTAINS -> "Hostname keywords (e.g. iPhone, Pixel, Galaxy)"
                        RuleMatchType.VENDOR_MATCH -> "Vendor string (e.g. Apple, Samsung, Intel)"
                        RuleMatchType.SCHEDULE_WINDOW -> "Active Time Window (HH:mm-HH:mm e.g. 08:00-20:00)"
                        else -> "Pattern"
                    }
                    OutlinedTextField(
                        value = matchPattern,
                        onValueChange = { matchPattern = it },
                        label = { Text(labelText) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Granted Duration (mins)
                OutlinedTextField(
                    value = durationMinutesText,
                    onValueChange = { durationMinutesText = it },
                    label = { Text("Granted Duration (Minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = downLimitKbpsText,
                        onValueChange = { downLimitKbpsText = it },
                        label = { Text("Down (Kbps)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = upLimitKbpsText,
                        onValueChange = { upLimitKbpsText = it },
                        label = { Text("Up (Kbps)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quotaMbText,
                    onValueChange = { quotaMbText = it },
                    label = { Text("Quota Limit (MB, 0 = Unlimited)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Policy Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
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
                            if (name.isNotBlank()) {
                                onSave(
                                    AutoApprovalRule(
                                        id = initialRule?.id ?: 0L,
                                        name = name,
                                        isEnabled = initialRule?.isEnabled ?: true,
                                        matchType = matchType,
                                        matchPattern = matchPattern,
                                        grantDurationMinutes = durationMinutesText.toIntOrNull() ?: 60,
                                        downloadRateLimitKbps = downLimitKbpsText.toIntOrNull() ?: 0,
                                        uploadRateLimitKbps = upLimitKbpsText.toIntOrNull() ?: 0,
                                        downloadQuotaMb = quotaMbText.toIntOrNull() ?: 0,
                                        description = description
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier
                            .testTag("save_rule_button")
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Rule")
                    }
                }
            }
        }
    }
}
