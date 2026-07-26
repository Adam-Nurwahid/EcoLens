package com.adam.ecolens.data.repository

import com.adam.ecolens.data.model.CategoryStat
import com.adam.ecolens.data.model.ScanRecord
import com.adam.ecolens.data.model.WasteCategory

/**
 * Repository for scan-related operations.
 * All persistence is now handled by [FirestoreRepository] — Room is no longer used
 * for scan data. The class is kept as a thin delegation layer so ViewModel
 * call-sites do not need to change their dependency references.
 */
class ScanRepository(
    private val firestoreRepository: FirestoreRepository
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
}
