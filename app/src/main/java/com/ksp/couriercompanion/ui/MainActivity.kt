package com.ksp.couriercompanion.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ksp.couriercompanion.data.AppDatabase
import com.ksp.couriercompanion.services.LocationLogService
import com.ksp.couriercompanion.services.OverlayService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var statsText: TextView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refreshStats() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Courier Companion AIO"
            textSize = 24f
        }

        statsText = TextView(this).apply {
            textSize = 16f
            text = "Loading stats..."
        }

        val permissionsButton = Button(this).apply {
            text = "Grant location / notification permissions"
            setOnClickListener {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                )
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val overlayPermissionButton = Button(this).apply {
            text = "Allow floating overlay"
            setOnClickListener {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        val startGpsButton = Button(this).apply {
            text = "Start GPS logging"
            setOnClickListener {
                ContextCompat.startForegroundService(
                    this@MainActivity,
                    Intent(this@MainActivity, LocationLogService::class.java)
                )
            }
        }

        val startOverlayButton = Button(this).apply {
            text = "Start overlay"
            setOnClickListener {
                startService(Intent(this@MainActivity, OverlayService::class.java))
            }
        }

        val importButton = Button(this).apply {
            text = "Import Maxymo history"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, ImportActivity::class.java))
            }
        }

        layout.addView(title)
        layout.addView(statsText)
        layout.addView(permissionsButton)
        layout.addView(overlayPermissionButton)
        layout.addView(startGpsButton)
        layout.addView(startOverlayButton)
        layout.addView(importButton)

        setContentView(ScrollView(this).apply { addView(layout) })
        refreshStats()
    }

    private fun refreshStats() {
        lifecycleScope.launch {
            val dao = AppDatabase.get(this@MainActivity).offerDao()
            val count = dao.count()
            val avgPayout = dao.avgPayout() ?: 0.0
            val avgScore = dao.avgScore() ?: 0.0
            statsText.text = "Logged offers: $count\nAverage payout: €${"%.2f".format(avgPayout)}\nAverage score: ${"%.1f".format(avgScore)}"
        }
    }
}
