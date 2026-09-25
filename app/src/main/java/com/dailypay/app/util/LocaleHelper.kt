package com.dailypay.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {

    val supportedLanguages = listOf(
        LanguageOption("en", "English", "English"),
        LanguageOption("bn", "বাংলা", "Bengali"),
        LanguageOption("hi", "हिन्दी", "Hindi")
    )

    fun getCurrentLanguageCode(): String {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        return if (!currentLocales.isEmpty) {
            currentLocales[0]?.language ?: "en"
        } else {
            "en"
        }
    }

    fun setAppLanguage(languageCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String
)
