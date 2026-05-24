package com.ksp.couriercompanion.importer
import com.ksp.couriercompanion.data.OfferEntity
import com.ksp.couriercompanion.scoring.OfferParser
import com.ksp.couriercompanion.scoring.OfferScorer
import org.json.JSONArray
import org.json.JSONObject

object MaxymoImportParser {
    data class Result(val imported: List<OfferEntity>, val rejectedLines: Int)

    fun parse(content: String): Result {
        val trimmed = content.trim()
        return when {
            trimmed.startsWith("[") -> parseJsonArray(trimmed)
            trimmed.startsWith("{") -> parseJsonObject(trimmed)
            else -> parseCsvOrText(trimmed)
        }
    }

    private fun parseJsonArray(text: String): Result {
        val arr = JSONArray(text); val list = mutableListOf<OfferEntity>()
        for (i in 0 until arr.length()) list += mapJson(arr.getJSONObject(i), arr.getJSONObject(i).toString())
        return Result(list, 0)
    }
    private fun parseJsonObject(text: String): Result {
        val obj = JSONObject(text)
        val arr = obj.optJSONArray("history") ?: obj.optJSONArray("offers") ?: JSONArray().put(obj)
        val list = mutableListOf<OfferEntity>()
        for (i in 0 until arr.length()) list += mapJson(arr.getJSONObject(i), arr.getJSONObject(i).toString())
        return Result(list, 0)
    }
    private fun mapJson(o: JSONObject, raw: String): OfferEntity {
        val timestamp = o.optLong("timestamp", System.currentTimeMillis())
        val payout = o.optDoubleOrNull("offerAmount") ?: o.optDoubleOrNull("payout") ?: o.optDoubleOrNull("amount")
        val distance = o.optDoubleOrNull("distanceKm") ?: o.optDoubleOrNull("distance")
        val minutes = o.optIntOrNull("estimatedMinutes") ?: o.optIntOrNull("minutes")
        val parsed = OfferParser.parse(raw)
        val scored = OfferScorer.score(parsed.copy(payout=payout ?: parsed.payout, distanceKm=distance ?: parsed.distanceKm, minutes=minutes ?: parsed.minutes))
        return OfferEntity(source="maxymo_import", timestamp=timestamp, restaurant=o.optStringOrNull("restaurant") ?: parsed.restaurant,
            pickupArea=o.optStringOrNull("pickupArea") ?: parsed.pickupArea, dropoffArea=o.optStringOrNull("dropoffArea") ?: parsed.dropoffArea,
            offerAmount=payout ?: parsed.payout, distanceKm=distance ?: parsed.distanceKm, estimatedMinutes=minutes ?: parsed.minutes,
            accepted=o.optBooleanOrNull("accepted") ?: parsed.accepted, completed=o.optBooleanOrNull("completed") ?: parsed.completed,
            score=scored.score, recommendation=scored.recommendation, latitude=null, longitude=null, rawText=raw)
    }
    private fun parseCsvOrText(text: String): Result {
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return Result(emptyList(),0)
        val delimiter = if (lines.first().contains(';')) ';' else ','
        val header = lines.first().split(delimiter).map { it.trim().lowercase() }
        val hasHeader = header.any { it.contains("amount") || it.contains("payout") || it.contains("restaurant") || it.contains("distance") }
        val data = if (hasHeader) lines.drop(1) else lines
        var rejected = 0
        val out = data.mapNotNull { line ->
            runCatching {
                if (hasHeader) mapCsv(header, line.split(delimiter).map { it.trim().trim('"') }, line) else mapFreeText(line)
            }.getOrElse { rejected++; null }
        }
        return Result(out, rejected)
    }
    private fun mapCsv(header: List<String>, cols: List<String>, raw: String): OfferEntity {
        fun col(vararg names: String): String? = names.firstNotNullOfOrNull { n -> header.indexOfFirst { it.contains(n) }.takeIf { it >= 0 && it < cols.size }?.let { cols[it] } }
        val parsed = OfferParser.parse(raw)
        val payout = col("amount","payout","earn")?.replace(',', '.')?.toDoubleOrNull() ?: parsed.payout
        val distance = col("distance","km")?.replace(',', '.')?.toDoubleOrNull() ?: parsed.distanceKm
        val minutes = col("minute","time","duration")?.filter { it.isDigit() }?.toIntOrNull() ?: parsed.minutes
        val scored = OfferScorer.score(parsed.copy(payout=payout,distanceKm=distance,minutes=minutes))
        return OfferEntity(source="maxymo_import", timestamp=System.currentTimeMillis(), restaurant=col("restaurant","store") ?: parsed.restaurant,
            pickupArea=col("pickup") ?: parsed.pickupArea, dropoffArea=col("drop") ?: parsed.dropoffArea, offerAmount=payout,
            distanceKm=distance, estimatedMinutes=minutes, accepted=col("accepted")?.toBool() ?: parsed.accepted,
            completed=col("completed")?.toBool() ?: parsed.completed, score=scored.score, recommendation=scored.recommendation,
            latitude=null, longitude=null, rawText=raw)
    }
    private fun mapFreeText(raw: String): OfferEntity {
        val p = OfferParser.parse(raw); val s = OfferScorer.score(p)
        return OfferEntity(source="maxymo_import", timestamp=System.currentTimeMillis(), restaurant=p.restaurant, pickupArea=p.pickupArea,
            dropoffArea=p.dropoffArea, offerAmount=p.payout, distanceKm=p.distanceKm, estimatedMinutes=p.minutes,
            accepted=p.accepted, completed=p.completed, score=s.score, recommendation=s.recommendation, latitude=null, longitude=null, rawText=raw)
    }
}
private fun JSONObject.optStringOrNull(name:String)= if(has(name)&&!isNull(name)) optString(name) else null
private fun JSONObject.optDoubleOrNull(name:String)= if(has(name)&&!isNull(name)) optDouble(name) else null
private fun JSONObject.optIntOrNull(name:String)= if(has(name)&&!isNull(name)) optInt(name) else null
private fun JSONObject.optBooleanOrNull(name:String)= if(has(name)&&!isNull(name)) optBoolean(name) else null
private fun String.toBool()= lowercase().let { it=="true" || it=="yes" || it=="1" || it=="accepted" }
