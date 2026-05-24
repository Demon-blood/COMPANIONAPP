package com.ksp.couriercompanion.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ksp.couriercompanion.data.AppDatabase
import com.ksp.couriercompanion.data.LocationPingEntity
import com.ksp.couriercompanion.util.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationLogService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val client by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            scope.launch {
                AppDatabase.get(applicationContext).locationDao().insert(
                    LocationPingEntity(
                        timestamp = System.currentTimeMillis(),
                        latitude = location.latitude,
                        longitude = location.longitude,
                        speedMetersPerSecond = location.speed
                    )
                )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            1001,
            Notifications.foreground(this, "Courier Companion", "GPS logging active")
        )

        val hasFine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            stopSelf()
            return START_NOT_STICKY
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .build()

        client.requestLocationUpdates(request, callback, mainLooper)
        return START_STICKY
    }

    override fun onDestroy() {
        client.removeLocationUpdates(callback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
