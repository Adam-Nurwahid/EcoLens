package com.adam.ecolens.ui.home

import androidx.lifecycle.ViewModel
import com.adam.ecolens.data.local.entity.UserEntity
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.ScanRepository
import kotlinx.coroutines.flow.Flow

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    fun getActiveUserFlow(): Flow<UserEntity?>? {
        return authRepository.getActiveUserFlow()
    }

    fun getTotalScans(): Flow<Int>? {
        val username = authRepository.getActiveUsername() ?: return null
        return scanRepository.getTotalScans(username)
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
