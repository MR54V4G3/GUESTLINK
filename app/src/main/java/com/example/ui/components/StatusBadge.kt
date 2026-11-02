package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientState
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPending
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.RedBlocked
import com.example.ui.theme.RedLight

@Composable
fun StatusBadge(
    state: ClientState,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, label, icon) = when (state) {
        ClientState.PREAUTHENTICATED -> StatusStyle(
            bgColor = AmberPending.copy(alpha = 0.15f),
            borderColor = AmberPending.copy(alpha = 0.5f),
            textColor = AmberLight,
            label = "Pending Approval",
            icon = Icons.Filled.HourglassTop
        )
        ClientState.AUTHENTICATED -> StatusStyle(
            bgColor = EmeraldActive.copy(alpha = 0.15f),
            borderColor = EmeraldActive.copy(alpha = 0.5f),
            textColor = EmeraldLight,
            label = "Active Session",
            icon = Icons.Filled.CheckCircle
        )
        ClientState.BLOCKED -> StatusStyle(
            bgColor = RedBlocked.copy(alpha = 0.15f),
            borderColor = RedBlocked.copy(alpha = 0.5f),
            textColor = RedLight,
            label = "Blocked",
            icon = Icons.Filled.Block
        )
        ClientState.TRUSTED -> StatusStyle(
            bgColor = Color(0xFF6366F1).copy(alpha = 0.15f),
            borderColor = Color(0xFF6366F1).copy(alpha = 0.5f),
            textColor = Color(0xFFA5B4FC),
            label = "Trusted Bypass",
            icon = Icons.Filled.LockOpen
        )
        ClientState.DEAUTHENTICATED -> StatusStyle(
            bgColor = Color(0xFF64748B).copy(alpha = 0.15f),
            borderColor = Color(0xFF64748B).copy(alpha = 0.4f),
            textColor = Color(0xFFCBD5E1),
            label = "Expired",
            icon = Icons.Filled.TimerOff
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(100.dp))
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

private data class StatusStyle(
    val bgColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
