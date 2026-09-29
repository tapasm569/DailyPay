package com.dailypay.app.ui.navigation

// Ensures route path parameters are never empty or malformed
private fun sanitizeParam(id: String): String {
    val clean = id.trim()
    return if (clean.isNotBlank() && clean != "unknown" && !clean.startsWith("{")) clean else "me"
}

sealed class Screen(val route: String) {

    // ================= AUTH =================
    data object Login : Screen("login")

    // ================= ADMIN =================
    data object AdminDashboard : Screen("admin_dashboard")
    data object CreateLender : Screen("create_lender")
    data object ManageLender : Screen("manage_lender")
    data object LenderStatus : Screen("lender_status")

    // Backward-compatibility fallbacks
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
        fun createRoute(lenderId: String) = "lender_home/${sanitizeParam(lenderId)}"
    }
    data object LenderDashboard : Screen("lender_dashboard/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_dashboard/${sanitizeParam(lenderId)}"
    }
    data object AddBorrower : Screen("add_borrower/{lenderId}") {
        fun createRoute(lenderId: String) = "add_borrower/${sanitizeParam(lenderId)}"
    }
    data object GiveLoanManual : Screen("give_loan_manual/{lenderId}") {
        fun createRoute(lenderId: String) = "give_loan_manual/${sanitizeParam(lenderId)}"
    }
    data object NewLoanRequest : Screen("new_loan_request/{lenderId}") {
        fun createRoute(lenderId: String) = "new_loan_request/${sanitizeParam(lenderId)}"
    }
    data object NewLoanRequests : Screen("new_loan_request/{lenderId}") {
        fun createRoute(lenderId: String) = "new_loan_request/${sanitizeParam(lenderId)}"
    }
    data object LenderApprovedLoans : Screen("lender_approved_loans/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_approved_loans/${sanitizeParam(lenderId)}"
    }
    data object ApprovedLoans : Screen("lender_approved_loans/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_approved_loans/${sanitizeParam(lenderId)}"
    }
    data object TodaysDue : Screen("todays_due/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_due/${sanitizeParam(lenderId)}"
    }
    data object TodaysPayment : Screen("todays_payment/{lenderId}") {
        fun createRoute(lenderId: String) = "todays_payment/${sanitizeParam(lenderId)}"
    }
    data object VerifyPayment : Screen("verify_payment/{lenderId}") {
        fun createRoute(lenderId: String) = "verify_payment/${sanitizeParam(lenderId)}"
    }
    data object TrackPayments : Screen("track_payments/{lenderId}") {
        fun createRoute(lenderId: String) = "track_payments/${sanitizeParam(lenderId)}"
    }
    data object MasterClient : Screen("master_client/{lenderId}") {
        fun createRoute(lenderId: String) = "master_client/${sanitizeParam(lenderId)}"
    }
    data object LenderLedger : Screen("lender_ledger/{lenderId}") {
        fun createRoute(lenderId: String) = "lender_ledger/${sanitizeParam(lenderId)}"
    }

    // ================= BORROWER =================
    data object BorrowerDashboard : Screen("borrower_dashboard/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_dashboard/${sanitizeParam(borrowerId)}"
    }
    data object BorrowerPayEmi : Screen("borrower_pay_emi/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_pay_emi/${sanitizeParam(borrowerId)}"
    }
    data object BorrowerApplyLoan : Screen("borrower_apply_loan/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_apply_loan/${sanitizeParam(borrowerId)}"
    }
    data object ApplyLoan : Screen("borrower_apply_loan/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_apply_loan/${sanitizeParam(borrowerId)}"
    }
    data object CustomerApprovedLoans : Screen("customer_approved_loans/{borrowerId}") {
        fun createRoute(borrowerId: String) = "customer_approved_loans/${sanitizeParam(borrowerId)}"
    }
    data object BorrowerLedger : Screen("borrower_ledger/{borrowerId}") {
        fun createRoute(borrowerId: String) = "borrower_ledger/${sanitizeParam(borrowerId)}"
    }
}
