package com.adam.ecolens.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.FirebaseAuthResult
import kotlinx.coroutines.launch

/**
 * ViewModel for [LoginFragment].
 * Coordinates the Firebase Anonymous sign-in flow — the user enters only
 * their display name; no password or email is required.
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableLiveData<FirebaseAuthResult?>()
    val loginState: LiveData<FirebaseAuthResult?> = _loginState

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /** Returns true if there is an existing local session (skip login). */
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    /**
     * Triggers anonymous sign-in with the given display [name].
     * Posts the result to [loginState] for the Fragment to observe.
     */
    fun signInAnonymously(name: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.signInAnonymously(name)
            _loginState.value = result
            _isLoading.value = false
        }
    }

    fun resetState() {
        _loginState.value = null
    }
}
