package com.locup.mvp.ml

/**
 * Deterministic fallback scorer. Used when the TFLite asset is missing so the
 * app stays demoable. Also powers the feature vector for the real model
 * (hash-trick bag-of-words) so the same code path trains and infers.
 */
object KeywordFallback {

    private val seeds: Map<String, List<String>> = mapOf(
        "Emergency" to listOf("fire", "accident", "flood", "rescue", "police", "ambulance", "waterlogging", "stuck", "danger"),
        "Traffic" to listOf("traffic", "jam", "roadblock", "closed", "congestion", "delay", "lane", "signal"),
        "Event" to listOf("festival", "concert", "yoga", "match", "celebration", "fair", "weekend", "workshop"),
        "Civic" to listOf("garbage", "streetlight", "pothole", "potholes", "water", "power", "sewage", "drain", "smell"),
        "General" to listOf("hello", "hi", "anyone", "lost", "found", "thanks", "shoutout")
    )

    fun score(text: String, labels: List<String>, modelName: String = "Keyword fallback (local)"): LocalClassifier.Classification {
        val tokens = text.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }
        val raw = labels.associateWith { label ->
            val seed = seeds[label].orEmpty()
            val hits = tokens.count { it in seed }
            (hits + 0.001f) // smoothing
        }
        val total = raw.values.sum()
        val scores = labels.map { (raw[it] ?: 0f) / total }
        return LocalClassifier.Classification(labels, scores, modelName)
    }

    fun featureVector(text: String, dim: Int): FloatArray {
        val v = if (dim <= 0) FloatArray(64) else FloatArray(dim)
        val tokens = text.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }
        for (t in tokens) {
            val idx = (t.hashCode() and 0x7FFFFFFF) % v.size
            v[idx] += 1f
        }
        return v
    }
}
