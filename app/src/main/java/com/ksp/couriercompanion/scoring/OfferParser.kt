package com.ksp.couriercompanion.scoring

object OfferParser {
    private val euroRegex = Regex("""(?:€|EUR)\s*([0-9]+(?:[,.][0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    private val kmRegex = Regex("""([0-9]+(?:[,.][0-9]+)?)\s*km""", RegexOption.IGNORE_CASE)
    private val minRegex = Regex("""([0-9]{1,3})\s*(?:min|minutes)""", RegexOption.IGNORE_CASE)

    fun parse(rawText: String): ParsedOffer {
        val normalized = rawText.replace(',', '.')
        val amount = euroRegex.find(normalized)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        val distance = kmRegex.find(normalized)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        val minutes = minRegex.find(normalized)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val restaurant = rawText.lineSequence()
            .map { it.trim() }
            .firstOrNull { line ->
                line.length in 3..60 &&
                !line.contains("€") &&
                !line.contains("km", ignoreCase = true) &&
                !line.contains("min", ignoreCase = true)
            }

        return ParsedOffer(
            restaurant = restaurant,
            offerAmount = amount,
            distanceKm = distance,
            estimatedMinutes = minutes,
            rawText = rawText
        )
    }
}
