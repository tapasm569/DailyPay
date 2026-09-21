package com.dailypay.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApprovedLoanDetail(
    val loanId: String,
    val borrowerId: String,
    val borrowerName: String,
    val borrowerMobile: String,
    val principalAmount: Double,
    val totalPayable: Double,
    val dailyInstallment: Double,
    val tenureDays: Int,
    val startDate: String,
    val endDate: String,
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val todayDue: Double = 0.0,
    val todayPaid: Double = 0.0,
    val status: String = "ACTIVE"
)