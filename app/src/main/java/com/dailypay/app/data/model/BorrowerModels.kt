package com.dailypay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Borrower(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("lender_id")
    val lenderId: String = "",

    val name: String = "",

    @SerialName("mobile_number")
    val mobileNumber: String = "",

    @SerialName("village_city")
    val villageCity: String? = null,

    @SerialName("post_office")
    val postOffice: String? = null,

    @SerialName("police_station")
    val policeStation: String? = null,

    val dist: String? = null,

    @SerialName("aadhaar_card_url")
    val aadhaarCardUrl: String? = null,

    @SerialName("pan_card_url")
    val panCardUrl: String? = null,

    @SerialName("profile_pic_url")
    val profilePicUrl: String? = null,

    @SerialName("password_hash")
    val passwordHash: String = "",

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class BorrowerKycDocs(
    val aadhaarBytes: ByteArray? = null,
    val panBytes: ByteArray? = null,
    val avatarBytes: ByteArray? = null
)
