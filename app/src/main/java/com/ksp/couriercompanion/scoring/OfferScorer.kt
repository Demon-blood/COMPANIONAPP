package com.ksp.couriercompanion.scoring

data class ScoreResult(val score: Int, val recommendation: String, val euroPerHour: Double?, val euroPerKm: Double?)

object OfferScorer {
    fun score(offer: ParsedOffer, batteryPercent: Int? = null, raining: Boolean = false): ScoreResult {
        val payout = offer.payout ?: 0.0
        val km = offer.distanceKm ?: 0.0
        val min = offer.minutes ?: 0
        val eph = if (payout > 0 && min > 0) payout / min * 60.0 else null
        val epk = if (payout > 0 && km > 0) payout / km else null
        var s = 50
        if (eph != null) s += when { eph >= 22 -> 25; eph >= 18 -> 15; eph >= 14 -> 5; eph < 10 -> -20; else -> -5 }
        if (epk != null) s += when { epk >= 2.2 -> 15; epk >= 1.6 -> 7; epk < 1.0 -> -15; else -> 0 }
        if (km > 5.0) s -= 10
        if (min > 30) s -= 10
        if ((batteryPercent ?: 100) < 25 && km > 3.0) s -= 15
        if (raining) s += 8
        s = s.coerceIn(0, 100)
        val rec = when { s >= 75 -> "ACCEPT STRONG"; s >= 60 -> "ACCEPT"; s >= 45 -> "MAYBE"; else -> "REJECT" }
        return ScoreResult(s, rec, eph, epk)
    }
}
