package com.ksp.couriercompanion.ocr

import android.content.Context
import com.ksp.couriercompanion.data.AppDatabase
import com.ksp.couriercompanion.data.OfferEntity
import com.ksp.couriercompanion.scoring.OfferParser
import com.ksp.couriercompanion.scoring.OfferScorer

object OcrOfferProcessor {
    suspend fun processVisibleText(context: Context, text: String): Long? {
        if (!looksLikeCourierOffer(text)) return null

        val parsed = OfferParser.parse(text)
        val scored = OfferScorer.score(parsed)

        val offer = OfferEntity(
            source = "screen_ocr",
            timestamp = System.currentTimeMillis(),
            restaurant = parsed.restaurant,
            pickupArea = null,
            dropoffArea = null,
            offerAmount = parsed.offerAmount,
            distanceKm = parsed.distanceKm,
            estimatedMinutes = parsed.estimatedMinutes,
            accepted = null,
            completed = null,
            score = scored.score,
            recommendation = scored.recommendation,
            rawText = text
        )

        return AppDatabase.get(context.applicationContext).offerDao().insert(offer)
    }

    private fun looksLikeCourierOffer(text: String): Boolean {
        if (text.isBlank()) return false
        val lower = text.lowercase()
        val hasMoney = lower.contains("€") || lower.contains("eur")
        val hasDistance = lower.contains("km")
        val hasTime = lower.contains("min")
        val hasCourierHint = lower.contains("uber") ||
            lower.contains("maxymo") ||
            lower.contains("delivery") ||
            lower.contains("pickup") ||
            lower.contains("pick up") ||
            lower.contains("restaurant")

        return (hasMoney && hasDistance) || (hasMoney && hasTime) || (hasCourierHint && (hasMoney || hasDistance))
    }
}
