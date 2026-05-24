package com.ksp.couriercompanion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,
    val timestamp: Long,
    val restaurant: String?,
    val pickupArea: String?,
    val dropoffArea: String?,
    val offerAmount: Double?,
    val distanceKm: Double?,
    val estimatedMinutes: Int?,
    val accepted: Boolean?,
    val completed: Boolean?,
    val score: Double?,
    val recommendation: String?,
    val rawText: String
)
