package com.adam.ecolens.data.model

data class Question(
    val id: Int = 0,
    val order: Int = 0,
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val explanation: String = ""
)

data class QuizLevel(
    val levelId: Int,
    val title: String,
    val subtitle: String,
    val questions: List<Question>,
    val isUnlocked: Boolean = false,
    /** Number of questions answered correctly in the best attempt (same as starsAchieved). */
    val correctCount: Int = 0,
    /** Total questions in the best attempt stored in Firestore (0 if never attempted). */
    val totalQuestionsAttempted: Int = 0,
    @Deprecated("Use correctCount / totalQuestionsAttempted for display logic")
    val starsAchieved: Int = 0,
    val minScoreToPass: Int = 70
)

data class LeaderboardItem(
    val rank: Int,
    val username: String,
    val fullName: String,
    val totalPoints: Int,
    val currentLevel: Int
)
