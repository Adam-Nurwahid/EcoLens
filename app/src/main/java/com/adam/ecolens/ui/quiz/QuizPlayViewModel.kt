package com.adam.ecolens.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.data.model.Question
import com.adam.ecolens.data.model.QuizLevel
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.QuizRepository
import kotlinx.coroutines.launch

data class QuizCompletedState(
    val score: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val isLevelUnlocked: Boolean
)

/**
 * ViewModel for the active quiz play screen.
 *
 * [loadLevel] is now async — it fetches the level from the Firestore-backed cache
 * (via [QuizRepository.getLevelById]) and exposes loading / error states so the
 * Fragment can show a spinner or a retry panel while the data arrives.
 *
 * Once loaded, quiz progression (question display, answer selection, scoring) runs
 * entirely in-memory with no additional network calls.
 *
 * Results are saved to Firestore via [QuizRepository.saveQuizResult].
 */
class QuizPlayViewModel(
    private val authRepository: AuthRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private var currentLevel: QuizLevel? = null
    private var currentQuestionIndex = 0
    private var correctAnswersCount = 0

    // Keep the requested levelId so the Fragment can call retry without re-passing args
    private var pendingLevelId: Int = -1

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

    /** True while the level is being loaded from Firestore (or cache). */
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Non-null when the level could not be loaded (e.g. no internet on first visit).
     * Null while loading or when data is successfully loaded.
     */
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Loads the quiz level with [levelId] from the in-memory cache or Firestore.
     * Call again (with the same levelId) to retry after an error.
     */
    fun loadLevel(levelId: Int) {
        pendingLevelId = levelId
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                currentLevel = quizRepository.getLevelById(levelId)
                currentQuestionIndex = 0
                correctAnswersCount = 0
                _quizCompletedState.value = null
                showCurrentQuestion()
            } catch (e: Exception) {
                _errorMessage.value =
                    "Soal tidak bisa dimuat. Periksa internetmu dan coba lagi! 🌐"
            } finally {
                _isLoading.value = false
            }
        }
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
        if (selected == q.correctAnswerIndex) correctAnswersCount++
        _isAnswerSubmitted.value = true
    }

    fun nextQuestion() {
        currentQuestionIndex++
        showCurrentQuestion()
    }

    private fun finishQuiz() {
        val level = currentLevel ?: return
        val total = level.questions.size
        val finalScore = if (total > 0) ((correctAnswersCount.toFloat() / total.toFloat()) * 100).toInt() else 0

        viewModelScope.launch {
            val uid = authRepository.getUid() ?: return@launch
            quizRepository.saveQuizResult(
                uid = uid,
                levelId = level.levelId,
                score = finalScore,
                correctCount = correctAnswersCount,
                totalQuestions = total
            )

            _quizCompletedState.value = QuizCompletedState(
                score = finalScore,
                correctCount = correctAnswersCount,
                totalQuestions = total,
                // Completing always unlocks the next level (Bug 1 fix)
                isLevelUnlocked = true
            )
        }
    }

    // tambahin ini, panggil dari Fragment setelah dialog di-dismiss
    fun consumeQuizCompletedState() {
        _quizCompletedState.value = null
    }
}
