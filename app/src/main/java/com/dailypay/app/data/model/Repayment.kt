package com.dailypay.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Repayment(
    val id: String? = null,
    val loanId: String = "",
    val borrowerId: String = "",
    val lenderId: String = "",
    val paymentDate: String? = null,
    val amountPaid: Double = 0.0,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val utrReference: String? = null,
    val notes: String? = null,
    val status: RepaymentStatus? = RepaymentStatus.VERIFIED,
    val createdAt: String? = null
)
