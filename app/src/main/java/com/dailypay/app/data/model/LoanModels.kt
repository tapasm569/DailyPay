package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Loan(
    val id: String? = null,
    @SerialName("lender_id") val lenderId: String,
    @SerialName("borrower_id") val borrowerId: String,
    @SerialName("principal_amount") val principalAmount: Double,
    @SerialName("tenure_days") val tenureDays: Int, // 30, 60, or 90
    @SerialName("monthly_interest_rate") val monthlyInterestRate: Double = 10.0,
    @SerialName("total_interest") val totalInterest: Double = 0.0,
    @SerialName("total_payable") val totalPayable: Double = 0.0,
    @SerialName("daily_installment") val dailyInstallment: Double = 0.0,
    @SerialName("total_paid") val totalPaid: Double = 0.0,
    @SerialName("disbursement_mode") val disbursementMode: PaymentMode = PaymentMode.CASH,
    @SerialName("disbursement_ref") val disbursementRef: String? = null,
    val status: LoanStatus = LoanStatus.PENDING,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// Maps to PostgreSQL view: v_lender_daily_dues
@Serializable
data class DailyDueItem(
    @SerialName("loan_id") val loanId: String,
    @SerialName("lender_id") val lenderId: String,
    @SerialName("borrower_id") val borrowerId: String,
    @SerialName("borrower_name") val borrowerName: String,
    @SerialName("borrower_mobile") val borrowerMobile: String,
    @SerialName("principal_amount") val principalAmount: Double,
    @SerialName("daily_installment") val dailyInstallment: Double,
    @SerialName("total_payable") val totalPayable: Double,
    @SerialName("total_paid") val totalPaid: Double,
    @SerialName("remaining_balance") val remainingBalance: Double,
    @SerialName("expected_due_to_date") val expectedDueToDate: Double,
    @SerialName("today_due_balance") val todayDueBalance: Double,
    @SerialName("today_paid_amount") val todayPaidAmount: Double
)

// Maps to PostgreSQL view: v_lender_ledger_summary
@Serializable
data class LenderLedgerSummary(
    @SerialName("lender_id") val lenderId: String,
    @SerialName("total_disbursed") val totalDisbursed: Double,
    @SerialName("total_paid") val totalPaid: Double,
    @SerialName("total_remaining_balance") val totalRemainingBalance: Double,
    @SerialName("total_due_balance") val totalDueBalance: Double
)
