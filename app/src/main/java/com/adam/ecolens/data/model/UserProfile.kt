package com.adam.ecolens.data.model

import com.google.firebase.Timestamp

/**
 * Mirrors the Firestore document at users/{uid}.
 * Used for reading/writing user profile data from Firestore.
 *
 * Firestore structure:
 * users/{uid}
 *   ├── name           : String
 *   ├── totalPoints    : Number (default 0)
 *   ├── unlockedLevel  : Number (default 1)
 *   └── createdAt      : Timestamp
 */
data class UserProfile(
    val name: String = "",
    val totalPoints: Long = 0L,
    val unlockedLevel: Int = 1,
    val createdAt: Timestamp? = null
)
