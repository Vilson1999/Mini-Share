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
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.transfer_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.transfer_notification_channel_desc)
                setShowBadge(false)
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        pendingStartService = false
        when (intent?.action) {
            ACTION_STOP_BY_USER -> {
                if (!isForegroundActive) enterForeground(buildNotification(currentState))
                stopForegroundAndSelf(notifyClosed = true)
            }
            ACTION_STOP -> {
                if (!isForegroundActive) enterForeground(buildNotification(currentState))
                stopForegroundAndSelf(notifyClosed = false)
            }
            else -> {
                if (!shouldStayRunning) {
                    if (!isForegroundActive) enterForeground(buildNotification(currentState))
                    stopForegroundAndSelf(notifyClosed = false)
                } else {
                    syncWakeLock(currentState.isTransferring)
                    enterForeground(buildNotification(currentState))
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopForegroundAndSelf(notifyClosed = true)
        super.onTaskRemoved(rootIntent)
    }

    private fun enterForeground(notification: Notification) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
        isForegroundActive = true
    }

    private fun updateNotification(state: ForegroundState) {
        if (!shouldStayRunning || !isForegroundActive) return
        syncWakeLock(state.isTransferring)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        manager.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun stopForegroundAndSelf(notifyClosed: Boolean) {
        shouldStayRunning = false
        currentState = ForegroundState()
        syncWakeLock(false)
        if (isForegroundActive) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            isForegroundActive = false
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(NOTIFICATION_ID)
        if (notifyClosed) {
            _stopRequests.tryEmit(Unit)
        }
        stopSelf()
    }

    override fun onDestroy() {
        val wasRunning = shouldStayRunning
        if (activeInstance === this) activeInstance = null
        shouldStayRunning = false
        pendingStartService = false
        isForegroundActive = false
        syncWakeLock(false)
        if (wasRunning) {
            _stopRequests.tryEmit(Unit)
        }
        super.onDestroy()
    }

    private fun syncWakeLock(needed: Boolean) {
        if (needed) {
            if (wakeLock?.isHeld != true) {
                val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MiniShare:Transfer").apply {
                    setReferenceCounted(false)
                    acquire(30 * 60 * 1000L)
                }
            }
        } else {
            try {
                if (wakeLock?.isHeld == true) wakeLock?.release()
            } catch (_: Exception) {
            } finally {
                wakeLock = null
            }
        }
    }

    private fun buildNotification(state: ForegroundState): Notification {
        val openPending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val actionPending = PendingIntent.getService(
            this,
            1,
            Intent(this, TransferForegroundService::class.java).apply { action = ACTION_STOP_BY_USER },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_transfer_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openPending)

        return if (!state.isTransferring) {
            builder
                .setContentTitle(getString(R.string.background_running_title))
                .setContentText(getString(R.string.background_running_text))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .addAction(0, getString(R.string.background_stop_action), actionPending)
                .build()
        } else {
            val title = getString(
                if (state.direction == TransferDirection.SEND) R.string.transfer_sending_title
                else R.string.transfer_receiving_title
            )
            val text = buildString {
                if (state.totalCount > 0) append("${state.completedCount}/${state.totalCount} • ")
                append("${state.progressPercent}%")
                if (state.speedText.isNotBlank() && state.speedText != "0 B/s") {
                    append(" • ").append(state.speedText)
                }
            }
            builder
                .setContentTitle(title)
                .setContentText(text)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setProgress(100, state.progressPercent.coerceIn(0, 100), state.totalCount == 0 && state.progressPercent == 0)
                .addAction(0, getString(R.string.transfer_cancel_action), actionPending)
                .build()
        }
    }

    private data class ForegroundState(
        val isTransferring: Boolean = false,
        val direction: TransferDirection = TransferDirection.SEND,
        val completedCount: Int = 0,
        val totalCount: Int = 0,
        val progressPercent: Int = 0,
        val speedText: String = ""
    )

    companion object {
        private const val CHANNEL_ID = "mini_share_bg"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_START = "com.example.transfer.START"
        private const val ACTION_STOP = "com.example.transfer.STOP"
        private const val ACTION_STOP_BY_USER = "com.example.transfer.USER_STOP"

        private val mainHandler = Handler(Looper.getMainLooper())

        @Volatile
        private var shouldStayRunning = false

        @Volatile
        private var pendingStartService = false

        @Volatile
        private var currentState = ForegroundState()

        @Volatile
        private var activeInstance: TransferForegroundService? = null

        private val _stopRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val stopRequests = _stopRequests.asSharedFlow()

        fun isServiceRunning(): Boolean = shouldStayRunning || pendingStartService || activeInstance != null

        fun startBackgroundIdle(context: Context) {
            startOrUpdate(context, ForegroundState(isTransferring = false))
        }

        fun startTransfer(context: Context, direction: TransferDirection, totalCount: Int) {
            startOrUpdate(
                context,
                ForegroundState(
                    isTransferring = true,
                    direction = direction,
                    totalCount = totalCount
                )
            )
        }

        private fun startOrUpdate(context: Context, state: ForegroundState) {
            shouldStayRunning = true
            currentState = state
            if (activeInstance != null) {
                mainHandler.post { activeInstance?.updateNotification(state) }
                return
            }
            pendingStartService = true
            val intent = Intent(context, TransferForegroundService::class.java).apply { action = ACTION_START }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {
                pendingStartService = false
                shouldStayRunning = false
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
            val updated = ForegroundState(
                isTransferring = true,
                direction = direction,
                completedCount = completedCount,
                totalCount = totalCount,
                progressPercent = progressPercent.coerceIn(0, 100),
                speedText = speedText
            )
            currentState = updated
            activeInstance?.updateNotification(updated)
        }

        fun endTransfer(context: Context, keepBackgroundRunning: Boolean) {
            if (keepBackgroundRunning) startBackgroundIdle(context) else stop(context)
        }

        fun stop(context: Context) {
            shouldStayRunning = false
            mainHandler.post {
                val instance = activeInstance
                if (instance != null) {
                    instance.stopForegroundAndSelf(notifyClosed = false)
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
