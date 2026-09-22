package com.dailypay.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dailypay.app.data.model.UserRole
import com.dailypay.app.ui.screens.admin.*
import com.dailypay.app.ui.screens.auth.LoginScreen
import com.dailypay.app.ui.screens.borrower.*
import com.dailypay.app.ui.screens.lender.*
import com.dailypay.app.util.SessionManager

@Composable
fun AppNavHost(
    navController: NavHostController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    val isLoggedIn = sessionManager.isLoggedIn()
    val savedRole = sessionManager.getUserRole()
    val savedId = sessionManager.getUserId()

    // Determine initial route using the concrete UUID rather than route templates
    val startDestination = remember {
        if (isLoggedIn && !savedId.isNullOrBlank() && savedRole != null) {
            when (savedRole) {
                UserRole.ADMIN -> NavRoutes.AdminDashboard.route
                UserRole.LENDER -> NavRoutes.LenderHome.createRoute(savedId)
                UserRole.BORROWER -> NavRoutes.BorrowerDashboard.createRoute(savedId)
            }
        } else {
            NavRoutes.Login.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ================= AUTH =================
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = { role, id ->
                    sessionManager.saveSession(role, id)
                    when (role) {
                        UserRole.ADMIN -> navController.navigate(NavRoutes.AdminDashboard.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                        UserRole.LENDER -> navController.navigate(NavRoutes.LenderHome.createRoute(id)) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                        UserRole.BORROWER -> navController.navigate(NavRoutes.BorrowerDashboard.createRoute(id)) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ================= LENDER SCREENS =================
        composable(
            route = NavRoutes.LenderHome.route,
            arguments = listOf(
                navArgument("lenderId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val argId = backStackEntry.arguments?.getString("lenderId")
            val effectiveLenderId = if (!argId.isNullOrBlank() && argId != "{lenderId}") {
                argId
            } else {
                sessionManager.getUserId() ?: ""
            }

            LenderHomeScreen(
                lenderId = effectiveLenderId,
                onNavigateToDashboard = {
                    navController.navigate(NavRoutes.LenderDashboard.createRoute(effectiveLenderId))
                },
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.LenderDashboard.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            LenderDashboardScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddBorrower = { navController.navigate(NavRoutes.AddBorrower.createRoute(lenderId)) },
                onNavigateToGiveLoan = { navController.navigate(NavRoutes.GiveLoanManual.createRoute(lenderId)) },
                onNavigateToPendingRequests = { navController.navigate(NavRoutes.NewLoanRequests.createRoute(lenderId)) },
                onNavigateToApprovedLoans = { navController.navigate(NavRoutes.ApprovedLoans.createRoute(lenderId)) },
                onNavigateToTodaysDue = { navController.navigate(NavRoutes.TodaysDue.createRoute(lenderId)) },
                onNavigateToTodaysPayment = { navController.navigate(NavRoutes.TodaysPayment.createRoute(lenderId)) },
                onNavigateToVerifyPayment = { navController.navigate(NavRoutes.VerifyPayment.createRoute(lenderId)) },
                onNavigateToTrackPayments = { navController.navigate(NavRoutes.TrackPayments.createRoute(lenderId)) },
                onNavigateToMasterClient = { navController.navigate(NavRoutes.MasterClient.createRoute(lenderId)) },
                onNavigateToLedger = { navController.navigate(NavRoutes.LenderLedger.createRoute(lenderId)) }
            )
        }

        composable(
            route = NavRoutes.AddBorrower.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            AddBorrowerScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.GiveLoanManual.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            GiveLoanManualScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.NewLoanRequests.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            NewLoanRequestScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.ApprovedLoans.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            ApprovedLoansScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.TodaysDue.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            TodaysDueScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.TodaysPayment.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            TodaysPaymentScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.VerifyPayment.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            VerifyPaymentScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.TrackPayments.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            TrackPaymentsScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.MasterClient.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            MasterClientScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() },
                onAddNewBorrowerClick = { navController.navigate(NavRoutes.AddBorrower.createRoute(lenderId)) }
            )
        }

        composable(
            route = NavRoutes.LenderLedger.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            LenderLedgerScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ================= BORROWER SCREENS =================
        composable(
            route = NavRoutes.BorrowerDashboard.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            BorrowerDashboardScreen(
                borrowerId = borrowerId,
                onNavigateToApplyLoan = { navController.navigate(NavRoutes.ApplyLoan.createRoute(borrowerId)) },
                onNavigateToPayEmi = { navController.navigate(NavRoutes.BorrowerPayEmi.createRoute(borrowerId)) },
                onNavigateToApprovedLoans = { navController.navigate(NavRoutes.CustomerApprovedLoans.createRoute(borrowerId)) },
                onNavigateToLedger = { navController.navigate(NavRoutes.BorrowerLedger.createRoute(borrowerId)) },
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.ApplyLoan.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            ApplyLoanScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.BorrowerPayEmi.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            BorrowerPayEmiScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.CustomerApprovedLoans.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            CustomerApprovedLoansScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.BorrowerLedger.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            BorrowerLedgerScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ================= ADMIN SCREENS =================
        composable(NavRoutes.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToCreateLender = { navController.navigate(NavRoutes.CreateLender.route) },
                onNavigateToManageLender = { navController.navigate(NavRoutes.ManageLender.route) },
                onNavigateToSubscriptions = { navController.navigate(NavRoutes.AdminSubscriptions.route) },
                onNavigateToReminders = { navController.navigate(NavRoutes.AdminReminders.route) },
                onNavigateToPasswordRequests = { navController.navigate(NavRoutes.AdminPasswordRequests.route) },
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.CreateLender.route) {
            CreateLenderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.ManageLender.route) {
            ManageLenderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.AdminSubscriptions.route) {
            SubscriptionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.AdminReminders.route) {
            ReminderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.AdminPasswordRequests.route) {
            PasswordRequestsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
