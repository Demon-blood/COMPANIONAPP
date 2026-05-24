package com.ksp.couriercompanion.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import com.ksp.couriercompanion.ocr.DuplicateOfferFilter
import com.ksp.couriercompanion.ocr.OcrOfferProcessor
import com.ksp.couriercompanion.ocr.OcrRuntimeStatus
import com.ksp.couriercompanion.ocr.TextRecognitionEngine
import com.ksp.couriercompanion.util.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenCaptureService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val recognizer by lazy { TextRecognitionEngine() }
    private val duplicateFilter = DuplicateOfferFilter()

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var captureWidth: Int = 720
    private var captureHeight: Int = 1280
    private var captureDensity: Int = DisplayMetrics.DENSITY_DEFAULT

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            1002,
            Notifications.foreground(this, "Courier Companion", "Live screen OCR active")
        )

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

        if (resultCode != 0 && resultData != null && mediaProjection == null) {
            val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = manager.getMediaProjection(resultCode, resultData)
            setupCaptureSurface()
            startOcrLoop()
        }

        return START_STICKY
    }

    private fun setupCaptureSurface() {
        val metrics = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        captureWidth = metrics.widthPixels.coerceAtMost(1080)
        captureHeight = metrics.heightPixels.coerceAtMost(1920)
        captureDensity = metrics.densityDpi

        imageReader = ImageReader.newInstance(
            captureWidth,
            captureHeight,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "CourierCompanionLiveOCR",
            captureWidth,
            captureHeight,
            captureDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        OcrRuntimeStatus.update("OCR capture ready: ${captureWidth}x$captureHeight")
    }

    private fun startOcrLoop() {
        scope.launch {
            while (isActive && mediaProjection != null) {
                try {
                    val bitmap = captureLatestBitmap()
                    if (bitmap != null) {
                        val text = recognizer.read(bitmap)
                        bitmap.recycle()

                        if (text.isNotBlank() && duplicateFilter.shouldAccept(text)) {
                            val insertedId = OcrOfferProcessor.processVisibleText(applicationContext, text)
                            if (insertedId != null) {
                                OcrRuntimeStatus.update("Offer detected from screen OCR")
                            } else {
                                OcrRuntimeStatus.update("OCR read screen, no courier offer detected")
                            }
                        }
                    } else {
                        OcrRuntimeStatus.update("Waiting for screen frame...")
                    }
                } catch (error: Throwable) {
                    OcrRuntimeStatus.update("OCR error: ${error.message ?: error.javaClass.simpleName}")
                }

                delay(5_000L)
            }
        }
    }

    private suspend fun captureLatestBitmap(): Bitmap? = withContext(Dispatchers.Default) {
        val reader = imageReader ?: return@withContext null
        val image = reader.acquireLatestImage() ?: return@withContext null

        try {
            val plane = image.planes.firstOrNull() ?: return@withContext null
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * captureWidth
            val bitmapWidth = captureWidth + rowPadding / pixelStride

            val rawBitmap = Bitmap.createBitmap(bitmapWidth, captureHeight, Bitmap.Config.ARGB_8888)
            rawBitmap.copyPixelsFromBuffer(buffer)

            if (bitmapWidth == captureWidth) {
                rawBitmap
            } else {
                val cropped = Bitmap.createBitmap(rawBitmap, 0, 0, captureWidth, captureHeight)
                rawBitmap.recycle()
                cropped
            }
        } finally {
            image.close()
        }
    }

    override fun onDestroy() {
        virtualDisplay?.release()
        virtualDisplay = null

        imageReader?.close()
        imageReader = null

        mediaProjection?.stop()
        mediaProjection = null

        scope.cancel()
        OcrRuntimeStatus.update("OCR stopped")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
    }
}
