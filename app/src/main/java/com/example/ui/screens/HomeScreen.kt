package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppIcons
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue

@Composable
fun HomeScreen(
    userName: String,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onShareApkClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Top row: device name button pill opening Settings (left) + Share App pill (right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val settingsLabel = stringResource(R.string.settings)
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline, CircleShape)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = settingsLabel,
                        onClick = onSettingsClick
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("settings_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.Devices,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = userName,
                    color = colors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("user_name_text")
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            val shareAppLabel = stringResource(R.string.share_app)
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.14f))
                    .border(1.dp, colors.primary.copy(alpha = 0.45f), CircleShape)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = shareAppLabel,
                        onClick = onShareApkClick
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("share_apk_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.Share,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = shareAppLabel,
                    color = colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val isWide = maxWidth >= 520.dp && maxWidth > maxHeight
            val circleSize = if (isWide) {
                minOf((maxWidth - 112.dp) / 2f, maxHeight - 48.dp).coerceIn(148.dp, 220.dp)
            } else {
                minOf((maxHeight - 88.dp) / 2f, maxWidth - 56.dp).coerceIn(148.dp, 212.dp)
            }

            val buttons: @Composable () -> Unit = {
                CircleActionButton(
                    title = "SEND",
                    icon = AppIcons.Send,
                    circleSize = circleSize,
                    iconOffsetX = 4.dp,
                    primaryColor = colors.primary,
                    secondaryColor = ElectricBlue,
                    surfaceColor = colors.surfaceVariant,
                    titleColor = colors.onSurface,
                    testTag = "send_button",
                    onClick = onSendClick
                )

                CircleActionButton(
                    title = "RECEIVE",
                    icon = AppIcons.FileDownload,
                    circleSize = circleSize,
                    iconOffsetX = 0.dp,
                    primaryColor = colors.secondary,
                    secondaryColor = DeepViolet,
                    surfaceColor = colors.surfaceVariant,
                    titleColor = colors.onSurface,
                    testTag = "receive_button",
                    onClick = onReceiveClick
                )
            }

            if (isWide) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    buttons()
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    buttons()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.developer_name),
                color = colors.onSurfaceVariant.copy(alpha = 0.82f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("developer_name_text")
            )
        }
    }
}

@Composable
private fun CircleActionButton(
    title: String,
    icon: ImageVector,
    circleSize: Dp,
    iconOffsetX: Dp,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    titleColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val titleFontSize = if (circleSize < 172.dp) 17.sp else 20.sp

    Box(
        modifier = Modifier
            .size(circleSize)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = primaryColor.copy(alpha = 0.28f),
                spotColor = primaryColor.copy(alpha = 0.45f)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.28f),
                        secondaryColor.copy(alpha = 0.14f),
                        surfaceColor
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.85f),
                        secondaryColor.copy(alpha = 0.45f),
                        primaryColor.copy(alpha = 0.30f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = primaryColor),
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Soft inner focal ring containing both the big icon and label
        Box(
            modifier = Modifier
                .size(circleSize * 0.84f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.16f),
                            secondaryColor.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = primaryColor.copy(alpha = 0.24f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier
                        .size(circleSize * 0.34f)
                        .offset(x = iconOffsetX)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = titleColor,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(primaryColor, secondaryColor)
                            )
                        )
                )
            }
        }
    }
}
