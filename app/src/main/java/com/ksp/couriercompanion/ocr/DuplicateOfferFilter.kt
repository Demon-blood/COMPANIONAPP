package com.ksp.couriercompanion.ocr

import java.security.MessageDigest

class DuplicateOfferFilter(
    private val windowMs: Long = 45_000L
) {
    private val seen = LinkedHashMap<String, Long>()

    fun shouldAccept(rawText: String, now: Long = System.currentTimeMillis()): Boolean {
        val normalized = rawText
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.length < 8) return false

        val hash = sha256(normalized)
        cleanup(now)

        val previous = seen[hash]
        if (previous != null && now - previous < windowMs) {
            return false
        }

        seen[hash] = now
        return true
    }

    private fun cleanup(now: Long) {
        val iterator = seen.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value > windowMs) {
                iterator.remove()
            }
        }
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
