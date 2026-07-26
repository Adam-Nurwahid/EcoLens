package com.adam.ecolens.data.repository

import com.adam.ecolens.data.local.SessionManager
import com.adam.ecolens.data.local.dao.UserDao
import com.adam.ecolens.data.local.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/** Result type specifically for the Firebase anonymous sign-in flow. */
sealed class FirebaseAuthResult {
    data class Success(val uid: String, val displayName: String) : FirebaseAuthResult()
    data class Error(val message: String) : FirebaseAuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    // FirestoreRepository is optional — null-safe so existing callers don't break
    private val firestoreRepository: FirestoreRepository? = null
) {

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun getActiveUsername(): String? = sessionManager.getUsername()

    fun getActiveUserFlow(): Flow<UserEntity?>? {
        val username = getActiveUsername() ?: return null
        return userDao.observeUser(username)
    }

    /** Returns the Firebase UID stored in the local session, or null if not signed in. */
    fun getUid(): String? = sessionManager.getUid()

    /** Fetches the Firestore user profile for the current UID. Returns null if not signed in. */
    suspend fun getUserProfile(uid: String): com.adam.ecolens.data.model.UserProfile? {
        return firestoreRepository?.getUserProfile(uid)
    }

    // ------------------------------------------------------------------
    // Firebase Anonymous Authentication (new flow)
    // ------------------------------------------------------------------

    /**
     * Signs the user in anonymously via Firebase Auth, then:
     *  1. Saves the session locally (UID + display name) via [SessionManager]
     *  2. Creates/updates the Firestore users/{uid} document
     *
     * This function is safe to call repeatedly — existing Firestore data is
     * preserved thanks to [FirestoreRepository.createOrUpdateUser] using merge.
     */
    suspend fun signInAnonymously(name: String): FirebaseAuthResult = withContext(Dispatchers.IO) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return@withContext FirebaseAuthResult.Error("Nama tidak boleh kosong.")
        }

        return@withContext try {
            // 1. Firebase Anonymous sign-in — returns immediately if already signed in
            val authResult = FirebaseAuth.getInstance().signInAnonymously().await()
            val uid = authResult.user?.uid
                ?: return@withContext FirebaseAuthResult.Error("Gagal mendapatkan UID dari Firebase.")

            // 2. Persist session locally so the app skips login on next launch
            sessionManager.createAnonymousSession(uid, cleanName)

            // 3. Upsert Firestore user document (no-op if already exists, safe to re-call)
            firestoreRepository?.createOrUpdateUser(uid, cleanName)

            FirebaseAuthResult.Success(uid, cleanName)
        } catch (e: Exception) {
            FirebaseAuthResult.Error("Login gagal: ${e.localizedMessage}")
        }
    }

    // ------------------------------------------------------------------
    // Legacy Room-based auth (kept for backward compatibility)
    // ------------------------------------------------------------------

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
        // Sign out of Firebase Auth as well
        FirebaseAuth.getInstance().signOut()
        sessionManager.logout()
    }
}
