package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppIcons
import com.example.ui.components.AppTopBar

@Composable
fun SettingsScreen(
    storageDisplayName: String,
    hasCustomStorage: Boolean,
    onSelectStorageClick: () -> Unit,
    onResetStorageClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val settingsLabel = stringResource(R.string.settings)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("settings_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppTopBar(
            title = settingsLabel.uppercase(),
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Select Storage
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.14f))
                            .border(1.dp, colors.primary.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.Folder,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.select_storage),
                            color = colors.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.select_storage_subtitle),
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.background)
                        .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("storage_location_text")
                ) {
                    Text(
                        text = storageDisplayName,
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val chooseLabel = stringResource(R.string.choose_folder)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = chooseLabel,
                                onClick = onSelectStorageClick
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("choose_storage_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chooseLabel,
                            color = colors.onPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    if (hasCustomStorage) {
                        val defaultLabel = stringResource(R.string.reset_default)
                        Box(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(CircleShape)
                                .background(colors.background)
                                .border(1.dp, colors.outline, CircleShape)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = defaultLabel,
                                    onClick = onResetStorageClick
                                )
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                                .testTag("reset_storage_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = defaultLabel,
                                color = colors.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
