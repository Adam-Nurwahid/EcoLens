package com.adam.ecolens.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ecolens_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
        // Firebase UID for anonymous auth — stored so we can skip login on relaunch
        private const val KEY_UID = "firebase_uid"
        // Onboarding — set to true once the user completes the baseline-knowledge survey
        private const val KEY_ONBOARDING_DONE = "has_completed_onboarding"
        private const val KEY_ONBOARDING_ANSWERS = "onboarding_answers"
        // Dashboard walkthrough — shown once, right after onboarding completes (or first home visit)
        private const val KEY_TUTORIAL_DONE = "has_completed_tutorial"
    }

    /** Original session creator (Room-based users) — kept for backward compatibility. */
    fun createLoginSession(username: String, fullName: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            apply()
        }
    }

    /**
     * Session creator for Firebase Anonymous Auth.
     * Persists the Firebase UID alongside the display name so the app can
     * skip the login screen on subsequent launches.
     */
    fun createAnonymousSession(uid: String, displayName: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_UID, uid)
            putString(KEY_FULL_NAME, displayName)
            // Store name also as "username" so existing Home/Profile screens work unchanged
            putString(KEY_USERNAME, displayName)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, "Siswa EcoLens")

    /** Returns the Firebase UID, or null if the user logged in via the old Room flow. */
    fun getUid(): String? = prefs.getString(KEY_UID, null)

    // ------------------------------------------------------------------
    // Onboarding
    // ------------------------------------------------------------------

    /** Returns true if the user has already completed the onboarding survey. */
    fun hasCompletedOnboarding(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    /**
     * Marks onboarding as completed and persists the user's answers.
     * [answers] is a pipe-separated string of selected answer labels,
     * e.g. "Ya, tahu|Kadang-kadang|Jarang|Pernah dengar|Belajar tentang lingkungan".
     */
    fun setOnboardingCompleted(answers: String = "") {
        prefs.edit().apply {
            putBoolean(KEY_ONBOARDING_DONE, true)
            if (answers.isNotEmpty()) putString(KEY_ONBOARDING_ANSWERS, answers)
            apply()
        }
    }

    // ------------------------------------------------------------------
    // Dashboard walkthrough
    // ------------------------------------------------------------------

    /** Returns true if the user has already seen the one-time Dashboard coach-marks tutorial. */
    fun hasTutorialCompleted(): Boolean = prefs.getBoolean(KEY_TUTORIAL_DONE, false)

    /** Marks the Dashboard tutorial as seen so it is never shown again. */
    fun setTutorialCompleted() {
        prefs.edit().putBoolean(KEY_TUTORIAL_DONE, true).apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
