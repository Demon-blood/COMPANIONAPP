package com.ksp.couriercompanion.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ksp.couriercompanion.util.Notifications

class ScreenCaptureService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            1002,
            Notifications.foreground(this, "Courier Companion", "Screen OCR service scaffold active")
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
