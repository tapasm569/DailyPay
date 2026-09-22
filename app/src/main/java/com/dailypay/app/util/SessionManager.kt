package com.dailypay.app.util

import android.content.Context
import android.content.SharedPreferences
import com.dailypay.app.data.model.UserRole

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "dailypay_session_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
    }

    fun saveSession(role: UserRole, userId: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ROLE, role.name)
            putString(KEY_USER_ID, userId)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserRole(): UserRole? {
        val roleStr = prefs.getString(KEY_USER_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
