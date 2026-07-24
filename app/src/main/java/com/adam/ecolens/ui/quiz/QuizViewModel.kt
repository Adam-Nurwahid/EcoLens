package com.adam.ecolens.ui.quiz

import androidx.lifecycle.ViewModel
import com.adam.ecolens.data.model.LeaderboardItem
import com.adam.ecolens.data.model.QuizLevel
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.QuizRepository
import kotlinx.coroutines.flow.Flow

class QuizViewModel(
    private val authRepository: AuthRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    fun getLevelsFlow(): Flow<List<QuizLevel>>? {
        val username = authRepository.getActiveUsername() ?: return null
        return quizRepository.getLevelsFlow(username)
    }

    fun getLeaderboardFlow(): Flow<List<LeaderboardItem>> {
        return quizRepository.getLeaderboard()
    }
}
