package com.ksp.couriercompanion.scoring

data class ParsedOffer(
    val restaurant: String? = null,
    val pickupArea: String? = null,
    val dropoffArea: String? = null,
    val payout: Double? = null,
    val distanceKm: Double? = null,
    val minutes: Int? = null,
    val accepted: Boolean? = null,
    val completed: Boolean? = null,
    val rawText: String
)
