package com.ksp.couriercompanion.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.ksp.couriercompanion.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OverlayService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var view: TextView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        view = TextView(this).apply {
            text = "Courier Companion\nWaiting for offer..."
            textSize = 14f
            setPadding(24, 16, 24, 16)
            setBackgroundColor(0xCC000000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 120
        }

        windowManager?.addView(view, params)
        observeOffers()
    }

    private fun observeOffers() {
        scope.launch {
            AppDatabase.get(applicationContext).offerDao().observeRecent().collect { offers ->
                val latest = offers.firstOrNull()
                view?.text = if (latest == null) {
                    "Courier Companion\nWaiting for offer..."
                } else {
                    "Courier Companion\n${latest.recommendation ?: "UNKNOWN"} | Score ${"%.1f".format(latest.score ?: 0.0)}\n€${latest.offerAmount ?: 0.0} | ${latest.distanceKm ?: 0.0} km | ${latest.estimatedMinutes ?: 0} min\n${latest.restaurant ?: "Unknown restaurant"}"
                }
            }
        }
    }

    override fun onDestroy() {
        view?.let { windowManager?.removeView(it) }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
