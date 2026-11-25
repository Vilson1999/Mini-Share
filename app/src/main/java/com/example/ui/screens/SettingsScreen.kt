package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.ui.components.CircleIconBadge
import com.example.ui.components.MiniShareIcon

@Composable
fun SettingsScreen(
    storageDisplayName: String,
    hasCustomStorage: Boolean,
    runInBackground: Boolean,
    onToggleRunInBackground: (Boolean) -> Unit,
    onSelectStorageClick: () -> Unit,
    onResetStorageClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val settingsLabel = stringResource(R.string.settings)

    var storageExpanded by rememberSaveable { mutableStateOf(true) }
    var backgroundExpanded by rememberSaveable { mutableStateOf(true) }
    var aboutExpanded by rememberSaveable { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Storage Folder
            SettingsFolder(
                title = stringResource(R.string.folder_storage),
                subtitle = if (storageExpanded) {
                    stringResource(R.string.select_storage_subtitle)
                } else {
                    storageDisplayName
                },
                expanded = storageExpanded,
                onToggleExpand = { storageExpanded = !storageExpanded },
                testTag = "storage_folder_header",
                leadingBadge = {
                    CircleIconBadge(
                        icon = AppIcons.Folder,
                        accentColor = colors.primary
                    )
                }
            ) {
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

            // Background Folder (directly below Storage Location)
            SettingsFolder(
                title = stringResource(R.string.folder_background),
                subtitle = stringResource(
                    if (runInBackground) R.string.folder_background_on_short
                    else R.string.folder_background_off_short
                ),
                expanded = backgroundExpanded,
                onToggleExpand = { backgroundExpanded = !backgroundExpanded },
                testTag = "background_folder_header",
                leadingBadge = {
                    CircleIconBadge(
                        icon = AppIcons.BackgroundSync,
                        accentColor = colors.primary,
                        isActive = runInBackground
                    )
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.background)
                        .border(1.dp, colors.outline, RoundedCornerShape(14.dp))
                        .toggleable(
                            value = runInBackground,
                            role = Role.Switch,
                            onValueChange = onToggleRunInBackground
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("run_in_background_toggle"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.run_in_background),
                            color = colors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                if (runInBackground) R.string.run_in_background_on
                                else R.string.run_in_background_off
                            ),
                            color = if (runInBackground) colors.primary else colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = runInBackground,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onPrimary,
                            checkedTrackColor = colors.primary,
                            uncheckedThumbColor = colors.onSurfaceVariant,
                            uncheckedTrackColor = colors.surfaceVariant,
                            uncheckedBorderColor = colors.outline
                        ),
                        modifier = Modifier.testTag("run_in_background_switch")
                    )
                }
            }

            // App Info Folder (small app details inside folder)
            SettingsFolder(
                title = stringResource(R.string.folder_about),
                subtitle = stringResource(R.string.app_name),
                expanded = aboutExpanded,
                onToggleExpand = { aboutExpanded = !aboutExpanded },
                testTag = "about_folder_header",
                leadingBadge = {
                    MiniShareIcon(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(11.dp))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.background)
                        .border(1.dp, colors.outline, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_mode_label),
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.developer_name),
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsFolder(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    testTag: String,
    leadingBadge: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "folder_chevron"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.outline, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable(
                    role = Role.Button,
                    onClickLabel = title,
                    onClick = onToggleExpand
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingBadge()
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = AppIcons.ExpandMore,
                contentDescription = title,
                tint = colors.onSurfaceVariant,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(chevronRotation)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 18.dp)
            ) {
                content()
            }
        }
    }
}
