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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppThemeMode
import com.example.ui.components.AppIcons
import com.example.ui.components.AppTopBar

@Composable
fun SettingsScreen(
    userName: String,
    hasCustomUserName: Boolean,
    storageDisplayName: String,
    hasCustomStorage: Boolean,
    selectedTheme: AppThemeMode,
    onSaveUserName: (String) -> Unit,
    onResetUserNameClick: () -> Unit,
    onSelectStorageClick: () -> Unit,
    onResetStorageClick: () -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    onBackClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val settingsLabel = stringResource(R.string.settings)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var draftName by remember(userName) { mutableStateOf(userName) }
    val trimmedDraft = draftName.trim()
    val isValidName = trimmedDraft.isNotEmpty() && !trimmedDraft.equals("unknown", ignoreCase = true)

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Edit User Name
            SettingsSectionCard(
                icon = AppIcons.Person,
                title = stringResource(R.string.user_name_title),
                subtitle = stringResource(R.string.user_name_subtitle)
            ) {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { updated ->
                        if (updated.length <= 32) {
                            draftName = updated
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_name_input"),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.user_name_placeholder),
                            color = colors.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        cursorColor = colors.primary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValidName) {
                                onSaveUserName(trimmedDraft)
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val saveLabel = stringResource(R.string.save)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(CircleShape)
                            .background(if (isValidName) colors.primary else colors.outline)
                            .clickable(
                                enabled = isValidName,
                                role = Role.Button,
                                onClickLabel = saveLabel,
                                onClick = {
                                    onSaveUserName(trimmedDraft)
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("save_user_name_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = saveLabel,
                            color = if (isValidName) colors.onPrimary else colors.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    if (hasCustomUserName) {
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
                                    onClick = {
                                        onResetUserNameClick()
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                )
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                                .testTag("reset_user_name_button"),
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

            // 2. Select Storage
            SettingsSectionCard(
                icon = AppIcons.Folder,
                title = stringResource(R.string.select_storage),
                subtitle = stringResource(R.string.select_storage_subtitle)
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

            // 2. Theme (Default, Light, Dark)
            SettingsSectionCard(
                icon = AppIcons.Contrast,
                title = stringResource(R.string.theme_title),
                subtitle = stringResource(R.string.theme_subtitle)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = mode == selectedTheme
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) colors.primary.copy(alpha = 0.16f)
                                    else colors.background
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) colors.primary else colors.outline,
                                    shape = CircleShape
                                )
                                .clickable(
                                    role = Role.RadioButton,
                                    onClickLabel = mode.label,
                                    onClick = { onThemeSelected(mode) }
                                )
                                .padding(horizontal = 10.dp, vertical = 12.dp)
                                .testTag("theme_option_${mode.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = colors.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}
