package com.srm.wifiunblocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat

class StealthVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    companion object {
        const val ACTION_CONNECT = "com.srm.wifiunblocker.CONNECT"
        const val ACTION_DISCONNECT = "com.srm.wifiunblocker.DISCONNECT"
        const val CHANNEL_ID = "SRM_STEALTH_VPN_CHANNEL"
        const val NOTIFICATION_ID = 1001
        
        var isConnected = false
            private set
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
        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())

            val builder = Builder()
                .setSession("SRM Wi-Fi Stealth Tunnel")
                .addAddress("10.0.0.2", 24)
                .addDnsServer("1.1.1.1")
                .addDnsServer("1.0.0.1")
                .addRoute("0.0.0.0", 0)
                .setMtu(1420)

            vpnInterface = builder.establish()
            isConnected = true

            val broadcastIntent = Intent("com.srm.wifiunblocker.VPN_STATUS_CHANGED")
            broadcastIntent.putExtra("connected", true)
            sendBroadcast(broadcastIntent)

        } catch (e: Exception) {
            e.printStackTrace()
            stopVpn()
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        isConnected = false

        val broadcastIntent = Intent("com.srm.wifiunblocker.VPN_STATUS_CHANGED")
        broadcastIntent.putExtra("connected", false)
        sendBroadcast(broadcastIntent)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
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
