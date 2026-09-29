package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Borrower(
    @SerialName("id")
    val id: String? = null,

    @SerialName("lender_id")
    val lenderId: String,

    @SerialName("name")
    val name: String,

    @SerialName("mobile_number")
    val mobileNumber: String,

    @SerialName("village_city")
    val villageCity: String? = null,

    @SerialName("post_office")
    val postOffice: String? = null,

    @SerialName("police_station")
    val policeStation: String? = null,

    @SerialName("dist")
    val dist: String? = null,

    @SerialName("password_hash")
    val passwordHash: String? = null,

    @SerialName("profile_pic_url")
    val profilePicUrl: String? = null,

    @SerialName("aadhaar_card_url")
    val aadhaarCardUrl: String? = null,

    @SerialName("pan_card_url")
    val panCardUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class BorrowerProfile(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("mobile_number")
    val mobileNumber: String,

    @SerialName("lender_id")
    val lenderId: String,

    @SerialName("total_active_loans")
    val totalActiveLoans: Int = 0,

    @SerialName("total_due_amount")
    val totalDueAmount: Double = 0.0
)
