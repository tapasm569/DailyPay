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
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ================= AUTH =================
        composable(Screen.Login.route) {
            LoginScreen(
                onAdminLoginSuccess = {
                    navController.navigate(Screen.AdminDashboard.route) { popUpTo(0) { inclusive = true } }
                },
                onLenderLoginSuccess = { lenderId ->
                    navController.navigate(Screen.LenderHome.createRoute(lenderId)) { popUpTo(0) { inclusive = true } }
                },
                onBorrowerLoginSuccess = { borrowerId ->
                    navController.navigate(Screen.BorrowerDashboard.createRoute(borrowerId)) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // ================= ADMIN =================
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToCreateLender = { navController.navigate(Screen.CreateLender.route) },
                onNavigateToManageLender = { navController.navigate(Screen.ManageLender.route) }
            )
        }
        composable(Screen.CreateLender.route) { CreateLenderScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.ManageLender.route) { ManageLenderScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.LenderStatus.route) { LenderStatusScreen(onNavigateBack = { navController.popBackStack() }) }

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
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
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
                onNavigateToAddBorrower = { navController.navigate(Screen.AddBorrower.createRoute(lenderId)) },
                onNavigateToMasterClient = { navController.navigate(Screen.MasterClient.createRoute(lenderId)) },
                onNavigateToGiveLoan = { navController.navigate(Screen.GiveLoanManual.createRoute(lenderId)) },
                onNavigateToNewLoanRequest = { navController.navigate(Screen.NewLoanRequest.createRoute(lenderId)) },
                onNavigateToApprovedLoans = { navController.navigate(Screen.ApprovedLoans.createRoute(lenderId)) },
                onNavigateToTrackPayments = { navController.navigate(Screen.TrackPayments.createRoute(lenderId)) },
                onNavigateToTodaysDue = { navController.navigate(Screen.TodaysDue.createRoute(lenderId)) },
                onNavigateToTodaysPayment = { navController.navigate(Screen.TodaysPayment.createRoute(lenderId)) },
                onNavigateToVerifyPayment = { navController.navigate(Screen.VerifyPayment.createRoute(lenderId)) },
                onNavigateToLedger = { navController.navigate(Screen.LenderLedger.createRoute(lenderId)) }
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
            route = Screen.MasterClient.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            MasterClientScreen(
                lenderId = lenderId,
                onNavigateBack = { navController.popBackStack() }
            )
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
            route = Screen.ApprovedLoans.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            ApprovedLoansScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.TrackPayments.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            TrackPaymentsScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.TodaysDue.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            TodaysDueScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.TodaysPayment.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            TodaysPaymentScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.VerifyPayment.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            VerifyPaymentScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.LenderLedger.route,
            arguments = listOf(navArgument("lenderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lenderId = backStackEntry.arguments?.getString("lenderId") ?: ""
            LenderLedgerScreen(lenderId = lenderId, onNavigateBack = { navController.popBackStack() })
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
                onApplyLoanClick = { navController.navigate(Screen.ApplyLoan.createRoute(borrowerId)) },
                onApprovedLoansClick = { navController.navigate(Screen.CustomerApprovedLoans.createRoute(borrowerId)) },
                onViewLedgerClick = { navController.navigate(Screen.BorrowerLedger.createRoute(borrowerId)) },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(
            route = Screen.BorrowerPayEmi.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            BorrowerPayEmiScreen(borrowerId = borrowerId, onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.ApplyLoan.route,
            arguments = listOf(navArgument("borrowerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val borrowerId = backStackEntry.arguments?.getString("borrowerId") ?: ""
            ApplyLoanScreen(borrowerId = borrowerId, onNavigateBack = { navController.popBackStack() })
        }

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
