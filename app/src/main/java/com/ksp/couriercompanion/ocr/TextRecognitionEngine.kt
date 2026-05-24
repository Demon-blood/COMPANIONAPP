package com.ksp.couriercompanion.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class TextRecognitionEngine {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun read(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return recognizer.process(image).await().text
    }
}
