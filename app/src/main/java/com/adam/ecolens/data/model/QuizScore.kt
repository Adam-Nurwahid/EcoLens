package com.adam.ecolens.data.model

import com.google.firebase.Timestamp

/**
 * Mirrors a Firestore document at users/{uid}/quizScores/{levelId}.
 * Document ID = level number as String ("1", "2", "3").
 *
 * Firestore structure:
 *   levelId        : Number
 *   score          : Number   (best score achieved, 0–100)
 *   stars          : Number   (= correctCount, kept for backward-compat)
 *   correctCount   : Number   (questions answered correctly in the best attempt)
 *   totalQuestions : Number   (total questions in this level)
 *   completedAt    : Timestamp
 */
data class QuizScore(
    val levelId: Int = 0,
    val score: Int = 0,
    /** Backward-compat alias for correctCount (written as correctCount going forward). */
    val stars: Int = 0,
    /** Number of questions answered correctly in the best attempt. */
    val correctCount: Int = 0,
    /** Total number of questions in this level. */
    val totalQuestions: Int = 0,
    val completedAt: Timestamp? = null
)
