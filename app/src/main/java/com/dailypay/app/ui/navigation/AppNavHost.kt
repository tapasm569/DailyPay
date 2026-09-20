package com.dailypay.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dailypay.app.ui.screens.admin.*
import com.dailypay.app.ui.screens.auth.LoginScreen
import com.dailypay.app.ui.screens.borrower.*
import com.dailypay.app.ui.screens.lender.*

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // Multi-Role Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onAdminLoginSuccess = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLenderLoginSuccess = { lenderId: String ->
                    navController.navigate(Screen.LenderHome.createRoute(lenderId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBorrowerLoginSuccess = { borrowerId: String ->
                    navController.navigate(Screen.BorrowerDashboard.createRoute(borrowerId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ================= ADMIN FLOW =================
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onCreateLenderClick = { navController.navigate(Screen.CreateLender.route) },
                onManageLenderClick = { navController.navigate(Screen.AdminSubscription.route) },
                onSubscriptionClick = { navController.navigate(Screen.AdminSubscription.route) },
                onReminderClick = { navController.navigate(Screen.AdminReminder.route) },
                onResetPasswordClick = { navController.navigate(Screen.AdminReminder.route) },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateLender.route) {
            CreateLenderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AdminSubscription.route) {
            SubscriptionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AdminReminder.route) {
            ReminderScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ================= LENDER FLOW =================
        composable(
            route = Screen.LenderHome.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            LenderHomeScreen(
                lenderId = lenderId,
                onNavigateToDashboard = {
                    navController.navigate(Screen.LenderDashboard.createRoute(lenderId))
                },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.LenderDashboard.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            LenderDashboardScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() },
                onAddBorrowerClick = { navController.navigate(Screen.AddBorrower.createRoute(lenderId)) },
                onNewLoanRequestClick = { navController.navigate(Screen.NewLoanRequest.createRoute(lenderId)) },
                onSetInterestClick = { navController.navigate(Screen.GiveLoanManual.createRoute(lenderId)) },
                onGiveLoanManualClick = { navController.navigate(Screen.GiveLoanManual.createRoute(lenderId)) },
                onTodaysDueClick = { navController.popBackStack() },
                onTodaysPaymentClick = { navController.popBackStack() },
                onMasterClientClick = { navController.navigate(Screen.AddBorrower.createRoute(lenderId)) },
                onLedgerBookClick = { navController.navigate(Screen.LenderLedger.createRoute(lenderId)) }
            )
        }

        composable(
            route = Screen.AddBorrower.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            AddBorrowerScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.GiveLoanManual.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            GiveLoanManualScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.NewLoanRequest.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            NewLoanRequestScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.LenderLedger.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            LenderLedgerScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        // ================= BORROWER FLOW =================
        composable(
            route = Screen.BorrowerDashboard.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            BorrowerDashboardScreen(
                borrowerId = borrowerId,
                onApplyLoanClick = { navController.navigate(Screen.BorrowerApplyLoan.createRoute(borrowerId)) },
                onViewLedgerClick = { navController.navigate(Screen.BorrowerLedger.createRoute(borrowerId)) },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.BorrowerApplyLoan.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            ApplyLoanScreen(borrowerId = borrowerId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.BorrowerLedger.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            BorrowerLedgerScreen(borrowerId = borrowerId, onNavigateBack = { navController.popBackStack() })
        }
    }
}
