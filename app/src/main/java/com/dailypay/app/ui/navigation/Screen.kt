package com.dailypay.app.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {

    // ================= AUTH =================
    data object Login : Screen("login")

    // ================= ADMIN =================
    data object AdminDashboard : Screen("admin_dashboard")
    data object CreateLender : Screen("create_lender")
    data object ManageLender : Screen("manage_lender")
    data object LenderStatus : Screen("lender_status")

    // Backward-compatibility fallbacks for removed admin screens (prevents compile errors in unedited files)
    @Deprecated("Replaced by LenderStatus", ReplaceWith("LenderStatus"))
    data object AdminPasswordRequests : Screen("admin_password_requests")
    @Deprecated("Replaced by LenderStatus", ReplaceWith("LenderStatus"))
    data object AdminSubscription : Screen("admin_subscription")
    @Deprecated("Replaced by LenderStatus", ReplaceWith("LenderStatus"))
    data object AdminSubscriptions : Screen("admin_subscription")
    @Deprecated("Replaced by LenderStatus", ReplaceWith("LenderStatus"))
    data object AdminReminder : Screen("admin_reminder")
    @Deprecated("Replaced by LenderStatus", ReplaceWith("LenderStatus"))
    data object AdminReminders : Screen("admin_reminder")

    // ================= LENDER =================
    data object LenderHome : Screen("lender_home/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_home/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object LenderDashboard : Screen("lender_dashboard/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_dashboard/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object AddBorrower : Screen("add_borrower/{lenderId}") {
        fun createRoute(lenderId: String) = "add_borrower/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object GiveLoanManual : Screen("give_loan_manual/{lenderId}") {
        fun createRoute(lenderId: String) = "give_loan_manual/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }

    // New Loan Request (both singular and plural aliases supported)
    data object NewLoanRequest : Screen("new_loan_request/{lenderId}") {
        fun createRoute(lenderId: String) = "new_loan_request/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object NewLoanRequests : Screen("new_loan_request/{lenderId}") {
        fun createRoute(lenderId: String) = "new_loan_request/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }

    // Approved Loans (both prefixed and unprefixed aliases supported)
    data object LenderApprovedLoans : Screen("lender_approved_loans/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_approved_loans/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object ApprovedLoans : Screen("lender_approved_loans/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_approved_loans/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }

    data object TodaysDue : Screen("todays_due/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_due/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object TodaysPayment : Screen("todays_payment/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_payment/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object VerifyPayment : Screen("verify_payment/{lenderId}") {
        fun createRoute(lenderId: String) = "verify_payment/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object TrackPayments : Screen("track_payments/{lenderId}") {
        fun createRoute(lenderId: String) = "track_payments/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object MasterClient : Screen("master_client/{lenderId}") {
        fun createRoute(lenderId: String) = "master_client/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }
    data object LenderLedger : Screen("lender_ledger/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_ledger/${Uri.encode(lenderId.ifBlank { "unknown" })}"
    }

    // ================= BORROWER =================
    data object BorrowerDashboard : Screen("borrower_dashboard/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_dashboard/${Uri.encode(borrowerId.ifBlank { "unknown" })}"
    }
    data object BorrowerPayEmi : Screen("borrower_pay_emi/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_pay_emi/${Uri.encode(borrowerId.ifBlank { "unknown" })}"
    }

    // Apply Loan (both prefixed and clean aliases supported)
    data object BorrowerApplyLoan : Screen("borrower_apply_loan/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_apply_loan/${Uri.encode(borrowerId.ifBlank { "unknown" })}"
    }
    data object ApplyLoan : Screen("borrower_apply_loan/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_apply_loan/${Uri.encode(borrowerId.ifBlank { "unknown" })}"
    }

    data object CustomerApprovedLoans : Screen("customer_approved_loans/{borrowerId}") {
        fun createRoute(borrowerId: String) = "customer_approved_loans/${Uri.encode(borrowerId.ifBlank { "unknown" })}"
    }
    data object BorrowerLedger : Screen("borrower_ledger/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_ledger/${Uri.encode(borrowerId.ifBlank { "unknown" })}"
    }
}
