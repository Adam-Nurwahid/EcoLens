package com.adam.ecolens.ui.profile

import androidx.lifecycle.ViewModel
import com.adam.ecolens.data.local.entity.ScanHistoryEntity
import com.adam.ecolens.data.local.entity.UserEntity
import com.adam.ecolens.data.model.CategoryStat
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.ScanRepository
import kotlinx.coroutines.flow.Flow

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    fun getActiveUserFlow(): Flow<UserEntity?>? {
        return authRepository.getActiveUserFlow()
    }

    fun getScanHistoryFlow(): Flow<List<ScanHistoryEntity>>? {
        val username = authRepository.getActiveUsername() ?: return null
        return scanRepository.getScanHistory(username)
    }

    fun getCategoryStatsFlow(): Flow<List<CategoryStat>>? {
        val username = authRepository.getActiveUsername() ?: return null
        return scanRepository.getCategoryStats(username)
    }

    fun logout() {
        authRepository.logout()
    }
}
