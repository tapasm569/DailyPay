package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Lender(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    val name: String = "",

    @SerialName("owner_name")
    val ownerName: String = "",

    @SerialName("business_name")
    val businessName: String = "",

    @SerialName("mobile_number")
    val mobileNumber: String = "",

    @SerialName("village_town")
    val villageTown: String? = null,

    @SerialName("post_office")
    val postOffice: String? = null,

    val dist: String? = null,

    val address: String? = null,

    @SerialName("password_hash")
    val passwordHash: String = "",

    @SerialName("upi_id")
    val upiId: String? = null,

    val status: String = "ACTIVE",

    @SerialName("subscription_status")
    val subscriptionStatus: String = "ACTIVE",

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class Subscription(
    val id: String? = null,

    @SerialName("lender_id")
    val lenderId: String,

    @SerialName("plan_name")
    val planName: String = "MONTHLY",

    @SerialName("start_date")
    val startDate: String? = null,

    @SerialName("end_date")
    val endDate: String? = null,

    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class LenderLedgerSummary(
    @SerialName("lender_id")
    val lenderId: String = "",

    @SerialName("total_disbursed")
    val totalDisbursed: Double = 0.0,

    @SerialName("total_due_balance")
    val totalDueBalance: Double = 0.0,

    @SerialName("total_paid")
    val totalPaid: Double = 0.0,

    @SerialName("total_remaining_balance")
    val totalRemainingBalance: Double = 0.0
)
