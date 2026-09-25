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
    val transactionRef: String? = null, // 12-digit UPI UTR / Bank Reference Number
    val status: String = "VERIFIED",    // "PENDING", "VERIFIED", or "REJECTED"
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class PendingPaymentItem(
    val repayment: Repayment,
    @SerialName("borrower_name")
    val borrowerName: String = "",
    @SerialName("borrower_mobile")
    val borrowerMobile: String = ""
)
