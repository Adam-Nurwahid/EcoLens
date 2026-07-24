package com.adam.ecolens.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.AuthResult
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableLiveData<AuthResult?>()
    val loginState: LiveData<AuthResult?> = _loginState

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun login(username: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.login(username, password)
            _loginState.value = result
            _isLoading.value = false
        }
    }

    fun resetState() {
        _loginState.value = null
    }
}
