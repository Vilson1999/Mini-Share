package com.example.ui.screens

import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val settingsLabel = stringResource(R.string.settings)
    val developerName = stringResource(R.string.developer_name)
    val appVersion = remember(context) {
        try {
            val pm = context.packageManager
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, 0)
            }
            info.versionName ?: "1.1"
        } catch (_: Exception) {
            "1.1"
        }
    }

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
            // 1. Run in Background Toggle (Top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline, RoundedCornerShape(20.dp))
                    .toggleable(
                        value = runInBackground,
                        role = Role.Switch,
                        onValueChange = onToggleRunInBackground
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .testTag("run_in_background_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconBadge(
                        icon = AppIcons.BackgroundSync,
                        accentColor = colors.primary,
                        isActive = runInBackground
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.run_in_background),
                            color = colors.onSurface,
                            fontSize = 15.sp,
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
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = runInBackground,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.onPrimary,
                        checkedTrackColor = colors.primary,
                        uncheckedThumbColor = colors.onSurfaceVariant,
                        uncheckedTrackColor = colors.background,
                        uncheckedBorderColor = colors.outline
                    ),
                    modifier = Modifier.testTag("run_in_background_switch")
                )
            }

            // 2. Select Storage
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircleIconBadge(
                        icon = AppIcons.Folder,
                        accentColor = colors.primary
                    )
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

            // 3. App Info (Bottom Last: App Icon, App Name, App Version, Developer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline, RoundedCornerShape(20.dp))
                    .padding(18.dp)
                    .testTag("app_info_section"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniShareIcon(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.app_version_format, appVersion),
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.developer_by_format, developerName),
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
