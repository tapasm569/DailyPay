package com.dailypay.app.util

import android.content.Context
import android.content.SharedPreferences
import com.dailypay.app.data.model.UserRole

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("dailypay_user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_MOBILE = "user_mobile"
        private const val KEY_BUSINESS_NAME = "business_name"
    }

    fun saveSession(
        role: UserRole,
        userId: String,
        name: String = "",
        mobile: String = "",
        businessName: String = ""
    ) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ROLE, role.name)
            .putString(KEY_USER_ID, userId.trim())
            .putString(KEY_USER_NAME, name.trim())
            .putString(KEY_USER_MOBILE, mobile.trim())
            .putString(KEY_BUSINESS_NAME, businessName.trim())
            .apply()
    }

    fun saveBusinessName(businessName: String) {
        prefs.edit()
            .putString(KEY_BUSINESS_NAME, businessName.trim())
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return try {
            val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
            val userId = prefs.getString(KEY_USER_ID, null)
            val role = getUserRole()
            // Session is valid only when loggedIn flag is true, role is a valid enum, and user ID is not blank or a placeholder
            loggedIn && role != null && !userId.isNullOrBlank() &&
                    userId != "{lenderId}" && userId != "{borrowerId}"
        } catch (e: Exception) {
            false
        }
    }

    fun getUserRole(): UserRole? {
        return try {
            val roleStr = prefs.getString(KEY_USER_ROLE, null) ?: return null
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserId(): String? = try {
        prefs.getString(KEY_USER_ID, null)?.trim()?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }

    fun getUserName(): String = try {
        prefs.getString(KEY_USER_NAME, "").orEmpty()
    } catch (e: Exception) {
        ""
    }

    fun getUserMobile(): String = try {
        prefs.getString(KEY_USER_MOBILE, "").orEmpty()
    } catch (e: Exception) {
        ""
    }

    fun getBusinessName(): String = try {
        prefs.getString(KEY_BUSINESS_NAME, "").orEmpty()
    } catch (e: Exception) {
        ""
    }

    fun clearSession() {
        // commit() is used synchronously so session state clears before navigation re-evaluates
        prefs.edit().clear().commit()
    }
}
