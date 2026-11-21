package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppIcons
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue

@Composable
fun HomeScreen(
    userName: String,
    onUserNameChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember(userName) { mutableStateOf(userName) }

    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Top bar with editable device name centered at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline, CircleShape)
                    .clickable(role = Role.Button) {
                        tempName = userName
                        isEditingName = true
                    }
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .testTag("user_name_chip"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AppIcons.Person,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = userName,
                        color = colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = AppIcons.Edit,
                        contentDescription = "Edit Name",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (isEditingName) {
            AlertDialog(
                onDismissRequest = { isEditingName = false },
                title = {
                    Text(
                        text = "EDIT NAME",
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            cursorColor = colors.primary
                        ),
                        modifier = Modifier.testTag("edit_name_input")
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onUserNameChange(tempName)
                            isEditingName = false
                        },
                        modifier = Modifier.testTag("save_name_button")
                    ) {
                        Text("SAVE", color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isEditingName = false }) {
                        Text("CANCEL", color = colors.onSurfaceVariant)
                    }
                },
                containerColor = colors.surfaceVariant
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val isWide = maxWidth >= 520.dp && maxWidth > maxHeight

            if (isWide) {
                val circleSize = minOf(
                    (maxWidth - 128.dp) / 2f,
                    maxHeight - 88.dp
                ).coerceIn(132.dp, 200.dp)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
            } else {
                val circleSize = minOf(
                    (maxHeight - 156.dp) / 2f,
                    maxWidth - 72.dp
                ).coerceIn(132.dp, 188.dp)

                Column(
                    verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
            }
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick
            )
            .padding(4.dp)
            .testTag(testTag)
    ) {
        // Main circular icon orb
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
                            primaryColor.copy(alpha = 0.26f),
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
                .indication(
                    interactionSource = interactionSource,
                    indication = ripple(color = primaryColor)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Soft inner focal ring around the big icon
            Box(
                modifier = Modifier
                    .size(circleSize * 0.68f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.22f),
                                secondaryColor.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = primaryColor.copy(alpha = 0.28f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = primaryColor,
                    modifier = Modifier
                        .size(circleSize * 0.42f)
                        .offset(x = iconOffsetX)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.5.sp,
            color = titleColor
        )

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .width(28.dp)
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
