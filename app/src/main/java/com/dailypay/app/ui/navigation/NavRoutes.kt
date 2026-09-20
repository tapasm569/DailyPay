package com.dailypay.app.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")

    // Admin Flow
    data object AdminDashboard : Screen("admin_dashboard")
    data object CreateLender : Screen("create_lender")
    data object ManageLender : Screen("manage_lender")
    data object AdminSubscription : Screen("admin_subscription")
    data object AdminReminder : Screen("admin_reminder")
    data object AdminResetPassword : Screen("admin_reset_password")

    // Lender Flow
    data object LenderHome : Screen("lender_home/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_home/$lenderId"
    }
    data object LenderDashboard : Screen("lender_dashboard/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_dashboard/$lenderId"
    }
    data object AddBorrower : Screen("add_borrower/{lenderId}") {
        fun createRoute(lenderId: String) = "add_borrower/$lenderId"
    }
    data object NewLoanRequest : Screen("new_loan_request/{lenderId}") {
        fun createRoute(lenderId: String) = "new_loan_request/$lenderId"
    }
    data object SetInterest : Screen("set_interest/{lenderId}") {
        fun createRoute(lenderId: String) = "set_interest/$lenderId"
    }
    data object GiveLoanManual : Screen("give_loan_manual/{lenderId}") {
        fun createRoute(lenderId: String) = "give_loan_manual/$lenderId"
    }
    data object TodaysDue : Screen("todays_due/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_due/$lenderId"
    }
    data object TodaysPayment : Screen("todays_payment/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_payment/$lenderId"
    }
    data object MasterClient : Screen("master_client/{lenderId}") {
        fun createRoute(lenderId: String) = "master_client/$lenderId"
    }
    data object LenderLedger : Screen("lender_ledger/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_ledger/$lenderId"
    }

    // Borrower Flow
    data object BorrowerDashboard : Screen("borrower_dashboard/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_dashboard/$borrowerId"
    }
    data object BorrowerApplyLoan : Screen("borrower_apply_loan/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_apply_loan/$borrowerId"
    }
    data object BorrowerApprovedLoans : Screen("borrower_approved_loans/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_approved_loans/$borrowerId"
    }
    data object BorrowerPayment : Screen("borrower_payment/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_payment/$borrowerId"
    }
    data object BorrowerLedger : Screen("borrower_ledger/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_ledger/$borrowerId"
    }
}
