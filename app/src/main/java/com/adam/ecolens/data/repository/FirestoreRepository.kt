package com.adam.ecolens.data.repository

import com.adam.ecolens.data.model.EcoActivity
import com.adam.ecolens.data.model.Question
import com.adam.ecolens.data.model.QuizLevel
import com.adam.ecolens.data.model.QuizScore
import com.adam.ecolens.data.model.ScanRecord
import com.adam.ecolens.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Central repository for ALL Firestore read/write operations.
 *
 * Full Firestore structure managed here:
 *   users/{uid}                          ← UserProfile
 *   users/{uid}/scanHistory/{docId}      ← ScanRecord
 *   users/{uid}/quizScores/{levelId}     ← QuizScore  (docId = levelId.toString())
 *   users/{uid}/activities/{actId}       ← EcoActivity (eco-friendly activities)
 *
 * All functions are suspend — call from viewModelScope or another suspend context.
 */
class FirestoreRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // -----------------------------------------------------------------------
    // User Profile
    // -----------------------------------------------------------------------

    /**
     * Creates or updates the users/{uid} document.
     * First login → full document created with defaults.
     * Returning login → only [name] is updated (preserves points, level, etc.)
     */
    suspend fun createOrUpdateUser(uid: String, name: String) {
        val userRef = db.collection("users").document(uid)
        val snapshot = userRef.get().await()

        if (!snapshot.exists()) {
            // First login — initialise all fields
            val newProfile = hashMapOf(
                "name" to name,
                "totalPoints" to 0,
                "unlockedLevel" to 1,
                "createdAt" to Timestamp.now()
            )
            userRef.set(newProfile).await()
        } else {
            // Returning user — only refresh display name
            userRef.set(mapOf("name" to name), SetOptions.merge()).await()
        }
    }

    /**
     * One-shot fetch of the user profile. Returns null if the document
     * doesn't exist or an error occurs.
     */
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // -----------------------------------------------------------------------
    // Scan History
    // -----------------------------------------------------------------------

    /**
     * Saves a new scan result to users/{uid}/scanHistory.
     * Each call creates a new auto-ID document.
     */
    suspend fun saveScan(uid: String, category: String, confidence: Float, imageUri: String? = null) {
        val data = hashMapOf(
            "category" to category,
            "confidence" to confidence,
            "imageUri" to imageUri,
            "timestamp" to Timestamp.now()
        )
        db.collection("users").document(uid)
            .collection("scanHistory")
            .add(data)
            .await()
    }

    /**
     * Fetches the full scan history for [uid], ordered by most recent first.
     * Returns an empty list on error.
     */
    suspend fun getScanHistory(uid: String): List<ScanRecord> {
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("scanHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(ScanRecord::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Returns the total number of scans for [uid].
     */
    suspend fun getScanCount(uid: String): Int {
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("scanHistory")
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    // -----------------------------------------------------------------------
    // Quiz Scores
    // -----------------------------------------------------------------------

    /**
     * Saves or updates the quiz score for a given [levelId].
     * Document ID = levelId.toString() so each level has exactly one record.
     *
     * Logic:
     * - Keeps the BEST score and BEST stars achieved across multiple attempts.
     * - Atomically increments totalPoints by [pointsEarned].
     * - If [score] >= 70 and there is a next level, unlocks it.
     */
    suspend fun saveQuizScore(uid: String, levelId: Int, score: Int, stars: Int, pointsEarned: Int, totalLevels: Int) {
        val userRef = db.collection("users").document(uid)
        val scoreRef = userRef.collection("quizScores").document(levelId.toString())

        // Read existing best score to keep the highest
        val existing = try {
            scoreRef.get().await().toObject(QuizScore::class.java)
        } catch (e: Exception) {
            null
        }

        val bestScore = maxOf(existing?.score ?: 0, score)
        val bestStars = maxOf(existing?.stars ?: 0, stars)

        val scoreData = hashMapOf(
            "levelId" to levelId,
            "score" to bestScore,
            "stars" to bestStars,
            "completedAt" to Timestamp.now()
        )

        // Batch: upsert score + increment totalPoints on user doc
        db.runBatch { batch ->
            batch.set(scoreRef, scoreData)
            batch.update(userRef, "totalPoints", FieldValue.increment(pointsEarned.toLong()))

            // Unlock next level if score passes threshold and not last level
            if (score >= 70 && levelId < totalLevels) {
                batch.update(userRef, "unlockedLevel", FieldValue.increment(1))
            }
        }.await()
    }

    /**
     * Fetches all quiz scores for [uid]. Returns empty list on error.
     */
    suspend fun getQuizScores(uid: String): List<QuizScore> {
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("quizScores")
                .get()
                .await()
            snapshot.toObjects(QuizScore::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // -----------------------------------------------------------------------
    // Eco Activities (general eco-friendly actions, separate from quiz/scan)
    // -----------------------------------------------------------------------

    /**
     * Logs an eco-friendly activity and atomically increments totalPoints.
     * Uses [FieldValue.increment] to prevent race conditions.
     */
    suspend fun addActivity(uid: String, type: String, points: Int) {
        val userRef = db.collection("users").document(uid)
        val activitiesRef = userRef.collection("activities")

        val activityData = hashMapOf(
            "type" to type,
            "points" to points,
            "timestamp" to Timestamp.now()
        )

        db.runBatch { batch ->
            batch.set(activitiesRef.document(), activityData)
            batch.update(userRef, "totalPoints", FieldValue.increment(points.toLong()))
        }.await()
    }

    /**
     * Fetches all eco activities for [uid], ordered most recent first.
     */
    suspend fun getActivities(uid: String): List<EcoActivity> {
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("activities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(EcoActivity::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // -----------------------------------------------------------------------
    // Leaderboard
    // -----------------------------------------------------------------------

    /**
     * Fetches top 10 users ordered by totalPoints descending.
     * Returns a list of [UserProfile] with the document UID accessible
     * via the wrapped [LeaderboardEntry].
     */
    suspend fun getLeaderboard(): List<LeaderboardEntry> {
        return try {
            val snapshot = db.collection("users")
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            snapshot.documents.map { doc ->
                val profile = doc.toObject(UserProfile::class.java) ?: UserProfile()
                LeaderboardEntry(uid = doc.id, profile = profile)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // -----------------------------------------------------------------------
    // Quiz Level Bank (question content from Firestore)
    // -----------------------------------------------------------------------

    /**
     * Fetches all quiz levels and their questions from the `quiz_levels` collection.
     *
     * Structure expected in Firestore:
     *   quiz_levels/{levelId}               ← level metadata
     *   quiz_levels/{levelId}/questions/{q} ← question documents
     *
     * Does NOT apply per-user unlock/star state — that is layered on in [QuizRepository.getLevels].
     * Exceptions propagate to the caller; wrap in try/catch at the ViewModel layer.
     */
    suspend fun getQuizLevelsFromFirestore(): List<QuizLevel> {
        val levelsSnapshot = db.collection("quiz_levels")
            .orderBy("levelId", Query.Direction.ASCENDING)
            .get()
            .await()

        return levelsSnapshot.documents.map { levelDoc ->
            val levelId = (levelDoc.getLong("levelId") ?: 0L).toInt()
            val title = levelDoc.getString("title") ?: ""
            val subtitle = levelDoc.getString("subtitle") ?: ""

            val questionsSnapshot = levelDoc.reference
                .collection("questions")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            val questions = questionsSnapshot.documents.mapNotNull { qDoc ->
                qDoc.toObject(Question::class.java)
            }

            QuizLevel(
                levelId = levelId,
                title = title,
                subtitle = subtitle,
                questions = questions
            )
        }
    }
}

/** Wrapper pairing a Firebase UID with its UserProfile for leaderboard display. */
data class LeaderboardEntry(
    val uid: String,
    val profile: UserProfile
)
