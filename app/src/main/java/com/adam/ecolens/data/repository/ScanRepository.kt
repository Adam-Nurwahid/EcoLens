package com.adam.ecolens.data.repository

import com.adam.ecolens.data.local.dao.ScanFeedbackDao
import com.adam.ecolens.data.local.entity.ScanFeedbackEntity
import com.adam.ecolens.data.model.CategoryStat
import com.adam.ecolens.data.model.ScanRecord
import com.adam.ecolens.data.model.WasteCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for scan-related operations.
 * Cloud persistence is handled by [FirestoreRepository]; local feedback reports are
 * persisted via Room through [ScanFeedbackDao].
 */
class ScanRepository(
    private val firestoreRepository: FirestoreRepository,
    private val scanFeedbackDao: ScanFeedbackDao
) {

    /**
     * Saves a new scan result to Firestore for the given [uid].
     */
    suspend fun saveScan(uid: String, categoryLabel: String, confidence: Float, imageUri: String? = null) {
        firestoreRepository.saveScan(
            uid = uid,
            category = categoryLabel.lowercase().trim(),
            confidence = confidence,
            imageUri = imageUri
        )
    }

    /**
     * Fetches the full scan history from Firestore for [uid].
     * Returns the list sorted by most recent first.
     */
    suspend fun getScanHistory(uid: String): List<ScanRecord> {
        return firestoreRepository.getScanHistory(uid)
    }

    /**
     * Returns the total number of scans recorded for [uid].
     */
    suspend fun getTotalScans(uid: String): Int {
        return firestoreRepository.getScanCount(uid)
    }

    /**
     * Computes category breakdown statistics from a list of [ScanRecord]s.
     * Aggregated locally from the fetched list to avoid extra Firestore queries.
     */
    fun computeCategoryStats(records: List<ScanRecord>): List<CategoryStat> {
        val total = records.size
        if (total == 0) {
            return listOf(
                CategoryStat(WasteCategory.ORGANIK, 0, 0f),
                CategoryStat(WasteCategory.ANORGANIK, 0, 0f),
                CategoryStat(WasteCategory.B3, 0, 0f)
            )
        }
        val countMap = records.groupingBy { it.category.lowercase().trim() }.eachCount()
        return listOf(WasteCategory.ORGANIK, WasteCategory.ANORGANIK, WasteCategory.B3).map { cat ->
            val count = countMap[cat.id] ?: 0
            CategoryStat(cat, count, (count.toFloat() / total.toFloat()) * 100f)
        }
    }

    /**
     * Saves a user-submitted incorrect-prediction report to:
     *  - Room (local, always — works offline)
     *  - Firestore `scanFeedback` collection (cloud — for model improvement analysis)
     *
     * Both writes run in parallel; an individual failure in either does not
     * cancel the other. [uid] is the Firebase UID of the reporting user.
     *
     * @param uid               Firebase UID of the reporting user.
     * @param predictedCategory The label predicted by the AI model.
     * @param correctCategory   The category the user selected as correct (may be null).
     * @param note              Optional free-text note from the user.
     * @param imageUri          Optional URI of the scanned image.
     */
    suspend fun saveFeedback(
        uid: String,
        predictedCategory: String,
        correctCategory: String?,
        note: String?,
        imageUri: String?
    ) = withContext(Dispatchers.IO) {
        // Room — local, offline-safe
        val entity = ScanFeedbackEntity(
            0L,
            predictedCategory,
            correctCategory,
            note,
            imageUri,
            System.currentTimeMillis()
        )
        runCatching { scanFeedbackDao.insertFeedback(entity) }

        // Firestore — cloud, best-effort
        runCatching {
            firestoreRepository.saveScanFeedback(
                uid = uid,
                predictedCategory = predictedCategory,
                correctCategory = correctCategory,
                note = note,
                imageUri = imageUri
            )
        }
    }
}
