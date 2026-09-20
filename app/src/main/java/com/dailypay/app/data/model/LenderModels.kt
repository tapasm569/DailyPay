package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Lender(
    val id: String? = null,
    val name: String,
    @SerialName("mobile_number") val mobileNumber: String,
    @SerialName("business_name") val businessName: String,
    @SerialName("upi_id") val upiId: String,
    val address: String? = null,
    @SerialName("village_town") val villageTown: String? = null,
    @SerialName("post_office") val postOffice: String? = null,
    val dist: String? = null,
    @SerialName("password_hash") val passwordHash: String,
    @SerialName("monthly_interest_rate") val monthlyInterestRate: Double = 10.0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class Subscription(
    val id: String? = null,
    @SerialName("lender_id") val lenderId: String,
    @SerialName("plan_name") val planName: String = "30-Day Free Trial",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("amount_paid") val amountPaid: Double = 0.0,
    @SerialName("payment_reference") val paymentReference: String? = null,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    @SerialName("verified_by_admin") val verifiedByAdmin: Boolean = true
)
