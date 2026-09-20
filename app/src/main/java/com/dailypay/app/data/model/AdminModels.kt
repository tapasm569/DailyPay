package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminUser(
    val id: String,
    val name: String,
    @SerialName("mobile_number") val mobileNumber: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SubscriptionMonitorItem(
    @SerialName("lender_id") val lenderId: String,
    @SerialName("lender_name") val lenderName: String,
    @SerialName("business_name") val businessName: String,
    @SerialName("mobile_number") val mobileNumber: String,
    @SerialName("subscription_id") val subscriptionId: String,
    @SerialName("plan_name") val planName: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val status: String,
    @SerialName("days_remaining") val daysRemaining: Int,
    @SerialName("needs_reminder") val needsReminder: Boolean
)
