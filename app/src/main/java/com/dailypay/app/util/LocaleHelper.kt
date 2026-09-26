package com.dailypay.app.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
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

    fun applyLanguageContext(context: Context): Context {
        val languageCode = getCurrentLanguageCode(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun setAppLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("dailypay_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_LANG, languageCode).commit()

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        (context as? Activity)?.recreate()
    }
}

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String
)
