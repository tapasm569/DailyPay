package com.dailypay.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ================= AUTH =================
        composable(Screen.Login.route) {
            LoginScreen(
                onAdminLoginSuccess = {
                    sessionManager.saveSession(UserRole.ADMIN, "admin", "Admin", "")
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLenderLoginSuccess = { lenderId ->
                    sessionManager.saveSession(UserRole.LENDER, lenderId)
                    navController.navigate(Screen.LenderHome.createRoute(lenderId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBorrowerLoginSuccess = { borrowerId ->
                    sessionManager.saveSession(UserRole.BORROWER, borrowerId)
                    navController.navigate(Screen.BorrowerDashboard.createRoute(borrowerId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ================= LENDER SCREENS =================
        composable(
            route = Screen.LenderHome.route,
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
                    navController.navigate(Screen.LenderDashboard.createRoute(effectiveLenderId))
                },
                onLogoutClick = {
                    sessionManager.clearSession()
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
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            LenderDashboardScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() },
                onAddBorrowerClick = { navController.navigate(Screen.AddBorrower.createRoute(lenderId)) },
                onGiveLoanManualClick = { navController.navigate(Screen.GiveLoanManual.createRoute(lenderId)) },
                onNewLoanRequestClick = { navController.navigate(Screen.NewLoanRequests.createRoute(lenderId)) },
                onApprovedLoansClick = { navController.navigate(Screen.ApprovedLoans.createRoute(lenderId)) },
                onTodaysDueClick = { navController.navigate(Screen.TodaysDue.createRoute(lenderId)) },
                onTodaysPaymentClick = { navController.navigate(Screen.TodaysPayment.createRoute(lenderId)) },
                onVerifyPaymentClick = { navController.navigate(Screen.VerifyPayment.createRoute(lenderId)) },
                onTrackPaymentsClick = { navController.navigate(Screen.TrackPayments.createRoute(lenderId)) },
                onMasterClientClick = { navController.navigate(Screen.MasterClient.createRoute(lenderId)) },
                onLenderLedgerClick = { navController.navigate(Screen.LenderLedger.createRoute(lenderId)) }
            )
        }

        composable(
            route = Screen.AddBorrower.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            AddBorrowerScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GiveLoanManual.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            GiveLoanManualScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NewLoanRequests.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            NewLoanRequestScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ApprovedLoans.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            ApprovedLoansScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TodaysDue.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            TodaysDueScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TodaysPayment.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            TodaysPaymentScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.VerifyPayment.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            VerifyPaymentScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TrackPayments.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            TrackPaymentsScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MasterClient.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: sessionManager.getUserId() ?: ""
            MasterClientScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() },
                onAddNewBorrowerClick = { navController.navigate(Screen.AddBorrower.createRoute(lenderId)) }
            )
        }

        composable(
            route = Screen.LenderLedger.route,
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
            route = Screen.BorrowerDashboard.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            BorrowerDashboardScreen(
                borrowerId = borrowerId,
                onApplyLoanClick = { navController.navigate(Screen.ApplyLoan.createRoute(borrowerId)) },
                onPayEmiClick = { navController.navigate(Screen.BorrowerPayEmi.createRoute(borrowerId)) },
                onApprovedLoansClick = { navController.navigate(Screen.CustomerApprovedLoans.createRoute(borrowerId)) },
                onViewLedgerClick = { navController.navigate(Screen.BorrowerLedger.createRoute(borrowerId)) },
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ApplyLoan.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            ApplyLoanScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BorrowerPayEmi.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            BorrowerPayEmiScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CustomerApprovedLoans.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            CustomerApprovedLoansScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BorrowerLedger.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: sessionManager.getUserId() ?: ""
            BorrowerLedgerScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ================= ADMIN SCREENS =================
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onCreateLenderClick = { navController.navigate(Screen.CreateLender.route) },
                onManageLenderClick = { navController.navigate(Screen.ManageLender.route) },
                onSubscriptionClick = { navController.navigate(Screen.AdminSubscriptions.route) },
                onReminderClick = { navController.navigate(Screen.AdminReminders.route) },
                onResetPasswordClick = { navController.navigate(Screen.AdminPasswordRequests.route) },
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateLender.route) {
            CreateLenderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.ManageLender.route) {
            ManageLenderScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddNewLenderClick = { navController.navigate(Screen.CreateLender.route) }
            )
        }

        composable(Screen.AdminSubscriptions.route) {
            SubscriptionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AdminReminders.route) {
            ReminderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AdminPasswordRequests.route) {
            PasswordRequestsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
