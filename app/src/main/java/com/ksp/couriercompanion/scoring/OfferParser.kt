package com.ksp.couriercompanion.scoring

object OfferParser {
    private val euro = Regex("(?:€|EUR)\s*([0-9]+(?:[,.][0-9]{1,2})?)", RegexOption.IGNORE_CASE)
    private val km = Regex("([0-9]+(?:[,.][0-9]+)?)\s*km", RegexOption.IGNORE_CASE)
    private val min = Regex("([0-9]{1,3})\s*(?:min|minutes|mins)", RegexOption.IGNORE_CASE)
    private val from = Regex("(?:from|pickup|restaurant|store)[:\s]+([^\n]+)", RegexOption.IGNORE_CASE)
    private val to = Regex("(?:to|dropoff|deliver)[:\s]+([^\n]+)", RegexOption.IGNORE_CASE)

    fun parse(raw: String): ParsedOffer {
        val text = raw.replace("", "
")
        val payout = euro.find(text)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
        val distance = km.find(text)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
        val minutes = min.find(text)?.groupValues?.get(1)?.toIntOrNull()
        val restaurant = from.find(text)?.groupValues?.get(1)?.trim()?.take(80)
            ?: text.lines().firstOrNull { it.length in 3..80 && !it.contains("€") && !it.contains("km", true) }
        val drop = to.find(text)?.groupValues?.get(1)?.trim()?.take(80)
        val accepted = when {
            text.contains("accepted", true) || text.contains("aanvaard", true) -> true
            text.contains("rejected", true) || text.contains("declined", true) || text.contains("geweigerd", true) -> false
            else -> null
        }
        val completed = when {
            text.contains("completed", true) || text.contains("delivered", true) || text.contains("voltooid", true) -> true
            text.contains("cancelled", true) || text.contains("geannuleerd", true) -> false
            else -> null
        }
        return ParsedOffer(restaurant=restaurant, dropoffArea=drop, payout=payout, distanceKm=distance, minutes=minutes, accepted=accepted, completed=completed, rawText=raw)
    }
}
