package com.ksp.couriercompanion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferDao {
    @Insert
    suspend fun insert(offer: OfferEntity): Long

    @Insert
    suspend fun insertAll(offers: List<OfferEntity>)

    @Query("SELECT * FROM offers ORDER BY timestamp DESC LIMIT 50")
    fun observeRecent(): Flow<List<OfferEntity>>

    @Query("SELECT COUNT(*) FROM offers")
    suspend fun count(): Int

    @Query("SELECT AVG(offerAmount) FROM offers WHERE offerAmount IS NOT NULL")
    suspend fun avgPayout(): Double?

    @Query("SELECT AVG(score) FROM offers WHERE score IS NOT NULL")
    suspend fun avgScore(): Double?
}
