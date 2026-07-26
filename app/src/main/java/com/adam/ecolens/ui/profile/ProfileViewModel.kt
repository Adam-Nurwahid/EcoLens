package com.adam.ecolens.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.data.model.CategoryStat
import com.adam.ecolens.data.model.ScanRecord
import com.adam.ecolens.data.model.UserProfile
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.ScanRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for [ProfileFragment].
 * Fetches user profile, scan history, and category statistics from Firestore.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _scanHistory = MutableLiveData<List<ScanRecord>>(emptyList())
    val scanHistory: LiveData<List<ScanRecord>> = _scanHistory

    private val _categoryStats = MutableLiveData<List<CategoryStat>>(emptyList())
    val categoryStats: LiveData<List<CategoryStat>> = _categoryStats

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadProfileData()
    }

    /** Loads all profile-related data from Firestore in one coroutine. */
    fun loadProfileData() {
        val uid = authRepository.getUid() ?: return
        _isLoading.value = true
        viewModelScope.launch {
            // Fetch profile and scan history concurrently is possible but
            // sequential is simpler and sufficient for this screen
            _userProfile.value = authRepository.getUserProfile(uid)
            val history = scanRepository.getScanHistory(uid)
            _scanHistory.value = history
            _categoryStats.value = scanRepository.computeCategoryStats(history)
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
