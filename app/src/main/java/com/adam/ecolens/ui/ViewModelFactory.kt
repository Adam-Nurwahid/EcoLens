package com.adam.ecolens.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adam.ecolens.data.local.AppDatabase
import com.adam.ecolens.data.local.SessionManager
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.EducationRepository
import com.adam.ecolens.data.repository.FirestoreRepository
import com.adam.ecolens.data.repository.QuizRepository
import com.adam.ecolens.data.repository.ScanRepository
import com.adam.ecolens.ml.ImageClassifierHelper
import com.adam.ecolens.ui.auth.LoginViewModel
import com.adam.ecolens.ui.auth.RegisterViewModel
import com.adam.ecolens.ui.home.HomeViewModel
import com.adam.ecolens.ui.learn.LearnViewModel
import com.adam.ecolens.ui.profile.ProfileViewModel
import com.adam.ecolens.ui.quiz.QuizPlayViewModel
import com.adam.ecolens.ui.quiz.QuizViewModel
import com.adam.ecolens.ui.scan.ScanViewModel

/**
 * Single ViewModelProvider.Factory for all ViewModels in EcoLens.
 * All repositories are singletons (lazy) to avoid unnecessary re-creation.
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val db by lazy { AppDatabase.getDatabase(context) }
    private val sessionManager by lazy { SessionManager(context) }

    // Firestore — single source of truth for all cloud data
    private val firestoreRepository by lazy { FirestoreRepository() }

    // Auth — passes FirestoreRepository to create user docs on login
    private val authRepository by lazy { AuthRepository(db.userDao(), sessionManager, firestoreRepository) }

    // Scan & Quiz now backed by Firestore instead of Room
    private val scanRepository by lazy { ScanRepository(firestoreRepository) }
    private val quizRepository by lazy { QuizRepository(firestoreRepository) }

    private val educationRepository by lazy { EducationRepository() }
    private val imageClassifierHelper by lazy { ImageClassifierHelper(context) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(authRepository, scanRepository) as T
            }
            modelClass.isAssignableFrom(LearnViewModel::class.java) -> {
                LearnViewModel(educationRepository) as T
            }
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> {
                ScanViewModel(authRepository, scanRepository, imageClassifierHelper) as T
            }
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> {
                QuizViewModel(authRepository, quizRepository) as T
            }
            modelClass.isAssignableFrom(QuizPlayViewModel::class.java) -> {
                QuizPlayViewModel(authRepository, quizRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(authRepository, scanRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
