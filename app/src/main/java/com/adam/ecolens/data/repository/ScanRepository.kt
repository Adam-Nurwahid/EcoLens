package com.adam.ecolens.data.repository

import com.adam.ecolens.data.local.dao.ScanHistoryDao
import com.adam.ecolens.data.local.entity.ScanHistoryEntity
import com.adam.ecolens.data.model.CategoryStat
import com.adam.ecolens.data.model.WasteCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ScanRepository(
    private val scanHistoryDao: ScanHistoryDao
) {

    suspend fun saveScan(username: String, categoryLabel: String, confidence: Float, imageUri: String? = null): Long = withContext(Dispatchers.IO) {
        val scan = ScanHistoryEntity(
            0,
            username,
            categoryLabel.lowercase().trim(),
            confidence,
            imageUri,
            System.currentTimeMillis()
        )
        return@withContext scanHistoryDao.insertScan(scan)
    }

    fun getScanHistory(username: String): Flow<List<ScanHistoryEntity>> {
        return scanHistoryDao.getHistoryByUsername(username)
    }

    fun getTotalScans(username: String): Flow<Int> {
        return scanHistoryDao.getTotalScansByUsername(username).map { it ?: 0 }
    }

    fun getCategoryStats(username: String): Flow<List<CategoryStat>> {
        return scanHistoryDao.getCategoryCountsByUsername(username).map { rawCounts ->
            val total = rawCounts.sumOf { it.count }
            if (total == 0) {
                listOf(
                    CategoryStat(WasteCategory.ORGANIK, 0, 0f),
                    CategoryStat(WasteCategory.ANORGANIK, 0, 0f),
                    CategoryStat(WasteCategory.B3, 0, 0f)
                )
            } else {
                val map = rawCounts.associate { it.category to it.count }
                listOf(WasteCategory.ORGANIK, WasteCategory.ANORGANIK, WasteCategory.B3).map { cat ->
                    val count = map[cat.id] ?: 0
                    val percentage = (count.toFloat() / total.toFloat()) * 100f
                    CategoryStat(cat, count, percentage)
                }
            }
        }
    }
}
