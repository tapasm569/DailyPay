package com.dailypay.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dailypay.app.data.model.UserRole

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        // Fallback to standard private SharedPreferences if Android KeyStore fails on non-standard ROMs
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "dailypay_secure_prefs"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_MOBILE = "key_user_mobile"
        private const val KEY_LANGUAGE = "key_language"
    }

    fun saveSession(
        role: UserRole,
        userId: String,
        name: String = "",
        mobile: String = ""
    ) {
        val cleanId = userId.trim()
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ROLE, role.name)
            putString(KEY_USER_ID, cleanId)
            putString(KEY_USER_NAME, name.trim())
            putString(KEY_USER_MOBILE, mobile.trim())
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): String? {
        val id = prefs.getString(KEY_USER_ID, null)?.trim()
        return if (!id.isNullOrBlank() && id != "unknown" && id != "{lenderId}" && id != "{borrowerId}") {
            id
        } else {
            null
        }
    }

    fun getUserRole(): UserRole? {
        val roleStr = prefs.getString(KEY_USER_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            null
        }
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun getUserMobile(): String = prefs.getString(KEY_USER_MOBILE, "") ?: ""

    fun setLanguage(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode.trim()).apply()
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"

    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_ROLE)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_MOBILE)
            apply()
        }
    }
}
