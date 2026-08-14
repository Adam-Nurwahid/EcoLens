package com.adam.ecolens.ui.onboarding


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.data.repository.AuthRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun submitOnboarding(answers: List<String>, bonusPoints: Int) {
        viewModelScope.launch {
            authRepository.saveOnboardingResult(answers, bonusPoints)
        }
    }
}