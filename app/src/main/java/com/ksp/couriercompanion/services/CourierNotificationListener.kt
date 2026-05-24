package com.ksp.couriercompanion.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ksp.couriercompanion.data.AppDatabase
import com.ksp.couriercompanion.data.OfferEntity
import com.ksp.couriercompanion.scoring.OfferParser
import com.ksp.couriercompanion.scoring.OfferScorer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourierNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val body = extras.getCharSequence("android.text")?.toString().orEmpty()
        val fullText = listOf(title, body).filter { it.isNotBlank() }.joinToString("\n")

        if (fullText.isBlank()) return

        val packageName = sbn.packageName.lowercase()
        val isCourierRelated = packageName.contains("uber") ||
            packageName.contains("maxymo") ||
            fullText.contains("uber", ignoreCase = true) ||
            fullText.contains("maxymo", ignoreCase = true)

        if (!isCourierRelated) return

        val parsed = OfferParser.parse(fullText)
        val scored = OfferScorer.score(parsed)

        scope.launch {
            AppDatabase.get(applicationContext).offerDao().insert(
                OfferEntity(
                    source = "notification",
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
                    rawText = fullText
                )
            )
        }
    }
}
