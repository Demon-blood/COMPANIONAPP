package com.ksp.couriercompanion.services
import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.ksp.couriercompanion.data.AppDatabase
import com.ksp.couriercompanion.data.LocationPingEntity
import com.ksp.couriercompanion.util.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationLogService: Service(){
    private lateinit var client:FusedLocationProviderClient
    private val scope=CoroutineScope(Dispatchers.IO)
    private val cb=object:LocationCallback(){ override fun onLocationResult(r:LocationResult){ r.lastLocation?.let{loc-> scope.launch{ AppDatabase.get(applicationContext).locationDao().insert(LocationPingEntity(timestamp=System.currentTimeMillis(), latitude=loc.latitude, longitude=loc.longitude, speedMps=loc.speed)) } } } }
    override fun onCreate(){ super.onCreate(); client=LocationServices.getFusedLocationProviderClient(this); startForeground(20, Notifications.service(this,"Courier Companion","GPS learning is active")); start() }
    private fun start(){ if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) return
        val req=LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 15000L).setMinUpdateIntervalMillis(5000L).build(); client.requestLocationUpdates(req, cb, mainLooper) }
    override fun onDestroy(){ client.removeLocationUpdates(cb); super.onDestroy() }
    override fun onBind(intent:Intent?):IBinder?=null
}
