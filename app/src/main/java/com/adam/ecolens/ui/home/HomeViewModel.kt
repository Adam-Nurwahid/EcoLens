package com.adam.ecolens.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.data.model.UserProfile
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.ScanRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for [HomeFragment].
 * Loads user profile and total scan count from Firestore.
 */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _totalScans = MutableLiveData<Int>(0)
    val totalScans: LiveData<Int> = _totalScans

    init {
        // Load data as soon as the ViewModel is created
        loadData()
    }

    /** Fetches user profile and scan count from Firestore. */
    fun loadData() {
        val uid = authRepository.getUid() ?: return
        viewModelScope.launch {
            _userProfile.value = authRepository.getUserProfile(uid)
            _totalScans.value = scanRepository.getTotalScans(uid)
        }
    }

    fun getDailyTip(): String {
        val tips = listOf(
            "💡 Tahukah Kamu? Botol plastik bekas bisa didaur ulang menjadi serat kain untuk baju atau tas baru!",
            "🌱 Sampah kulit pisang dan sisa sayur bisa diolah menjadi pupuk kompos untuk menyuburkan tanah sekolah.",
            "⚠️ Jangan pernah membuang baterai bekas di tempat sampah biasa karena mengandung bahan kimia B3 beracun!",
            "♻️ Selalu bilas botol plastik atau kaleng minuman sebelum disetorkan ke bank sampah agar tidak bau.",
            "🎒 Gunakan tas kain ramah lingkungan saat berbelanja untuk mengurangi timbulan sampah plastik."
        )
        return tips.random()
    }
}
