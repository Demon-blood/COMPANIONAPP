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

class CourierNotificationListener: NotificationListenerService(){
    private val scope=CoroutineScope(Dispatchers.IO)
    override fun onNotificationPosted(sbn: StatusBarNotification){
        val pkg=sbn.packageName.lowercase()
        if (!listOf("uber","maxymo","eats").any{pkg.contains(it)}) return
        val extras=sbn.notification.extras
        val text=listOf(extras.getCharSequence("android.title"), extras.getCharSequence("android.text"), extras.getCharSequence("android.bigText"))
            .filterNotNull().joinToString("
")
        if(text.isBlank()) return
        scope.launch {
            val p=OfferParser.parse(text); val s=OfferScorer.score(p)
            AppDatabase.get(applicationContext).offerDao().insert(OfferEntity(source="notification", timestamp=System.currentTimeMillis(), restaurant=p.restaurant,
                pickupArea=p.pickupArea, dropoffArea=p.dropoffArea, offerAmount=p.payout, distanceKm=p.distanceKm, estimatedMinutes=p.minutes,
                accepted=p.accepted, completed=p.completed, score=s.score, recommendation=s.recommendation, latitude=null, longitude=null, rawText=text))
        }
    }
}
