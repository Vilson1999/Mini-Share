package com.example

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RadarScreen
import com.example.ui.screens.ReceiveScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.theme.AppTheme
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val incomingSharedUris = MutableStateFlow<List<Uri>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (savedInstanceState == null) {
            handleShareIntent(intent)
        }

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val pendingSharedUris by incomingSharedUris.collectAsStateWithLifecycle()

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
                onDispose {}
            }

            AppTheme(darkTheme = darkTheme) {
                MainAppContent(
                    viewModel = viewModel,
                    incomingSharedUris = pendingSharedUris,
                    onConsumeSharedUris = { incomingSharedUris.value = emptyList() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        val uris = extractSharedUris(intent)
        if (uris.isNotEmpty()) {
            incomingSharedUris.value = uris
            intent?.action = Intent.ACTION_MAIN
        }
    }
}

private enum class RequiredSetting {
    WIFI,
    BLUETOOTH,
    LOCATION
}

private fun getMissingRequiredSetting(context: Context): RequiredSetting? {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    val isWifiOn = wifiManager?.isWifiEnabled ?: true
    if (!isWifiOn) {
        return RequiredSetting.WIFI
    }

    val btManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val btAdapter = btManager?.adapter
    if (btAdapter != null && !btAdapter.isEnabled) {
        return RequiredSetting.BLUETOOTH
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        val locationManager = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isLocationOn = locationManager?.let { LocationManagerCompat.isLocationEnabled(it) } ?: true
        if (!isLocationOn) {
            return RequiredSetting.LOCATION
        }
    }

    return null
}

private fun createSettingIntent(context: Context, setting: RequiredSetting): Intent {
    return when (setting) {
        RequiredSetting.WIFI -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
                if (panelIntent.resolveActivity(context.packageManager) != null) {
                    panelIntent
                } else {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                }
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
        }
        RequiredSetting.BLUETOOTH -> {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (enableIntent.resolveActivity(context.packageManager) != null) {
                enableIntent
            } else {
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            }
        }
        RequiredSetting.LOCATION -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }
}

private fun extractSharedUris(intent: Intent?): List<Uri> {
    if (intent == null) return emptyList()
    if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_SEND_MULTIPLE) {
        return emptyList()
    }
    val result = LinkedHashSet<Uri>()

    when (intent.action) {
        Intent.ACTION_SEND -> {
            IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)?.let {
                result.add(it)
            }
        }
        Intent.ACTION_SEND_MULTIPLE -> {
            IntentCompat.getParcelableArrayListExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)?.let {
                result.addAll(it)
            }
        }
    }

    intent.clipData?.let { clipData ->
        for (i in 0 until clipData.itemCount) {
            clipData.getItemAt(i).uri?.let { result.add(it) }
        }
    }

    return result.toList()
}

private suspend fun buildShareApkChooserIntent(context: Context): Intent = withContext(Dispatchers.IO) {
    val sourceApk = File(context.applicationInfo.sourceDir)
    val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
    val targetApk = File(shareDir, "Mini_Share.apk")
    if (!targetApk.exists() ||
        targetApk.length() != sourceApk.length() ||
        targetApk.lastModified() < sourceApk.lastModified()
    ) {
        sourceApk.inputStream().use { input ->
            targetApk.outputStream().use { output ->
                input.copyTo(output, bufferSize = 64 * 1024)
            }
        }
    }

    val apkUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        targetApk
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.android.package-archive"
        putExtra(Intent.EXTRA_STREAM, apkUri)
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        clipData = ClipData.newRawUri(null, apkUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    Intent.createChooser(shareIntent, context.getString(R.string.share_app)).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

internal fun getRequiredPermissions(): Array<String> {
    return when {
        // Android 13 - 17 (API 33+): Wi-Fi Nearby + Bluetooth Nearby runtime permissions
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        // Android 12 / 12L (API 31-32): Bluetooth Nearby + Location for Wi-Fi P2P
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // Android 10 & 11 (API 29-30): Location runtime permission (BLUETOOTH is install-time, Scoped Storage for Downloads)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // Android 9 (API 28): Location + WRITE_EXTERNAL_STORAGE for Downloads
        else -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}

private fun hasAllPermissions(context: Context): Boolean {
    val perms = getRequiredPermissions()
    return perms.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    incomingSharedUris: List<Uri> = emptyList(),
    onConsumeSharedUris: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val peers by viewModel.peers.collectAsStateWithLifecycle()
    val isScanning by viewModel.isDiscovering.collectAsStateWithLifecycle()
    val selectedFiles by viewModel.selectedFiles.collectAsStateWithLifecycle()
    val transferFiles by viewModel.transferFiles.collectAsStateWithLifecycle()
    val transferStats by viewModel.transferStats.collectAsStateWithLifecycle()
    val transferDirection by viewModel.transferDirection.collectAsStateWithLifecycle()
    val isTransferActive by viewModel.isTransferActive.collectAsStateWithLifecycle()
    val isTransferComplete by viewModel.isTransferComplete.collectAsStateWithLifecycle()
    val transferError by viewModel.transferError.collectAsStateWithLifecycle()
    val thisDevice by viewModel.thisDevice.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val storageTreeUri by viewModel.storageTreeUri.collectAsStateWithLifecycle()
    val storageDisplayName by viewModel.storageDisplayName.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val connectingDeviceAddress by viewModel.connectingDeviceAddress.collectAsStateWithLifecycle()

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var awaitingExternalModal by remember { mutableStateOf(false) }
    var isSharingApk by remember { mutableStateOf(false) }
    var missingSetting by remember { mutableStateOf<RequiredSetting?>(null) }

    val proceedWithChecks: (() -> Unit) -> Unit = remember(context) {
        { action ->
            val missing = getMissingRequiredSetting(context)
            if (missing == null) {
                missingSetting = null
                pendingAction = null
                action()
            } else {
                pendingAction = action
                missingSetting = missing
            }
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        awaitingExternalModal = false
        if (uris.isNotEmpty()) {
            viewModel.onFilesPicked(context, uris)
        }
    }

    // System APK share launcher
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        awaitingExternalModal = false
    }

    // Receive storage folder picker launcher (SAF)
    val storagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        awaitingExternalModal = false
        if (treeUri != null) {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, flags)
                viewModel.setStorageTreeUri(treeUri.toString())
            } catch (_: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Unable to use selected folder")
                }
            }
        }
    }

    // Settings launcher
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        awaitingExternalModal = false
        pendingAction?.let(proceedWithChecks)
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        awaitingExternalModal = false
        val allGranted = grants.values.all { it }
        val action = pendingAction
        if (allGranted && action != null) {
            proceedWithChecks(action)
        } else {
            pendingAction = null
            scope.launch {
                snackbarHostState.showSnackbar("Permissions are required for file sharing")
            }
        }
    }

    val requestActionWithPermissionsAndSettings: (() -> Unit) -> Unit = { action ->
        if (hasAllPermissions(context)) {
            proceedWithChecks(action)
        } else {
            pendingAction = action
            awaitingExternalModal = true
            permissionLauncher.launch(getRequiredPermissions())
        }
    }

    // Direct share when launched from Android Share menu
    LaunchedEffect(incomingSharedUris) {
        if (incomingSharedUris.isNotEmpty()) {
            val urisToShare = incomingSharedUris.toList()
            onConsumeSharedUris()
            requestActionWithPermissionsAndSettings {
                viewModel.onFilesPicked(context, urisToShare)
            }
        }
    }

    // Never run discovery, transfers, or broadcast receivers in background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.onAppForegrounded()
                }
                Lifecycle.Event.ON_RESUME -> {
                    awaitingExternalModal = false
                    val action = pendingAction
                    if (action != null && missingSetting != null) {
                        proceedWithChecks(action)
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    val activity = context as? Activity
                    val isConfigChange = activity?.isChangingConfigurations == true
                    if (!isConfigChange && !awaitingExternalModal) {
                        pendingAction = null
                        missingSetting = null
                        viewModel.onAppBackgrounded()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Show error toast/snackbar if transfer encounters an error
    LaunchedEffect(transferError) {
        transferError?.let { err ->
            if (err.isNotBlank() && !err.contains("closed", ignoreCase = true)) {
                snackbarHostState.showSnackbar(err)
            }
        }
    }

    missingSetting?.let { setting ->
        val colors = MaterialTheme.colorScheme
        AlertDialog(
            onDismissRequest = {
                missingSetting = null
                pendingAction = null
            },
            title = {
                Text(
                    text = when (setting) {
                        RequiredSetting.WIFI -> "TURN ON WI-FI"
                        RequiredSetting.BLUETOOTH -> "TURN ON BLUETOOTH"
                        RequiredSetting.LOCATION -> "TURN ON LOCATION"
                    },
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (setting) {
                        RequiredSetting.WIFI -> "Wi-Fi is off."
                        RequiredSetting.BLUETOOTH -> "Bluetooth is off."
                        RequiredSetting.LOCATION -> "Location is off."
                    },
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        awaitingExternalModal = true
                        settingsLauncher.launch(createSettingIntent(context, setting))
                    },
                    modifier = Modifier.testTag("open_setting_button")
                ) {
                    Text("OPEN SETTINGS", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        missingSetting = null
                        pendingAction = null
                    },
                    modifier = Modifier.testTag("cancel_setting_button")
                ) {
                    Text("CANCEL", color = colors.onSurfaceVariant)
                }
            },
            containerColor = colors.surfaceVariant
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        userName = userName,
                        onSendClick = {
                            requestActionWithPermissionsAndSettings {
                                awaitingExternalModal = true
                                filePickerLauncher.launch(arrayOf("*/*"))
                            }
                        },
                        onReceiveClick = {
                            requestActionWithPermissionsAndSettings {
                                viewModel.onReceiveClicked()
                            }
                        },
                        onShareApkClick = {
                            if (!isSharingApk) {
                                isSharingApk = true
                                scope.launch {
                                    try {
                                        val chooser = buildShareApkChooserIntent(context)
                                        awaitingExternalModal = true
                                        shareLauncher.launch(chooser)
                                    } catch (_: Exception) {
                                        awaitingExternalModal = false
                                        snackbarHostState.showSnackbar("Unable to share app APK")
                                    } finally {
                                        isSharingApk = false
                                    }
                                }
                            }
                        },
                        onSettingsClick = {
                            viewModel.openSettings()
                        }
                    )
                }

                AppScreen.RADAR -> {
                    BackHandler {
                        viewModel.resetToHome()
                    }
                    RadarScreen(
                        peers = peers,
                        selectedFiles = selectedFiles,
                        isScanning = isScanning,
                        connectingDeviceAddress = connectingDeviceAddress,
                        onBackClick = { viewModel.resetToHome() },
                        onDeviceClick = { device -> viewModel.connectAndSend(device) }
                    )
                }

                AppScreen.RECEIVE -> {
                    BackHandler {
                        viewModel.resetToHome()
                    }
                    ReceiveScreen(
                        userName = userName,
                        thisDevice = thisDevice,
                        onBackClick = { viewModel.resetToHome() }
                    )
                }

                AppScreen.TRANSFER -> {
                    BackHandler {
                        if (isTransferComplete || !isTransferActive) {
                            viewModel.resetToHome()
                        } else {
                            viewModel.cancelAllTransfers()
                        }
                    }
                    TransferScreen(
                        files = transferFiles,
                        stats = transferStats,
                        direction = transferDirection,
                        isComplete = isTransferComplete,
                        onCancelFile = { fileId -> viewModel.cancelTransferringFile(fileId) },
                        onCancelAll = { viewModel.resetToHome() },
                        onDone = { viewModel.resetToHome() }
                    )
                }

                AppScreen.SETTINGS -> {
                    BackHandler {
                        viewModel.resetToHome()
                    }
                    SettingsScreen(
                        storageDisplayName = storageDisplayName,
                        hasCustomStorage = storageTreeUri != null,
                        onSelectStorageClick = {
                            awaitingExternalModal = true
                            storagePickerLauncher.launch(null)
                        },
                        onResetStorageClick = {
                            viewModel.setStorageTreeUri(null)
                        },
                        onBackClick = {
                            viewModel.resetToHome()
                        }
                    )
                }
            }
        }
    }
}
