package com.ksp.couriercompanion.importer

import com.ksp.couriercompanion.data.OfferEntity
import com.ksp.couriercompanion.scoring.OfferParser
import com.ksp.couriercompanion.scoring.OfferScorer

object MaxymoImportParser {
    fun parse(content: String): List<OfferEntity> {
        return content.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .dropWhile { it.contains("timestamp", ignoreCase = true) || it.contains("restaurant", ignoreCase = true) }
            .map { line ->
                val parsed = OfferParser.parse(line)
                val scored = OfferScorer.score(parsed)
                OfferEntity(
                    source = "maxymo_import",
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
                    rawText = line
                )
            }
            .toList()
    }
}
