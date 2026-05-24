package com.ksp.couriercompanion.ui
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ksp.couriercompanion.data.AppDatabase
import com.ksp.couriercompanion.overlay.OverlayService
import com.ksp.couriercompanion.services.LocationLogService
import com.ksp.couriercompanion.services.ScreenCaptureService
import kotlinx.coroutines.launch

class MainActivity: ComponentActivity(){
    private lateinit var status:TextView
    private val perm=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ refreshStats() }
    override fun onCreate(b:Bundle?){ super.onCreate(b)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(32,48,32,32) }
        status=TextView(this).apply{ textSize=16f }
        fun btn(t:String, f:()->Unit)=Button(this).apply{ text=t; setOnClickListener{f()} }
        root.addView(TextView(this).apply{ text="Courier Companion AIO"; textSize=26f })
        root.addView(status)
        root.addView(btn("Grant Location + Notifications Permission"){ perm.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)) })
        root.addView(btn("Open Notification Access Settings"){ startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) })
        root.addView(btn("Allow Floating Overlay"){ startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) })
        root.addView(btn("Start GPS Learning"){ ContextCompat.startForegroundService(this, Intent(this, LocationLogService::class.java)) })
        root.addView(btn("Start Screen OCR Service"){ ContextCompat.startForegroundService(this, Intent(this, ScreenCaptureService::class.java)) })
        root.addView(btn("Show Overlay"){ if(Settings.canDrawOverlays(this)) startService(Intent(this, OverlayService::class.java)) else Toast.makeText(this,"Allow overlay permission first",Toast.LENGTH_LONG).show() })
        root.addView(btn("Import Maxymo History CSV / JSON / TXT"){ startActivity(Intent(this, ImportActivity::class.java)) })
        root.addView(btn("Refresh Stats"){ refreshStats() })
        setContentView(root); refreshStats()
    }
    private fun refreshStats(){ lifecycleScope.launch{ val dao=AppDatabase.get(this@MainActivity).offerDao(); status.text="Offers saved: ${dao.count()}
Average payout: €${"%.2f".format(dao.avgPayout() ?: 0.0)}
Average score: ${"%.1f".format(dao.avgScore() ?: 0.0)}" } }
}
