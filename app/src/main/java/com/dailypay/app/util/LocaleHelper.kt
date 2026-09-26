package com.dailypay.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.dailypay.app.data.model.UserRole
import com.dailypay.app.ui.navigation.AppNavHost
import com.dailypay.app.ui.navigation.Screen
import com.dailypay.app.ui.theme.DailyPayTheme
import com.dailypay.app.util.LocaleHelper
import com.dailypay.app.util.SessionManager
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguageContext(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val lang = LocaleHelper.getCurrentLanguageCode(this)
            val locale = Locale(lang)
            overrideConfiguration.setLocale(locale)
            overrideConfiguration.setLayoutDirection(locale)
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        val initialDestination = determineStartDestination(sessionManager)

        val langCode = LocaleHelper.getCurrentLanguageCode(this)
        val targetLocale = Locale(langCode)
        val localizedContext = LocaleHelper.applyLanguageContext(this)

        val localizedConfig = Configuration(resources.configuration).apply {
            setLocale(targetLocale)
            setLayoutDirection(targetLocale)
        }

        setContent {
            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalContext provides localizedContext
            ) {
                DailyPayTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        AppNavHost(
                            navController = navController,
                            startDestination = initialDestination
                        )
                    }
                }
            }
        }
    }

    private fun determineStartDestination(sessionManager: SessionManager): String {
        if (!sessionManager.isLoggedIn()) {
            return Screen.Login.route
        }

        val role = sessionManager.getUserRole()
        val userId = sessionManager.getUserId() ?: ""

        return when (role) {
            UserRole.ADMIN -> Screen.AdminDashboard.route
            UserRole.LENDER -> {
                if (userId.isNotBlank()) Screen.LenderHome.createRoute(userId)
                else Screen.Login.route
            }
            UserRole.BORROWER -> {
                if (userId.isNotBlank()) Screen.BorrowerDashboard.createRoute(userId)
                else Screen.Login.route
            }
            null -> Screen.Login.route
        }
    }
}
