package com.ksp.couriercompanion.scoring

data class ParsedOffer(
    val restaurant: String?,
    val offerAmount: Double?,
    val distanceKm: Double?,
    val estimatedMinutes: Int?,
    val rawText: String
)
