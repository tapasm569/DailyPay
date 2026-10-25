package com.dailypay.app.data.model

import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.serialization.Serializable

@IgnoreExtraProperties
@Serializable
data class Lender(
    val id: String? = null,
    val name: String = "",
    val ownerName: String? = null,
    val businessName: String? = null,
    val mobileNumber: String = "",
    val upiId: String? = null,
    val address: String? = null,
    val villageTown: String? = null,
    val postOffice: String? = null,
    val dist: String? = null,
    val status: String = "RUN",
    val passwordHash: String? = null,
    val createdAt: String? = null
)

@IgnoreExtraProperties
@Serializable
data class DailyDueItem(
    val loanId: String = "",
    val borrowerId: String = "",
    val borrowerName: String = "",
    val borrowerMobile: String = "",
    val dailyInstallment: Double = 0.0,
    val todayDueBalance: Double = 0.0,
    val todayPaidAmount: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val totalPaid: Double = 0.0
)

@IgnoreExtraProperties
@Serializable
data class LenderLedgerSummary(
    val lenderId: String = "",
    val totalDisbursed: Double = 0.0,
    val totalDueBalance: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalRemainingBalance: Double = 0.0
)

@IgnoreExtraProperties
@Serializable
data class Subscription(
    val id: String? = null,
    val lenderId: String = "",
    val planName: String = "",
    val amount: Double = 0.0,
    val status: String = "ACTIVE",
    val startDate: String? = null,
    val endDate: String? = null,
    val createdAt: String? = null
)
