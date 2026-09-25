package com.dailypay.app.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    val supportedLanguages = listOf(
        LanguageOption("en", "English", "English"),
        LanguageOption("bn", "বাংলা", "Bengali"),
        LanguageOption("hi", "हिन्दी", "Hindi")
    )

    private const val PREF_KEY_LANG = "app_language"

    fun getCurrentLanguageCode(context: Context): String {
        val prefs = context.getSharedPreferences("dailypay_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString(PREF_KEY_LANG, null)
        if (!saved.isNullOrBlank()) return saved
        return Locale.getDefault().language.let { if (it in listOf("bn", "hi")) it else "en" }
    }

    fun setAppLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("dailypay_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_LANG, languageCode).apply()

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
                localeManager?.applicationLocales = LocaleList.forLanguageTags(languageCode)
            } catch (_: Exception) {}
        }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        // Recreate activity to apply the language change immediately across Compose
        (context as? Activity)?.recreate()
    }
}

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String
)
