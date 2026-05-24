package com.ksp.couriercompanion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(ping: LocationPingEntity): Long

    @Query("SELECT * FROM location_pings ORDER BY timestamp DESC LIMIT 100")
    suspend fun recent(): List<LocationPingEntity>
}
