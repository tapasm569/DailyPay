package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Repayment(
    val id: String? = null,
    @SerialName("loan_id")
    val loanId: String,
    @SerialName("borrower_id")
    val borrowerId: String,
    @SerialName("lender_id")
    val lenderId: String,
    @SerialName("amount_paid")
    val amountPaid: Double,
    @SerialName("payment_date")
    val paymentDate: String,
    @SerialName("payment_mode")
    val paymentMode: PaymentMode = PaymentMode.CASH,
    @SerialName("transaction_ref")
    val transactionRef: String? = null, // Stores 12-digit UPI UTR / Bank Reference Number
    val status: String = "VERIFIED",    // "PENDING" (awaiting lender approval), "VERIFIED", or "REJECTED"
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class DailyDueItem(
    @SerialName("loan_id")
    val loanId: String = "",
    @SerialName("lender_id")
    val lenderId: String = "",
    @SerialName("borrower_id")
    val borrowerId: String = "",
    @SerialName("borrower_name")
    val borrowerName: String = "",
    @SerialName("borrower_mobile")
    val borrowerMobile: String = "",
    @SerialName("daily_installment")
    val dailyInstallment: Double = 0.0,
    @SerialName("total_payable")
    val totalPayable: Double = 0.0,
    @SerialName("total_paid")
    val totalPaid: Double = 0.0,
    @SerialName("today_paid_amount")
    val todayPaidAmount: Double = 0.0,
    @SerialName("remaining_balance")
    val remainingBalance: Double = 0.0,
    @SerialName("today_due_balance")
    val todayDueBalance: Double = 0.0
)
