package com.dailypay.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Loan(
    val id: String? = null,
    val lenderId: String = "",
    val borrowerId: String = "",
    val principalAmount: Double = 0.0,
    val monthlyInterestRate: Double = 10.0,
    val tenureDays: Int = 30,
    val totalPayable: Double = 0.0,
    val dailyInstallment: Double = 0.0,
    val disbursementMode: PaymentMode = PaymentMode.CASH,
    val disbursementRef: String? = null,
    val status: LoanStatus = LoanStatus.PENDING,
    val startDate: String? = null,
    val endDate: String? = null,
    val createdAt: String? = null
)
