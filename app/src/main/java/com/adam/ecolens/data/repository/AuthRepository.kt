package com.adam.ecolens.data.repository

import com.adam.ecolens.data.local.SessionManager
import com.adam.ecolens.data.local.dao.UserDao
import com.adam.ecolens.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun getActiveUsername(): String? = sessionManager.getUsername()

    fun getActiveUserFlow(): Flow<UserEntity?>? {
        val username = getActiveUsername() ?: return null
        return userDao.observeUser(username)
    }

    suspend fun login(username: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.isEmpty() || password.isEmpty()) {
            return@withContext AuthResult.Error("Username dan password tidak boleh kosong.")
        }

        val user = userDao.getUserByUsername(cleanUsername)
            ?: return@withContext AuthResult.Error("Akun tidak ditemukan. Silakan daftar terlebih dahulu.")

        if (user.password != password) {
            return@withContext AuthResult.Error("Password yang Anda masukkan salah.")
        }

        sessionManager.createLoginSession(user.username, user.fullName)
        return@withContext AuthResult.Success(user)
    }

    suspend fun register(username: String, password: String, fullName: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val cleanName = fullName.trim()

        if (cleanUsername.isEmpty() || password.isEmpty() || cleanName.isEmpty()) {
            return@withContext AuthResult.Error("Semua kolom harus diisi.")
        }

        if (cleanUsername.length < 3) {
            return@withContext AuthResult.Error("Username minimal 3 karakter.")
        }

        val existingUser = userDao.getUserByUsername(cleanUsername)
        if (existingUser != null) {
            return@withContext AuthResult.Error("Username sudah terdaftar. Gunakan username lain.")
        }

        val newUser = UserEntity(
            cleanUsername,
            password,
            cleanName,
            0,
            1
        )

        userDao.insertUser(newUser)
        sessionManager.createLoginSession(newUser.username, newUser.fullName)
        return@withContext AuthResult.Success(newUser)
    }

    fun logout() {
        sessionManager.logout()
    }
}
