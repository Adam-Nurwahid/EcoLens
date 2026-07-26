package com.adam.ecolens.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.data.model.LeaderboardItem
import com.adam.ecolens.data.model.QuizLevel
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.QuizRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Quiz list screen.
 * Loads the Firestore question bank, applies per-user unlock/star state,
 * and fetches the leaderboard. Exposes loading and error states via LiveData.
 */
class QuizViewModel(
    private val authRepository: AuthRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _levels = MutableLiveData<List<QuizLevel>>(emptyList())
    val levels: LiveData<List<QuizLevel>> = _levels

    private val _leaderboard = MutableLiveData<List<LeaderboardItem>>(emptyList())
    val leaderboard: LiveData<List<LeaderboardItem>> = _leaderboard

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /** Non-null when an error has occurred; null when data is loading or successfully loaded. */
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadData()
    }

    fun loadData() {
        val uid = authRepository.getUid() ?: return
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                _levels.value = quizRepository.getLevels(uid)
                _leaderboard.value = quizRepository.getLeaderboard()
            } catch (e: Exception) {
                _errorMessage.value = "Ups! Soal tidak bisa dimuat. Periksa internetmu dan coba lagi ya! 😊"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
