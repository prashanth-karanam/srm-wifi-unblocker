package com.srm.wifiunblocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class StealthVpnService : VpnService(), Tunnel {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var backend: Backend? = null

    companion object {
        const val ACTION_CONNECT = "com.srm.wifiunblocker.CONNECT"
        const val ACTION_DISCONNECT = "com.srm.wifiunblocker.DISCONNECT"
        const val CHANNEL_ID = "SRM_STEALTH_VPN_CHANNEL"
        const val NOTIFICATION_ID = 1001
        const val TUNNEL_NAME = "SRMWiFiUnblocker"
        
        var isConnected = false
            private set
    }

    override fun getName(): String = TUNNEL_NAME

    override fun onStateChange(newState: Tunnel.State) {
        isConnected = (newState == Tunnel.State.UP)
        val broadcastIntent = Intent("com.srm.wifiunblocker.VPN_STATUS_CHANGED").apply {
            putExtra("connected", isConnected)
        }
        sendBroadcast(broadcastIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_DISCONNECT) {
            stopVpn()
            return START_NOT_STICKY
        }

        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            try {
                if (backend == null) {
                    backend = GoBackend(applicationContext)
                }
                val warpManager = WarpManager(applicationContext)
                val config = warpManager.getOrGenerateConfig()

                backend?.setState(this@StealthVpnService, Tunnel.State.UP, config)
                isConnected = true

                val broadcastIntent = Intent("com.srm.wifiunblocker.VPN_STATUS_CHANGED").apply {
                    putExtra("connected", true)
                }
                sendBroadcast(broadcastIntent)

            } catch (e: Exception) {
                e.printStackTrace()
                stopVpn()
            }
        }
    }

    private fun stopVpn() {
        serviceScope.launch {
            try {
                backend?.setState(this@StealthVpnService, Tunnel.State.DOWN, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isConnected = false

            val broadcastIntent = Intent("com.srm.wifiunblocker.VPN_STATUS_CHANGED").apply {
                putExtra("connected", false)
            }
            sendBroadcast(broadcastIntent)

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SRM Wi-Fi Unblocker Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SRM Wi-Fi Unblocker")
            .setContentText("Stealth tunnel active over Port 443")
            .setSmallIcon(R.drawable.ic_shield)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
