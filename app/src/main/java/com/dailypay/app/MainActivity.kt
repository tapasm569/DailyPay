package com.dailypay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dailypay.app.data.model.UserRole
import com.dailypay.app.ui.navigation.AppNavHost
import com.dailypay.app.ui.navigation.Screen
import com.dailypay.app.ui.theme.DailyPayTheme
import com.dailypay.app.util.SessionManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        val initialDestination = determineStartDestination(sessionManager)

        setContent {
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
