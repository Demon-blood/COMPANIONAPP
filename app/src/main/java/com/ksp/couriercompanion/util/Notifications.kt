package com.ksp.couriercompanion.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ksp.couriercompanion.R

object Notifications {
    const val CHANNEL_ID = "courier_companion_tracking"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Courier Companion Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun foreground(service: Service, title: String, text: String): Notification {
        ensureChannel(service)
        return NotificationCompat.Builder(service, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .build()
    }
}
