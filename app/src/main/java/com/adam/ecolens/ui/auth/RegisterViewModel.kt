package com.adam.ecolens.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.AuthResult
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _registerState = MutableLiveData<AuthResult?>()
    val registerState: LiveData<AuthResult?> = _registerState

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(username: String, password: String, fullName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.register(username, password, fullName)
            _registerState.value = result
            _isLoading.value = false
        }
    }

    fun resetState() {
        _registerState.value = null
    }
}
