package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Repayment(
    val id: String? = null,
    @SerialName("loan_id") val loanId: String,
    @SerialName("borrower_id") val borrowerId: String,
    @SerialName("lender_id") val lenderId: String,
    @SerialName("payment_date") val paymentDate: String? = null,
    @SerialName("amount_paid") val amountPaid: Double,
    @SerialName("payment_mode") val paymentMode: PaymentMode = PaymentMode.CASH,
    @SerialName("transaction_ref") val transactionRef: String? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class PasswordResetRequest(
    val id: String? = null,
    @SerialName("user_type") val userType: String, // LENDER or BORROWER
    @SerialName("user_id") val userId: String,
    @SerialName("mobile_number") val mobileNumber: String,
    @SerialName("reset_code") val resetCode: String? = null,
    val status: ResetStatus = ResetStatus.PENDING,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null
)
