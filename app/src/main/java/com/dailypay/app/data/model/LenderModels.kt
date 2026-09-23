package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Lender(
    val id: String? = null,
    val name: String,
    @SerialName("owner_name")
    val ownerName: String? = null,
    @SerialName("business_name")
    val businessName: String? = null,
    @SerialName("mobile_number")
    val mobileNumber: String,
    @SerialName("village_town")
    val villageTown: String? = null,
    @SerialName("post_office")
    val postOffice: String? = null,
    val dist: String? = null,
    val address: String? = null,
    @SerialName("password_hash")
    val passwordHash: String? = null,
    val status: String = "RUN", // "RUN" or "STOP"
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class LenderLedgerSummary(
    val lenderId: String,
    val totalDisbursed: Double,
    val totalDueBalance: Double,
    val totalPaid: Double,
    val totalRemainingBalance: Double
)
