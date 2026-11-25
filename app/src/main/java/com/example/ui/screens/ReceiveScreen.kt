package com.example.ui.screens

import android.net.wifi.p2p.WifiP2pDevice
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.toValidDeviceName
import com.example.ui.components.AppIcons
import com.example.ui.components.AppTopBar
import com.example.ui.components.RadarAnimation

@Composable
fun ReceiveScreen(
    userName: String,
    thisDevice: WifiP2pDevice?,
    onBackClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val displayDeviceName = userName.toValidDeviceName()
        ?: thisDevice?.deviceName.toValidDeviceName()
        ?: "Receiver"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("receive_screen")
    ) {
        AppTopBar(
            title = "RECEIVE",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Device name pill at top center
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.outline, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("device_name_pill"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.Devices,
                        contentDescription = null,
                        tint = colors.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayDeviceName,
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Radar beacon
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarAnimation(
                    modifier = Modifier.fillMaxSize(),
                    radarColor = colors.secondary
                )
            }

            // Status label
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.secondary.copy(alpha = 0.14f))
                        .border(1.dp, colors.secondary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "READY TO RECEIVE",
                        color = colors.secondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
