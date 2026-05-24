package com.ksp.couriercompanion.util
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ksp.couriercompanion.ui.MainActivity

object Notifications {
    const val CHANNEL_ID="courier_companion_service"
    fun ensure(context: Context){
        val nm=context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL_ID,"Courier Companion",NotificationManager.IMPORTANCE_LOW))
    }
    fun service(context: Context, title:String, text:String): Notification{
        ensure(context)
        val pi=PendingIntent.getActivity(context,0, Intent(context,MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title).setContentText(text).setContentIntent(pi).setOngoing(true).build()
    }
}
