package com.adam.ecolens.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.data.local.entity.QuizScoreEntity
import com.adam.ecolens.data.model.Question
import com.adam.ecolens.data.model.QuizLevel
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.QuizRepository
import kotlinx.coroutines.launch

data class QuizCompletedState(
    val score: Int,
    val totalQuestions: Int,
    val stars: Int,
    val isLevelUnlocked: Boolean
)

class QuizPlayViewModel(
    private val authRepository: AuthRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private var currentLevel: QuizLevel? = null
    private var currentQuestionIndex = 0
    private var correctAnswersCount = 0

    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question> = _currentQuestion

    private val _questionProgressText = MutableLiveData<String>()
    val questionProgressText: LiveData<String> = _questionProgressText

    private val _selectedAnswerIndex = MutableLiveData<Int?>()
    val selectedAnswerIndex: LiveData<Int?> = _selectedAnswerIndex

    private val _isAnswerSubmitted = MutableLiveData<Boolean>(false)
    val isAnswerSubmitted: LiveData<Boolean> = _isAnswerSubmitted

    private val _quizCompletedState = MutableLiveData<QuizCompletedState?>()
    val quizCompletedState: LiveData<QuizCompletedState?> = _quizCompletedState

    fun loadLevel(levelId: Int) {
        currentLevel = quizRepository.getLevelById(levelId)
        currentQuestionIndex = 0
        correctAnswersCount = 0
        _quizCompletedState.value = null
        showCurrentQuestion()
    }

    private fun showCurrentQuestion() {
        val level = currentLevel ?: return
        if (currentQuestionIndex < level.questions.size) {
            val q = level.questions[currentQuestionIndex]
            _currentQuestion.value = q
            _questionProgressText.value = "Soal ${currentQuestionIndex + 1} dari ${level.questions.size}"
            _selectedAnswerIndex.value = null
            _isAnswerSubmitted.value = false
        } else {
            finishQuiz()
        }
    }

    fun selectAnswer(index: Int) {
        if (_isAnswerSubmitted.value == true) return
        _selectedAnswerIndex.value = index
    }

    fun submitAnswer() {
        val q = _currentQuestion.value ?: return
        val selected = _selectedAnswerIndex.value ?: return

        if (selected == q.correctAnswerIndex) {
            correctAnswersCount++
        }
        _isAnswerSubmitted.value = true
    }

    fun nextQuestion() {
        currentQuestionIndex++
        showCurrentQuestion()
    }

    private fun finishQuiz() {
        val level = currentLevel ?: return
        val total = level.questions.size
        val finalScore = ((correctAnswersCount.toFloat() / total.toFloat()) * 100).toInt()

        viewModelScope.launch {
            val username = authRepository.getActiveUsername() ?: "guest"
            val scoreEntity: QuizScoreEntity = quizRepository.saveQuizResult(username, level.levelId, finalScore)

            _quizCompletedState.value = QuizCompletedState(
                score = finalScore,
                totalQuestions = total,
                stars = scoreEntity.stars,
                isLevelUnlocked = finalScore >= level.minScoreToPass
            )
        }
    }
}
