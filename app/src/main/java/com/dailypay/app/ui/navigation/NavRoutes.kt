package com.dailypay.app.ui.navigation

sealed class Screen(val route: String) {

    // ================= AUTH =================
    object Login : Screen("login")

    // ================= ADMIN =================
    object AdminDashboard : Screen("admin_dashboard")
    object CreateLender : Screen("create_lender")
    object AdminSubscription : Screen("admin_subscription")
    object AdminReminder : Screen("admin_reminder")

    // ================= LENDER =================
    object LenderHome : Screen("lender_home/{lenderId}") {
        fun createRoute(lenderId: String): String = "lender_home/$lenderId"
    }

    object LenderDashboard : Screen("lender_dashboard/{lenderId}") {
        fun createRoute(lenderId: String): String = "lender_dashboard/$lenderId"
    }

    object AddBorrower : Screen("add_borrower/{lenderId}") {
        fun createRoute(lenderId: String): String = "add_borrower/$lenderId"
    }

    object GiveLoanManual : Screen("give_loan_manual/{lenderId}") {
        fun createRoute(lenderId: String): String = "give_loan_manual/$lenderId"
    }

    object NewLoanRequest : Screen("new_loan_request/{lenderId}") {
        fun createRoute(lenderId: String): String = "new_loan_request/$lenderId"
    }

    object LenderApprovedLoans : Screen("lender_approved_loans/{lenderId}") {
        fun createRoute(lenderId: String): String = "lender_approved_loans/$lenderId"
    }

    object SetInterest : Screen("set_interest/{lenderId}") {
        fun createRoute(lenderId: String): String = "set_interest/$lenderId"
    }

    object TodaysDue : Screen("todays_due/{lenderId}") {
        fun createRoute(lenderId: String): String = "todays_due/$lenderId"
    }

    object TodaysPayment : Screen("todays_payment/{lenderId}") {
        fun createRoute(lenderId: String): String = "todays_payment/$lenderId"
    }

    object MasterClient : Screen("master_client/{lenderId}") {
        fun createRoute(lenderId: String): String = "master_client/$lenderId"
    }

    object LenderLedger : Screen("lender_ledger/{lenderId}") {
        fun createRoute(lenderId: String): String = "lender_ledger/$lenderId"
    }

    // ================= BORROWER / CUSTOMER =================
    object BorrowerDashboard : Screen("borrower_dashboard/{borrowerId}") {
        fun createRoute(borrowerId: String): String = "borrower_dashboard/$borrowerId"
    }

    object BorrowerApplyLoan : Screen("borrower_apply_loan/{borrowerId}") {
        fun createRoute(borrowerId: String): String = "borrower_apply_loan/$borrowerId"
    }

    object CustomerApprovedLoans : Screen("customer_approved_loans/{borrowerId}") {
        fun createRoute(borrowerId: String): String = "customer_approved_loans/$borrowerId"
    }

    object BorrowerLedger : Screen("borrower_ledger/{borrowerId}") {
        fun createRoute(borrowerId: String): String = "borrower_ledger/$borrowerId"
    }
}
