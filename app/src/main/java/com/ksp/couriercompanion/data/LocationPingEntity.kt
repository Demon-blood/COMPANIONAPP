package com.ksp.couriercompanion.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="location_pings")
data class LocationPingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val batteryPercent: Int? = null,
    val speedMps: Float? = null
)
