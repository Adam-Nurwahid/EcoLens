package com.adam.ecolens.data.model

import com.google.firebase.Timestamp

/**
 * Mirrors a Firestore document at users/{uid}/activities/{activityId}.
 * Represents a single eco-friendly activity performed by the user.
 *
 * Firestore structure:
 * users/{uid}/activities/{activityId}
 *   ├── type      : String  (e.g. "buang_sampah", "hemat_air")
 *   ├── points    : Number
 *   └── timestamp : Timestamp
 */
data class EcoActivity(
    val type: String = "",
    val points: Int = 0,
    val timestamp: Timestamp? = null
)
