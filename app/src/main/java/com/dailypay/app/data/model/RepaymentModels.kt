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

    @SerialName("payment_date")
    val paymentDate: String? = null,

    @SerialName("amount_paid")
    val amountPaid: Double,

    @SerialName("payment_mode")
    val paymentMode: PaymentMode = PaymentMode.CASH,

    val notes: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
