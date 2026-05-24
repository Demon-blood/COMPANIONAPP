package com.ksp.couriercompanion.ocr

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object OcrRuntimeStatus {
    private val _status = MutableStateFlow("OCR idle")
    val status: StateFlow<String> = _status

    fun update(message: String) {
        _status.value = message
    }
}
