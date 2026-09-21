package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Loan(
    val id: String? = null,

    @SerialName("lender_id")
    val lenderId: String,

    @SerialName("borrower_id")
    val borrowerId: String,

    @SerialName("principal_amount")
    val principalAmount: Double,

    @SerialName("monthly_interest_rate")
    val monthlyInterestRate: Double = 0.0,

    @SerialName("total_payable")
    val totalPayable: Double = 0.0,

    @SerialName("daily_installment")
    val dailyInstallment: Double = 0.0,

    @SerialName("tenure_days")
    val tenureDays: Int = 30,

    @SerialName("start_date")
    val startDate: String? = null,

    @SerialName("end_date")
    val endDate: String? = null,

    val status: LoanStatus = LoanStatus.PENDING,

    @SerialName("disbursement_mode")
    val disbursementMode: PaymentMode = PaymentMode.CASH,

    @SerialName("disbursement_ref")
    val disbursementRef: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class DailyDueItem(
    @SerialName("loan_id")
    val loanId: String = "",

    @SerialName("borrower_id")
    val borrowerId: String = "",

    @SerialName("lender_id")
    val lenderId: String = "",

    @SerialName("borrower_name")
    val borrowerName: String = "",

    @SerialName("borrower_mobile")
    val borrowerMobile: String = "",

    @SerialName("principal_amount")
    val principalAmount: Double = 0.0,

    @SerialName("total_payable")
    val totalPayable: Double = 0.0,

    @SerialName("daily_installment")
    val dailyInstallment: Double = 0.0,

    @SerialName("tenure_days")
    val tenureDays: Int = 30,

    @SerialName("start_date")
    val startDate: String? = null,

    @SerialName("end_date")
    val endDate: String? = null,

    @SerialName("total_paid")
    val totalPaid: Double = 0.0,

    @SerialName("remaining_balance")
    val remainingBalance: Double = 0.0,

    @SerialName("today_paid_amount")
    val todayPaidAmount: Double = 0.0,

    @SerialName("today_due_balance")
    val todayDueBalance: Double = 0.0
)
