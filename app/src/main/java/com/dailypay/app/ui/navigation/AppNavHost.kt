package com.dailypay.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    startDestination: String = Screen.Login.route
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ================= AUTH =================
        composable(Screen.Login.route) {
            LoginScreen(
                onAdminLoginSuccess = {
                    sessionManager.saveSession(UserRole.ADMIN, "admin")
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLenderLoginSuccess = { lenderId: String ->
                    sessionManager.saveSession(UserRole.LENDER, lenderId)
                    navController.navigate(Screen.LenderHome.createRoute(lenderId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBorrowerLoginSuccess = { borrowerId: String ->
                    sessionManager.saveSession(UserRole.BORROWER, borrowerId)
                    navController.navigate(Screen.BorrowerDashboard.createRoute(borrowerId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ================= ADMIN =================
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onCreateLenderClick = { navController.navigate(Screen.CreateLender.route) },
                onManageLenderClick = { navController.navigate(Screen.ManageLender.route) },
                onSubscriptionClick = { navController.navigate(Screen.AdminSubscription.route) },
                onReminderClick = { navController.navigate(Screen.AdminReminder.route) },
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

        composable(Screen.AdminPasswordRequests.route) {
            PasswordRequestsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AdminSubscription.route) {
            SubscriptionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AdminReminder.route) {
            ReminderScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ================= LENDER =================
        composable(
            route = Screen.LenderHome.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            LenderHomeScreen(
                lenderId = lenderId,
                onNavigateToDashboard = { navController.navigate(Screen.LenderDashboard.createRoute(lenderId)) },
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
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            LenderDashboardScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() },
                onAddBorrowerClick = { navController.navigate(Screen.AddBorrower.createRoute(lenderId)) },
                onGiveLoanManualClick = { navController.navigate(Screen.GiveLoanManual.createRoute(lenderId)) },
                onNewLoanRequestClick = { navController.navigate(Screen.NewLoanRequest.createRoute(lenderId)) },
                onApprovedLoansClick = { navController.navigate(Screen.LenderApprovedLoans.createRoute(lenderId)) },
                onTodaysDueClick = { navController.navigate(Screen.TodaysDue.createRoute(lenderId)) },
                onTodaysPaymentClick = { navController.navigate(Screen.TodaysPayment.createRoute(lenderId)) },
                onVerifyPaymentClick = { navController.navigate(Screen.VerifyPayment.createRoute(lenderId)) },
                onTrackPaymentsClick = { navController.navigate(Screen.TrackPayments.createRoute(lenderId)) },
                onMasterClientClick = { navController.navigate(Screen.MasterClient.createRoute(lenderId)) },
                onLenderLedgerClick = { navController.navigate(Screen.LenderLedger.createRoute(lenderId)) }
            )
        }

        composable(
            route = Screen.VerifyPayment.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            VerifyPaymentScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TrackPayments.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            TrackPaymentsScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddBorrower.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            AddBorrowerScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GiveLoanManual.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            GiveLoanManualScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NewLoanRequest.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            NewLoanRequestScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LenderApprovedLoans.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            ApprovedLoansScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TodaysDue.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            TodaysDueScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TodaysPayment.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            TodaysPaymentScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MasterClient.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
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
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            LenderLedgerScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ================= BORROWER =================
        composable(
            route = Screen.BorrowerDashboard.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            BorrowerDashboardScreen(
                borrowerId = borrowerId,
                onPayEmiClick = { navController.navigate(Screen.BorrowerPayEmi.createRoute(borrowerId)) },
                onApplyLoanClick = { navController.navigate(Screen.BorrowerApplyLoan.createRoute(borrowerId)) },
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
            route = Screen.BorrowerPayEmi.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            BorrowerPayEmiScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BorrowerApplyLoan.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            ApplyLoanScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CustomerApprovedLoans.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            CustomerApprovedLoansScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BorrowerLedger.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            BorrowerLedgerScreen(
                borrowerId = borrowerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
