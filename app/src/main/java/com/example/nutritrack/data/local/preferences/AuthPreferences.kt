package com.example.nutritrack.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "nutritrack_auth_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
    }

    fun saveLoginState(userId: String, email: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
        android.util.Log.d("AuthPreferences", "✅ Login state saved: $email")
    }

    fun clearLoginState() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        android.util.Log.d("AuthPreferences", "🔓 Login state cleared")
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit()
            .putBoolean(KEY_HAS_COMPLETED_ONBOARDING, completed)
            .apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        android.util.Log.d("AuthPreferences", "🗑️ All preferences cleared")
    }
}
