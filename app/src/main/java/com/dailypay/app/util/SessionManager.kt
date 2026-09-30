package com.dailypay.app.util

import android.content.Context
import android.content.SharedPreferences
import com.dailypay.app.data.model.UserRole

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dailypay_user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_MOBILE = "user_mobile"
    }

    fun saveSession(role: UserRole, userId: String, name: String = "", mobile: String = "") {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ROLE, role.name)
            .putString(KEY_USER_ID, userId.trim())
            .putString(KEY_USER_NAME, name.trim())
            .putString(KEY_USER_MOBILE, mobile.trim())
            .apply()
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val userId = prefs.getString(KEY_USER_ID, null)
        // Session is only valid if both the flag is true AND the ID is a valid non-blank string
        return loggedIn && !userId.isNullOrBlank() && userId != "{lenderId}" && userId != "{borrowerId}"
    }

    fun getUserRole(): UserRole? {
        val roleStr = prefs.getString(KEY_USER_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)?.takeIf { it.isNotBlank() }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun getUserMobile(): String = prefs.getString(KEY_USER_MOBILE, "") ?: ""

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
