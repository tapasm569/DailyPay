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
        // Lender Home (Tabbed: Pending Dues & Paid Today)
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

        // Lender Dashboard Grid Menu
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
                onSetInterestClick = { navController.navigate(Screen.SetInterest.createRoute(lenderId)) },
                onGiveLoanManualClick = { navController.navigate(Screen.GiveLoanManual.createRoute(lenderId)) },
                onApprovedLoansClick = { navController.navigate(Screen.LenderApprovedLoans.createRoute(lenderId)) },
                onTodaysDueClick = { navController.navigate(Screen.TodaysDue.createRoute(lenderId)) },
                onTodaysPaymentClick = { navController.navigate(Screen.TodaysPayment.createRoute(lenderId)) },
                onMasterClientClick = { navController.navigate(Screen.MasterClient.createRoute(lenderId)) },
                onLedgerBookClick = { navController.navigate(Screen.LenderLedger.createRoute(lenderId)) }
            )
        }

        // Add Borrower Screen
        composable(
            route = Screen.AddBorrower.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            AddBorrowerScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        // Give Loan Manually
        composable(
            route = Screen.GiveLoanManual.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            GiveLoanManualScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        // New Loan Requests (Approval / Disburse)
        composable(
            route = Screen.NewLoanRequest.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            NewLoanRequestScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        // Lender Approved Loans Screen
        composable(
            route = Screen.LenderApprovedLoans.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            ApprovedLoansScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        // Set Interest Rate
        composable(
            route = Screen.SetInterest.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            SetInterestScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        // Today's Due (Auto-clearing unpaid list)
        composable(
            route = Screen.TodaysDue.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            TodaysDueScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        // Today's Payment (Collected payments list)
        composable(
            route = Screen.TodaysPayment.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            TodaysPaymentScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        // Master Client List
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

        // Business Ledger Book
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
                onApprovedLoansClick = { navController.navigate(Screen.CustomerApprovedLoans.createRoute(borrowerId)) },
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

        // Customer Approved Loans Screen
        composable(
            route = Screen.CustomerApprovedLoans.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            CustomerApprovedLoansScreen(borrowerId = borrowerId, onNavigateBack = { navController.popBackStack() })
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