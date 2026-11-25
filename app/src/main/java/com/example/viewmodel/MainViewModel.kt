package com.example.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppScreen
import com.example.model.FileItem
import com.example.model.TransferDirection
import com.example.model.TransferStats
import com.example.model.TransferStatus
import com.example.p2p.BluetoothDiscoveryHelper
import com.example.p2p.WifiDirectManager
import com.example.transfer.FileTransferEngine
import com.example.transfer.TransferForegroundService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("mini_share_prefs", Context.MODE_PRIVATE)
    private val wifiDirectManager = WifiDirectManager(application)
    private val bluetoothDiscoveryHelper = BluetoothDiscoveryHelper(application)
    private val transferEngine = FileTransferEngine(application)

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val selectedFiles: StateFlow<List<FileItem>> = _selectedFiles.asStateFlow()

    private val _transferFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val transferFiles: StateFlow<List<FileItem>> = _transferFiles.asStateFlow()

    private val _transferStats = MutableStateFlow(TransferStats())
    val transferStats: StateFlow<TransferStats> = _transferStats.asStateFlow()

    private val _transferDirection = MutableStateFlow(TransferDirection.SEND)
    val transferDirection: StateFlow<TransferDirection> = _transferDirection.asStateFlow()

    private val _isTransferActive = MutableStateFlow(false)
    val isTransferActive: StateFlow<Boolean> = _isTransferActive.asStateFlow()

    private val _isTransferComplete = MutableStateFlow(false)
    val isTransferComplete: StateFlow<Boolean> = _isTransferComplete.asStateFlow()

    private val _transferError = MutableStateFlow<String?>(null)
    val transferError: StateFlow<String?> = _transferError.asStateFlow()

    private val _connectingDeviceAddress = MutableStateFlow<String?>(null)
    val connectingDeviceAddress: StateFlow<String?> = _connectingDeviceAddress.asStateFlow()

    private var transferJob: Job? = null
    private var discoveryJob: Job? = null
    private var connectTimeoutJob: Job? = null
    @Volatile
    private var isAppInBackground = false

    val peers = wifiDirectManager.peers
    val isDiscovering = wifiDirectManager.isDiscovering
    val thisDevice = wifiDirectManager.thisDevice

    private val _userName = MutableStateFlow(resolveDefaultDeviceName(application))
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _storageTreeUri = MutableStateFlow(prefs.getString("storage_tree_uri", null))
    val storageTreeUri: StateFlow<String?> = _storageTreeUri.asStateFlow()

    private val _storageDisplayName = MutableStateFlow(formatTreeUriDisplayName(_storageTreeUri.value))
    val storageDisplayName: StateFlow<String> = _storageDisplayName.asStateFlow()

    private val _runInBackground = MutableStateFlow(prefs.getBoolean(KEY_RUN_IN_BACKGROUND, false))
    val runInBackground: StateFlow<Boolean> = _runInBackground.asStateFlow()

    private fun isValidDisplayName(name: String?): Boolean {
        val trimmed = name?.trim() ?: return false
        return trimmed.isNotEmpty() && !trimmed.equals("unknown", ignoreCase = true)
    }

    private fun resolveDefaultDeviceName(context: Context): String {
        val systemDeviceName = try {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        } catch (_: Exception) {
            null
        }
        return listOf(
            systemDeviceName,
            wifiDirectManager.thisDevice.value?.deviceName,
            android.os.Build.MODEL
        ).firstOrNull { isValidDisplayName(it) }?.trim()?.take(MAX_USER_NAME_LENGTH)
            ?: DEFAULT_USER_NAME
    }

    private fun formatTreeUriDisplayName(uriString: String?): String {
        if (uriString.isNullOrBlank()) return "Downloads / Mini Share"
        return try {
            val docId = DocumentsContract.getTreeDocumentId(Uri.parse(uriString))
            val parts = docId.split(":", limit = 2)
            val rootLabel = if (parts[0].equals("primary", ignoreCase = true)) "Internal Storage" else parts[0]
            val subPath = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
            if (subPath != null) "$rootLabel / $subPath" else rootLabel
        } catch (_: Exception) {
            "Custom Folder"
        }
    }

    fun setStorageTreeUri(uriString: String?) {
        _storageTreeUri.value = uriString
        _storageDisplayName.value = formatTreeUriDisplayName(uriString)
        prefs.edit().apply {
            if (uriString == null) remove("storage_tree_uri") else putString("storage_tree_uri", uriString)
        }.apply()
    }

    fun openSettings() {
        _currentScreen.value = AppScreen.SETTINGS
    }

    init {
        viewModelScope.launch {
            wifiDirectManager.thisDevice.collect { device ->
                val p2pName = device?.deviceName
                if (_userName.value == DEFAULT_USER_NAME && isValidDisplayName(p2pName)) {
                    _userName.value = p2pName!!.trim().take(MAX_USER_NAME_LENGTH)
                }
            }
        }
        viewModelScope.launch {
            wifiDirectManager.connectionInfo.collect { info ->
                if (info != null && info.groupFormed) {
                    val host = info.groupOwnerAddress?.hostAddress ?: "192.168.49.1"
                    val isGo = info.isGroupOwner
                    when (_currentScreen.value) {
                        AppScreen.RADAR -> {
                            if (_selectedFiles.value.isNotEmpty() && !_isTransferActive.value) {
                                connectTimeoutJob?.cancel()
                                stopDiscoveryLoop()
                                wifiDirectManager.stopDiscovery()
                                bluetoothDiscoveryHelper.stopAll()
                                _connectingDeviceAddress.value = null
                                startSendingFiles(isGroupOwner = isGo, hostAddress = host)
                            }
                        }
                        AppScreen.RECEIVE -> {
                            if (!_isTransferActive.value) {
                                stopDiscoveryLoop()
                                wifiDirectManager.stopDiscovery()
                                wifiDirectManager.stopReceiveListening()
                                bluetoothDiscoveryHelper.stopAll()
                                startReceivingFiles(isGroupOwner = isGo, hostAddress = host)
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
        viewModelScope.launch {
            TransferForegroundService.cancelRequests.collect {
                cancelAllTransfers()
                if (isAppInBackground && !_runInBackground.value) {
                    cleanupIdleBackgroundSession()
                }
            }
        }
        viewModelScope.launch {
            TransferForegroundService.turnOffRequests.collect {
                setRunInBackground(false)
            }
        }
        if (_runInBackground.value) {
            TransferForegroundService.startBackgroundIdle(getApplication())
        }
    }

    fun setRunInBackground(enabled: Boolean) {
        if (_runInBackground.value == enabled) return
        _runInBackground.value = enabled
        prefs.edit().putBoolean(KEY_RUN_IN_BACKGROUND, enabled).apply()
        if (enabled) {
            if (_isTransferActive.value) {
                TransferForegroundService.startTransfer(
                    context = getApplication(),
                    direction = _transferDirection.value,
                    totalCount = _transferFiles.value.size
                )
            } else {
                TransferForegroundService.startBackgroundIdle(getApplication())
            }
        } else {
            TransferForegroundService.stop(getApplication())
            if (isAppInBackground) {
                resetToHome()
                wifiDirectManager.unregister()
            }
        }
    }

    companion object {
        private const val DEFAULT_USER_NAME = "Mini Share User"
        private const val MAX_USER_NAME_LENGTH = 32
        private const val KEY_RUN_IN_BACKGROUND = "run_in_background"
    }

    fun onFilesPicked(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return

        val items = uris.mapNotNull { uri ->
            extractFileItem(context, uri)
        }

        if (items.isNotEmpty()) {
            connectTimeoutJob?.cancel()
            stopDiscoveryLoop()
            cancelAllTransfers()
            _connectingDeviceAddress.value = null
            _transferError.value = null
            _selectedFiles.value = items
            _transferFiles.value = items
            _transferDirection.value = TransferDirection.SEND
            _currentScreen.value = AppScreen.RADAR
            wifiDirectManager.prepareCleanSession {
                if (_currentScreen.value == AppScreen.RADAR) {
                    startDiscoveryLoop()
                }
            }
        }
    }

    private fun extractFileItem(context: Context, uri: Uri): FileItem? {
        var name = "file_${System.currentTimeMillis()}"
        var size = 0L
        val mime = context.contentResolver.getType(uri) ?: "*/*"

        if (uri.scheme == ContentResolver.SCHEME_FILE) {
            uri.path?.let { path ->
                val file = File(path)
                if (file.name.isNotBlank()) name = file.name
                if (file.exists()) size = file.length()
            }
        } else {
            try {
                val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) {
                            name = it.getString(nameIndex) ?: name
                        }
                        if (sizeIndex != -1) {
                            size = it.getLong(sizeIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error query URI", e)
            }
        }

        if (size <= 0) {
            try {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    if (afd.length > 0) size = afd.length
                }
            } catch (_: Exception) {}
        }
        if (size <= 0) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    size = stream.available().toLong()
                }
            } catch (_: Exception) {}
        }

        return FileItem(
            id = UUID.randomUUID().toString(),
            name = name,
            sizeBytes = size,
            uriString = uri.toString(),
            mimeType = mime,
            status = TransferStatus.PENDING
        )
    }

    private fun startDiscoveryLoop() {
        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            var firstRun = true
            var lastPeerDiscoveryAt = 0L
            var lastServiceDiscoveryAt = 0L
            var serviceWindowUntil = 0L
            while (isActive && (_currentScreen.value == AppScreen.RADAR || _currentScreen.value == AppScreen.RECEIVE)) {
                val isReceive = _currentScreen.value == AppScreen.RECEIVE
                val isRadar = _currentScreen.value == AppScreen.RADAR
                val now = System.currentTimeMillis()

                if (isReceive && firstRun) {
                    firstRun = false
                    lastPeerDiscoveryAt = now
                    bluetoothDiscoveryHelper.startAdvertising()
                    wifiDirectManager.registerLocalReceiveService(_userName.value) {
                        wifiDirectManager.startReceiveListening(
                            onFailure = { reason ->
                                Log.e("MainViewModel", "Receive listen failed: $reason")
                                if (reason == WifiP2pManager.P2P_UNSUPPORTED) {
                                    _transferError.value = "Wi-Fi Direct not supported"
                                }
                            }
                        )
                    }
                } else if (isRadar && _connectingDeviceAddress.value == null && !_isTransferActive.value) {
                    if (firstRun) {
                        bluetoothDiscoveryHelper.startScanning {
                            wifiDirectManager.requestPeers()
                        }
                    }
                    val needsPeerRefresh = firstRun ||
                        (!wifiDirectManager.isDiscovering.value && now >= serviceWindowUntil) ||
                        (now - lastPeerDiscoveryAt >= 22000L && now >= serviceWindowUntil)

                    if (needsPeerRefresh) {
                        firstRun = false
                        lastPeerDiscoveryAt = now
                        lastServiceDiscoveryAt = now
                        wifiDirectManager.discoverPeers(
                            onSuccess = {
                                Log.d("MainViewModel", "Discovery started")
                            },
                            onFailure = { reason ->
                                Log.e("MainViewModel", "Discovery failed: $reason")
                                if (reason == WifiP2pManager.P2P_UNSUPPORTED) {
                                    _transferError.value = "Wi-Fi Direct not supported"
                                }
                            }
                        )
                    } else if (now - lastServiceDiscoveryAt >= 10000L) {
                        // Let discoverPeers sweep channels cleanly before querying DNS-SD service names
                        lastServiceDiscoveryAt = now
                        serviceWindowUntil = now + 4000L
                        wifiDirectManager.discoverMiniShareServices()
                    }
                } else if (isReceive && !_isTransferActive.value) {
                    val needsReceiveRefresh = !wifiDirectManager.isDiscovering.value ||
                        (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU &&
                            now - lastPeerDiscoveryAt >= 22000L)
                    if (needsReceiveRefresh) {
                        lastPeerDiscoveryAt = now
                        wifiDirectManager.startReceiveListening()
                    }
                }

                if (_connectingDeviceAddress.value != null || isReceive) {
                    wifiDirectManager.requestConnectionInfo()
                }
                delay(2000L)
            }
        }
    }

    private fun stopDiscoveryLoop() {
        discoveryJob?.cancel()
        discoveryJob = null
        bluetoothDiscoveryHelper.stopAll()
    }

    fun onReceiveClicked() {
        _connectingDeviceAddress.value = null
        _transferError.value = null
        _transferDirection.value = TransferDirection.RECEIVE
        _currentScreen.value = AppScreen.RECEIVE
        wifiDirectManager.prepareCleanSession {
            if (_currentScreen.value == AppScreen.RECEIVE) {
                startDiscoveryLoop()
            }
        }
    }

    fun connectAndSend(device: WifiP2pDevice) {
        if (_connectingDeviceAddress.value == device.deviceAddress) return
        _connectingDeviceAddress.value = device.deviceAddress
        _transferError.value = null

        // Timeout guard if peer declines or system invitation expires
        connectTimeoutJob?.cancel()
        connectTimeoutJob = viewModelScope.launch {
            delay(25000L)
            if (_connectingDeviceAddress.value == device.deviceAddress &&
                _currentScreen.value == AppScreen.RADAR &&
                !_isTransferActive.value
            ) {
                _connectingDeviceAddress.value = null
                wifiDirectManager.cancelConnect()
            }
        }

        if (device.status == WifiP2pDevice.CONNECTED) {
            wifiDirectManager.requestConnectionInfo { info ->
                if (info.groupFormed && _currentScreen.value == AppScreen.RADAR && !_isTransferActive.value) {
                    stopDiscoveryLoop()
                    wifiDirectManager.stopDiscovery()
                    _connectingDeviceAddress.value = null
                    val host = info.groupOwnerAddress?.hostAddress ?: "192.168.49.1"
                    startSendingFiles(isGroupOwner = info.isGroupOwner, hostAddress = host)
                }
            }
            return
        }

        val initiateConnect: () -> Unit = {
            wifiDirectManager.connectToDevice(
                device = device,
                groupOwnerIntent = 0,
                onSuccess = {
                    Log.d("MainViewModel", "Connection initiated with ${device.deviceName}")
                },
                onFailure = { reason ->
                    if (reason == WifiP2pManager.BUSY || reason == WifiP2pManager.ERROR) {
                        wifiDirectManager.cancelConnect {
                            viewModelScope.launch {
                                delay(700)
                                if (_currentScreen.value == AppScreen.RADAR) {
                                    wifiDirectManager.connectToDevice(
                                        device = device,
                                        groupOwnerIntent = 0,
                                        onFailure = { retryReason ->
                                            _connectingDeviceAddress.value = null
                                            _transferError.value = "Connect failed (${reasonText(retryReason)})"
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        _connectingDeviceAddress.value = null
                        _transferError.value = "Connect failed (${reasonText(reason)})"
                    }
                }
            )
        }

        if (device.status == WifiP2pDevice.INVITED) {
            wifiDirectManager.cancelConnect {
                viewModelScope.launch {
                    delay(500)
                    if (_currentScreen.value == AppScreen.RADAR) {
                        initiateConnect()
                    }
                }
            }
        } else {
            initiateConnect()
        }
    }

    private fun reasonText(reason: Int): String = when (reason) {
        WifiP2pManager.BUSY -> "Busy"
        WifiP2pManager.P2P_UNSUPPORTED -> "Unsupported"
        else -> "Error"
    }

    private fun startSendingFiles(isGroupOwner: Boolean, hostAddress: String) {
        if (_isTransferActive.value) return
        _isTransferActive.value = true
        _isTransferComplete.value = false
        _transferError.value = null
        _currentScreen.value = AppScreen.TRANSFER
        if (_runInBackground.value) {
            TransferForegroundService.startTransfer(
                context = getApplication(),
                direction = TransferDirection.SEND,
                totalCount = _selectedFiles.value.size
            )
        }

        transferJob?.cancel()
        transferJob = viewModelScope.launch {
            transferEngine.sendFiles(
                isGroupOwner = isGroupOwner,
                hostAddress = hostAddress,
                files = _selectedFiles.value,
                onProgress = { updatedFiles, stats ->
                    _transferFiles.value = updatedFiles
                    _transferStats.value = stats
                    if (_runInBackground.value) {
                        updateForegroundServiceProgress(TransferDirection.SEND, updatedFiles, stats)
                    }
                },
                onComplete = { success, error ->
                    _isTransferActive.value = false
                    _isTransferComplete.value = true
                    TransferForegroundService.endTransfer(
                        context = getApplication(),
                        keepBackgroundRunning = _runInBackground.value
                    )
                    if (!success && error != null && !error.contains("closed", ignoreCase = true)) {
                        _transferError.value = error
                    }
                    if (isAppInBackground && !_runInBackground.value) {
                        cleanupIdleBackgroundSession()
                    }
                }
            )
        }
    }

    private fun startReceivingFiles(isGroupOwner: Boolean, hostAddress: String) {
        if (_isTransferActive.value) return
        _isTransferActive.value = true
        _isTransferComplete.value = false
        _transferError.value = null
        _transferFiles.value = emptyList()
        _transferStats.value = TransferStats()
        _currentScreen.value = AppScreen.TRANSFER
        if (_runInBackground.value) {
            TransferForegroundService.startTransfer(
                context = getApplication(),
                direction = TransferDirection.RECEIVE,
                totalCount = 0
            )
        }

        transferEngine.stopTransfer()
        transferJob?.cancel()
        transferJob = viewModelScope.launch {
            transferEngine.receiveFiles(
                isGroupOwner = isGroupOwner,
                hostAddress = hostAddress,
                storageTreeUri = _storageTreeUri.value,
                onProgress = { updatedFiles, stats ->
                    _transferFiles.value = updatedFiles
                    _transferStats.value = stats
                    if (_runInBackground.value) {
                        updateForegroundServiceProgress(TransferDirection.RECEIVE, updatedFiles, stats)
                    }
                },
                onComplete = { success, error ->
                    _isTransferActive.value = false
                    _isTransferComplete.value = success
                    TransferForegroundService.endTransfer(
                        context = getApplication(),
                        keepBackgroundRunning = _runInBackground.value
                    )
                    if (!success && error != null && !error.contains("closed", ignoreCase = true)) {
                        _transferError.value = error
                    }
                    if (isAppInBackground && !_runInBackground.value) {
                        cleanupIdleBackgroundSession()
                    }
                }
            )
        }
    }

    private fun updateForegroundServiceProgress(
        direction: TransferDirection,
        updatedFiles: List<FileItem>,
        stats: TransferStats
    ) {
        val doneCount = updatedFiles.count {
            it.status == TransferStatus.COMPLETED ||
                it.status == TransferStatus.CANCELLED ||
                it.status == TransferStatus.FAILED
        }
        TransferForegroundService.updateProgress(
            direction = direction,
            completedCount = doneCount,
            totalCount = updatedFiles.size,
            progressPercent = (stats.overallProgress * 100f).toInt(),
            speedText = stats.formattedSpeed
        )
    }

    fun cancelTransferringFile(fileId: String) {
        transferEngine.cancelFile(fileId)
        _transferFiles.update { list ->
            list.map { item ->
                if (item.id == fileId) {
                    item.copy(status = TransferStatus.CANCELLED)
                } else item
            }
        }
    }

    fun cancelAllTransfers() {
        transferEngine.stopTransfer()
        transferJob?.cancel()
        _isTransferActive.value = false
        TransferForegroundService.endTransfer(
            context = getApplication(),
            keepBackgroundRunning = _runInBackground.value
        )
        _transferFiles.update { list ->
            list.map {
                if (it.status == TransferStatus.TRANSFERRING || it.status == TransferStatus.PENDING) {
                    it.copy(status = TransferStatus.CANCELLED)
                } else it
            }
        }
    }

    fun onAppForegrounded() {
        isAppInBackground = false
        wifiDirectManager.ensureRegistered()
        if (_runInBackground.value && !_isTransferActive.value) {
            TransferForegroundService.startBackgroundIdle(getApplication())
        }
    }

    fun onAppBackgrounded() {
        isAppInBackground = true
        if (_runInBackground.value) {
            if (!_isTransferActive.value) {
                TransferForegroundService.startBackgroundIdle(getApplication())
            }
            return
        }
        TransferForegroundService.stop(getApplication())
        resetToHome()
        wifiDirectManager.unregister()
    }

    private fun cleanupIdleBackgroundSession() {
        TransferForegroundService.stop(getApplication())
        connectTimeoutJob?.cancel()
        stopDiscoveryLoop()
        wifiDirectManager.stopDiscovery()
        wifiDirectManager.disconnect()
        wifiDirectManager.unregister()
    }

    private fun stopActiveSession() {
        connectTimeoutJob?.cancel()
        stopDiscoveryLoop()
        cancelAllTransfers()
        wifiDirectManager.stopDiscovery()
        wifiDirectManager.disconnect()
    }

    fun resetToHome() {
        stopActiveSession()
        _connectingDeviceAddress.value = null
        _selectedFiles.value = emptyList()
        _transferFiles.value = emptyList()
        _transferStats.value = TransferStats()
        _isTransferComplete.value = false
        _transferError.value = null
        _currentScreen.value = AppScreen.HOME
    }

    override fun onCleared() {
        super.onCleared()
        TransferForegroundService.stop(getApplication())
        stopActiveSession()
        wifiDirectManager.unregister()
    }
}
