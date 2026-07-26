package com.adam.ecolens.data.repository

import com.adam.ecolens.data.model.LeaderboardItem
import com.adam.ecolens.data.model.QuizLevel
import com.adam.ecolens.data.model.QuizScore

/**
 * Repository for quiz-related operations.
 *
 * The question bank is now sourced from Firestore via [FirestoreRepository.getQuizLevelsFromFirestore]
 * and cached in-memory after the first successful fetch so Firestore is not re-queried on every
 * level navigation within the same app session.
 *
 * Persistence (scores, points, level unlock) continues to be handled by [FirestoreRepository].
 */
class QuizRepository(
    private val firestoreRepository: FirestoreRepository
) {

    // -----------------------------------------------------------------------
    // In-memory question bank cache (populated on first Firestore fetch)
    // -----------------------------------------------------------------------

    /** Null until the first successful fetch from Firestore. */
    private var cachedLevels: List<QuizLevel>? = null

    /**
     * Returns the cached level list, fetching from Firestore on the first call.
     * Subsequent calls within the same app session return the cached result immediately.
     *
     * @throws Exception if Firestore is unreachable and the cache is empty.
     */
    private suspend fun fetchAndCacheLevels(): List<QuizLevel> {
        cachedLevels?.let { return it }
        val levels = firestoreRepository.getQuizLevelsFromFirestore()
        cachedLevels = levels
        return levels
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Returns the [QuizLevel] with the given [levelId], or null if not found.
     * Fetches from Firestore on first access and caches for subsequent calls.
     *
     * @throws Exception if Firestore is unreachable and the cache is cold.
     */
    suspend fun getLevelById(levelId: Int): QuizLevel? =
        fetchAndCacheLevels().find { it.levelId == levelId }

    /**
     * Fetches Firestore quiz scores for [uid] and merges them with the Firestore-backed
     * level definitions to produce a fully enriched [QuizLevel] list.
     *
     * @throws Exception if the Firestore question bank fetch fails and the cache is cold.
     */
    suspend fun getLevels(uid: String): List<QuizLevel> {
        val baselevels = fetchAndCacheLevels()
        val scores: List<QuizScore> = firestoreRepository.getQuizScores(uid)
        val scoreMap = scores.associateBy { it.levelId }

        // Fetch current unlockedLevel from the user profile
        val profile = firestoreRepository.getUserProfile(uid)
        val unlockedLevel = profile?.unlockedLevel ?: 1

        return baselevels.map { level ->
            val userScore = scoreMap[level.levelId]
            level.copy(
                isUnlocked = level.levelId <= unlockedLevel,
                starsAchieved = userScore?.stars ?: 0
            )
        }
    }

    /**
     * Saves a quiz result to Firestore (best score kept) and updates
     * totalPoints + unlockedLevel atomically.
     *
     * Returns a [QuizScore] reflecting the best-ever result for this level.
     */
    suspend fun saveQuizResult(uid: String, levelId: Int, score: Int): QuizScore {
        val stars = when {
            score >= 90 -> 3
            score >= 70 -> 2
            score >= 50 -> 1
            else -> 0
        }

        // Use cached level count if available, fall back to 0 (safe — only used for unlock logic)
        val totalLevels = cachedLevels?.size ?: 0

        firestoreRepository.saveQuizScore(
            uid = uid,
            levelId = levelId,
            score = score,
            stars = stars,
            pointsEarned = score,     // points = final score value
            totalLevels = totalLevels
        )

        // Return the updated record (best score will be read from Firestore)
        return firestoreRepository.getQuizScores(uid)
            .find { it.levelId == levelId }
            ?: QuizScore(levelId = levelId, score = score, stars = stars)
    }

    /**
     * Fetches top 10 users from Firestore and maps to [LeaderboardItem].
     */
    suspend fun getLeaderboard(): List<LeaderboardItem> {
        return firestoreRepository.getLeaderboard().mapIndexed { index, entry ->
            LeaderboardItem(
                rank = index + 1,
                username = entry.uid,         // UID used as identifier
                fullName = entry.profile.name,
                totalPoints = entry.profile.totalPoints.toInt(),
                currentLevel = entry.profile.unlockedLevel
            )
        }
    }
}
