package com.adam.ecolens.data.model

import com.google.firebase.Timestamp

/**
 * Mirrors a Firestore document at users/{uid}/scanHistory/{docId}.
 * Replaces the local [com.adam.ecolens.data.local.entity.ScanHistoryEntity].
 *
 * Firestore structure:
 *   category   : String  (e.g. "organik", "anorganik", "b3")
 *   confidence : Number  (0.0–100.0)
 *   imageUri   : String? (nullable — local URI or null)
 *   timestamp  : Timestamp
 */
data class ScanRecord(
    val category: String = "",
    val confidence: Float = 0f,
    val imageUri: String? = null,
    val timestamp: Timestamp? = null
)
