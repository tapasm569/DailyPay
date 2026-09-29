package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Lender(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String = "",

    @SerialName("mobile_number")
    val mobileNumber: String = "",

    @SerialName("password_hash")
    val passwordHash: String = "",

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class LenderDashboardSummary(
    @SerialName("total_borrowers")
    val totalBorrowers: Int = 0,

    @SerialName("total_loans_disbursed")
    val totalLoansDisbursed: Double = 0.0,

    @SerialName("todays_due_amount")
    val todaysDueAmount: Double = 0.0,

    @SerialName("todays_collected_amount")
    val todaysCollectedAmount: Double = 0.0,

    @SerialName("pending_verifications_count")
    val pendingVerificationsCount: Int = 0
)
