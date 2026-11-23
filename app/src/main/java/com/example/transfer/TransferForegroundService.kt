package com.example.transfer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.TransferDirection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class TransferForegroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isForegroundActive = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        activeInstance = this
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        pendingStartService = false
        when (intent?.action) {
            ACTION_CANCEL -> {
                _cancelRequests.tryEmit(Unit)
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }
            ACTION_STOP -> {
                if (!isForegroundActive) {
                    enterForeground(buildNotificationFromState(currentState))
                }
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }
            else -> {
                if (!shouldStayRunning) {
                    if (!isForegroundActive) {
                        enterForeground(buildNotificationFromState(currentState))
                    }
                    stopForegroundAndSelf()
                    return START_NOT_STICKY
                }
                acquireWakeLock()
                enterForeground(buildNotificationFromState(currentState))
                return START_NOT_STICKY
            }
        }
    }

    private fun enterForeground(notification: Notification) {
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceType)
        isForegroundActive = true
    }

    private fun updateNotification(state: TransferNotificationState) {
        if (!shouldStayRunning || !isForegroundActive) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        manager.notify(NOTIFICATION_ID, buildNotificationFromState(state))
    }

    private fun stopForegroundAndSelf() {
        shouldStayRunning = false
        releaseWakeLock()
        if (isForegroundActive) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            isForegroundActive = false
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    override fun onDestroy() {
        if (activeInstance === this) {
            activeInstance = null
        }
        pendingStartService = false
        isForegroundActive = false
        releaseWakeLock()
        super.onDestroy()
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MiniShare:TransferWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(60 * 60 * 1000L) // 1 hour safety timeout
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {
        } finally {
            wakeLock = null
        }
    }

    private fun ensureNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.transfer_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.transfer_notification_channel_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotificationFromState(state: TransferNotificationState): Notification {
        return buildNotification(
            direction = state.direction,
            completedCount = state.completedCount,
            totalCount = state.totalCount,
            progressPercent = state.progressPercent,
            speedText = state.speedText
        )
    }

    private fun buildNotification(
        direction: TransferDirection,
        completedCount: Int,
        totalCount: Int,
        progressPercent: Int,
        speedText: String
    ): Notification {
        val title = if (direction == TransferDirection.SEND) {
            getString(R.string.transfer_sending_title)
        } else {
            getString(R.string.transfer_receiving_title)
        }

        val contentText = buildString {
            if (totalCount > 0) {
                append("$completedCount / $totalCount files")
                append(" • ")
            }
            append("$progressPercent%")
            if (speedText.isNotBlank() && speedText != "0 B/s") {
                append(" • ")
                append(speedText)
            }
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, TransferForegroundService::class.java).apply {
            action = ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_transfer_notification)
            .setContentTitle(title)
            .setContentText(contentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progressPercent.coerceIn(0, 100), totalCount == 0 && progressPercent == 0)
            .setContentIntent(contentPendingIntent)
            .addAction(0, getString(R.string.transfer_cancel_action), cancelPendingIntent)
            .build()
    }

    private data class TransferNotificationState(
        val direction: TransferDirection = TransferDirection.SEND,
        val completedCount: Int = 0,
        val totalCount: Int = 0,
        val progressPercent: Int = 0,
        val speedText: String = ""
    )

    companion object {
        private const val CHANNEL_ID = "mini_share_transfer_channel"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_START = "com.example.transfer.action.START"
        private const val ACTION_STOP = "com.example.transfer.action.STOP"
        private const val ACTION_CANCEL = "com.example.transfer.action.CANCEL"

        private val mainHandler = Handler(Looper.getMainLooper())

        @Volatile
        private var shouldStayRunning = false

        @Volatile
        private var pendingStartService = false

        @Volatile
        private var currentState = TransferNotificationState()

        @Volatile
        private var activeInstance: TransferForegroundService? = null

        private val _cancelRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val cancelRequests = _cancelRequests.asSharedFlow()

        fun start(context: Context, direction: TransferDirection, totalCount: Int) {
            shouldStayRunning = true
            pendingStartService = true
            currentState = TransferNotificationState(
                direction = direction,
                completedCount = 0,
                totalCount = totalCount,
                progressPercent = 0,
                speedText = ""
            )
            val intent = Intent(context, TransferForegroundService::class.java).apply {
                action = ACTION_START
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {
                pendingStartService = false
            }
        }

        fun updateProgress(
            direction: TransferDirection,
            completedCount: Int,
            totalCount: Int,
            progressPercent: Int,
            speedText: String
        ) {
            if (!shouldStayRunning) return
            val updated = TransferNotificationState(
                direction = direction,
                completedCount = completedCount,
                totalCount = totalCount,
                progressPercent = progressPercent.coerceIn(0, 100),
                speedText = speedText
            )
            currentState = updated
            activeInstance?.updateNotification(updated)
        }

        fun stop(context: Context) {
            shouldStayRunning = false
            mainHandler.post {
                val instance = activeInstance
                if (instance != null) {
                    instance.stopForegroundAndSelf()
                } else if (!pendingStartService) {
                    try {
                        context.stopService(Intent(context, TransferForegroundService::class.java))
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}
