package com.dailypay.app.data.model

import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.serialization.Serializable

@IgnoreExtraProperties
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

@IgnoreExtraProperties
@Serializable
data class ApprovedLoanDetail(
    val loanId: String = "",
    val borrowerId: String = "",
    val borrowerName: String = "",
    val borrowerMobile: String = "",
    val principalAmount: Double = 0.0,
    val totalPayable: Double = 0.0,
    val dailyInstallment: Double = 0.0,
    val tenureDays: Int = 30,
    val startDate: String = "",
    val endDate: String = "",
    val status: String = "",
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val todayPaid: Double = 0.0,
    val todayDue: Double = 0.0
)
