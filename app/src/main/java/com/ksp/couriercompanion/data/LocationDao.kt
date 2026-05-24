package com.ksp.couriercompanion.data
import androidx.room.*

@Dao
interface LocationDao {
    @Insert suspend fun insert(ping: LocationPingEntity): Long
    @Query("SELECT * FROM location_pings ORDER BY timestamp DESC LIMIT 1") suspend fun last(): LocationPingEntity?
}
