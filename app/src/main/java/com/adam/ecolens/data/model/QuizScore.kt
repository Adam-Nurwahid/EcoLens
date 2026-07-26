package com.adam.ecolens.data.model

import com.google.firebase.Timestamp

/**
 * Mirrors a Firestore document at users/{uid}/quizScores/{levelId}.
 * Document ID = level number as String ("1", "2", "3").
 * Replaces the local [com.adam.ecolens.data.local.entity.QuizScoreEntity].
 *
 * Firestore structure:
 *   levelId     : Number
 *   score       : Number  (best score achieved, 0–100)
 *   stars       : Number  (0–3)
 *   completedAt : Timestamp
 */
data class QuizScore(
    val levelId: Int = 0,
    val score: Int = 0,
    val stars: Int = 0,
    val completedAt: Timestamp? = null
)
