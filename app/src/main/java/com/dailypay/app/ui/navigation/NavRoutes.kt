package com.dailypay.app.ui.navigation

sealed class Screen(val route: String) {

    // Auth
    object Login : Screen("login")

    // Admin
    object AdminDashboard : Screen("admin_dashboard")
    object CreateLender : Screen("create_lender")
    object ManageLender : Screen("manage_lender")
    object AdminPasswordRequests : Screen("admin_password_requests")
    object AdminSubscription : Screen("admin_subscription")
    object AdminReminder : Screen("admin_reminder")

    // Lender
    object LenderHome : Screen("lender_home/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_home/${lenderId.ifBlank { "unknown" }}"
    }
    object LenderDashboard : Screen("lender_dashboard/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_dashboard/${lenderId.ifBlank { "unknown" }}"
    }
    object AddBorrower : Screen("add_borrower/{lenderId}") {
        fun createRoute(lenderId: String) = "add_borrower/${lenderId.ifBlank { "unknown" }}"
    }
    object GiveLoanManual : Screen("give_loan_manual/{lenderId}") {
        fun createRoute(lenderId: String) = "give_loan_manual/${lenderId.ifBlank { "unknown" }}"
    }
    object NewLoanRequest : Screen("new_loan_request/{lenderId}") {
        fun createRoute(lenderId: String) = "new_loan_request/${lenderId.ifBlank { "unknown" }}"
    }
    object LenderApprovedLoans : Screen("lender_approved_loans/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_approved_loans/${lenderId.ifBlank { "unknown" }}"
    }
    object TodaysDue : Screen("todays_due/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_due/${lenderId.ifBlank { "unknown" }}"
    }
    object TodaysPayment : Screen("todays_payment/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_payment/${lenderId.ifBlank { "unknown" }}"
    }
    object VerifyPayment : Screen("verify_payment/{lenderId}") {
        fun createRoute(lenderId: String) = "verify_payment/${lenderId.ifBlank { "unknown" }}"
    }
    object TrackPayments : Screen("track_payments/{lenderId}") {
        fun createRoute(lenderId: String) = "track_payments/${lenderId.ifBlank { "unknown" }}"
    }
    object MasterClient : Screen("master_client/{lenderId}") {
        fun createRoute(lenderId: String) = "master_client/${lenderId.ifBlank { "unknown" }}"
    }
    object LenderLedger : Screen("lender_ledger/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_ledger/${lenderId.ifBlank { "unknown" }}"
    }

    // Borrower
    object BorrowerDashboard : Screen("borrower_dashboard/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_dashboard/${borrowerId.ifBlank { "unknown" }}"
    }
    object BorrowerPayEmi : Screen("borrower_pay_emi/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_pay_emi/${borrowerId.ifBlank { "unknown" }}"
    }
    object BorrowerApplyLoan : Screen("borrower_apply_loan/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_apply_loan/${borrowerId.ifBlank { "unknown" }}"
    }
    object CustomerApprovedLoans : Screen("customer_approved_loans/{borrowerId}") {
        fun createRoute(borrowerId: String) = "customer_approved_loans/${borrowerId.ifBlank { "unknown" }}"
    }
    object BorrowerLedger : Screen("borrower_ledger/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_ledger/${borrowerId.ifBlank { "unknown" }}"
    }
}
