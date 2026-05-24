package com.ksp.couriercompanion.overlay
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ksp.couriercompanion.data.AppDatabase
import kotlinx.coroutines.*

class OverlayService: Service(){
    private var view: TextView?=null
    private lateinit var wm:WindowManager
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main)
    override fun onCreate(){ super.onCreate(); wm=getSystemService(WINDOW_SERVICE) as WindowManager; show(); observe() }
    private fun show(){
        view=TextView(this).apply{ text="Courier Companion
Waiting for offers"; setPadding(24,16,24,16); textSize=14f; setTextColor(0xffffffff.toInt()); setBackgroundColor(0xcc111827.toInt()) }
        val lp=WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT)
        lp.gravity=Gravity.TOP or Gravity.START; lp.x=20; lp.y=160; wm.addView(view,lp)
    }
    private fun observe(){ scope.launch { AppDatabase.get(applicationContext).offerDao().recent(1).collect{ list -> list.firstOrNull()?.let{ view?.text="${it.recommendation ?: "WAIT"}  ${it.score ?: 0}/100
€${it.offerAmount ?: 0.0} • ${it.distanceKm ?: 0.0} km • ${it.estimatedMinutes ?: 0} min
${it.restaurant ?: "Unknown"}" } } } }
    override fun onDestroy(){ view?.let{wm.removeView(it)}; scope.cancel(); super.onDestroy() }
    override fun onBind(intent:Intent?):IBinder?=null
}
