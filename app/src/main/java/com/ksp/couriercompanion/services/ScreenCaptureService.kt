package com.ksp.couriercompanion.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import com.ksp.couriercompanion.util.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScreenCaptureService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var mediaProjection: MediaProjection? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            1002,
            Notifications.foreground(this, "Courier Companion", "Screen OCR monitor active")
        )

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

        if (resultCode != 0 && resultData != null && mediaProjection == null) {
            val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = manager.getMediaProjection(resultCode, resultData)

            /*
             * Phase 2 stable implementation note:
             * The service now correctly receives and owns the Android MediaProjection session.
             * The actual ImageReader/VirtualDisplay frame extraction is intentionally isolated for the next build
             * so this APK remains stable while permission flow, foreground service, overlay, DB, and parser paths work.
             */
            startOcrSchedulerPlaceholder()
        }

        return START_STICKY
    }

    private fun startOcrSchedulerPlaceholder() {
        scope.launch {
            while (mediaProjection != null) {
                // Placeholder interval for the next module:
                // VirtualDisplay -> ImageReader -> Bitmap -> TextRecognitionEngine -> OcrOfferProcessor.
                delay(10_000L)
            }
        }
    }

    override fun onDestroy() {
        mediaProjection?.stop()
        mediaProjection = null
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
    }
}
