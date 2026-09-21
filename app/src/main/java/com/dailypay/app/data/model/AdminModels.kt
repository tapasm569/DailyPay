package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("lender_id")
    val lenderId: String? = null,

    @SerialName("mobile_number")
    val mobileNumber: String? = null,

    @SerialName("user_role")
    val userRole: UserRole = UserRole.LENDER,

    val status: ResetStatus = ResetStatus.PENDING,

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
