package com.ksp.couriercompanion.services
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import com.ksp.couriercompanion.data.AppDatabase
import com.ksp.couriercompanion.data.OfferEntity
import com.ksp.couriercompanion.ocr.TextRecognitionEngine
import com.ksp.couriercompanion.scoring.OfferParser
import com.ksp.couriercompanion.scoring.OfferScorer
import com.ksp.couriercompanion.util.Notifications
import kotlinx.coroutines.*

class ScreenCaptureService: Service(){
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
    private val ocr=TextRecognitionEngine()
    override fun onCreate(){ super.onCreate(); startForeground(30, Notifications.service(this,"Courier Companion","Screen OCR service ready")) }
    override fun onStartCommand(intent:Intent?, flags:Int, startId:Int):Int{
        // Placeholder for MediaProjection virtual display setup. The project is wired for it; add the resultCode/data from MainActivity when enabling real capture.
        return START_STICKY
    }
    suspend fun processBitmap(bitmap: Bitmap){
        val text=ocr.read(bitmap); if(text.isBlank()) return
        val p=OfferParser.parse(text); val s=OfferScorer.score(p)
        AppDatabase.get(applicationContext).offerDao().insert(OfferEntity(source="ocr", timestamp=System.currentTimeMillis(), restaurant=p.restaurant,
            pickupArea=p.pickupArea, dropoffArea=p.dropoffArea, offerAmount=p.payout, distanceKm=p.distanceKm, estimatedMinutes=p.minutes,
            accepted=p.accepted, completed=p.completed, score=s.score, recommendation=s.recommendation, latitude=null, longitude=null, rawText=text))
    }
    override fun onDestroy(){ scope.cancel(); super.onDestroy() }
    override fun onBind(intent:Intent?):IBinder?=null
}
