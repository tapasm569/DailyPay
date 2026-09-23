package com.dailypay.app.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")

    // Lender Routes
    data object LenderHome : Screen("lender_home/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_home/$lenderId"
    }
    data object LenderDashboard : Screen("lender_dashboard/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_dashboard/$lenderId"
    }
    data object AddBorrower : Screen("add_borrower/{lenderId}") {
        fun createRoute(lenderId: String) = "add_borrower/$lenderId"
    }
    data object GiveLoanManual : Screen("give_loan_manual/{lenderId}") {
        fun createRoute(lenderId: String) = "give_loan_manual/$lenderId"
    }
    data object NewLoanRequests : Screen("new_loan_requests/{lenderId}") {
        fun createRoute(lenderId: String) = "new_loan_requests/$lenderId"
    }
    data object ApprovedLoans : Screen("approved_loans/{lenderId}") {
        fun createRoute(lenderId: String) = "approved_loans/$lenderId"
    }
    data object TodaysDue : Screen("todays_due/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_due/$lenderId"
    }
    data object TodaysPayment : Screen("todays_payment/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_payment/$lenderId"
    }
    data object VerifyPayment : Screen("verify_payment/{lenderId}") {
        fun createRoute(lenderId: String) = "verify_payment/$lenderId"
    }
    data object TrackPayments : Screen("track_payments/{lenderId}") {
        fun createRoute(lenderId: String) = "track_payments/$lenderId"
    }
    data object MasterClient : Screen("master_client/{lenderId}") {
        fun createRoute(lenderId: String) = "master_client/$lenderId"
    }
    data object LenderLedger : Screen("lender_ledger/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_ledger/$lenderId"
    }

    // Borrower Routes
    data object BorrowerDashboard : Screen("borrower_dashboard/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_dashboard/$borrowerId"
    }
    data object ApplyLoan : Screen("apply_loan/{borrowerId}") {
        fun createRoute(borrowerId: String) = "apply_loan/$borrowerId"
    }
    data object BorrowerPayEmi : Screen("borrower_pay_emi/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_pay_emi/$borrowerId"
    }
    data object CustomerApprovedLoans : Screen("customer_approved_loans/{borrowerId}") {
        fun createRoute(borrowerId: String) = "customer_approved_loans/$borrowerId"
    }
    data object BorrowerLedger : Screen("borrower_ledger/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_ledger/$borrowerId"
    }

    // Admin Routes
    data object AdminDashboard : Screen("admin_dashboard")
    data object CreateLender : Screen("create_lender")
    data object ManageLender : Screen("manage_lender")
    data object AdminSubscriptions : Screen("admin_subscriptions")
    data object AdminReminders : Screen("admin_reminders")
    data object AdminPasswordRequests : Screen("admin_password_requests")
}
