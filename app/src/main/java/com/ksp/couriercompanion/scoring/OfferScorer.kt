package com.ksp.couriercompanion.scoring

data class ScoreResult(
    val score: Double,
    val recommendation: String
)

object OfferScorer {
    fun score(offer: ParsedOffer): ScoreResult {
        val amount = offer.offerAmount ?: 0.0
        val distance = offer.distanceKm ?: 3.0
        val minutes = offer.estimatedMinutes ?: 20

        val eurosPerKm = if (distance > 0.0) amount / distance else amount
        val eurosPerHour = if (minutes > 0) amount / (minutes / 60.0) else 0.0

        val score = (eurosPerKm * 25.0) + (eurosPerHour * 1.5) - (distance * 2.0)
        val recommendation = when {
            score >= 55.0 -> "ACCEPT"
            score >= 35.0 -> "MAYBE"
            else -> "REJECT"
        }

        return ScoreResult(score = score, recommendation = recommendation)
    }
}
