package com.dailypay.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Borrower(
    val id: String? = null,
    val lenderId: String = "",
    val name: String = "",
    val mobileNumber: String = "",
    val villageCity: String? = null,
    val postOffice: String? = null,
    val policeStation: String? = null,
    val dist: String? = null,
    val aadhaarCardUrl: String? = null,
    val panCardUrl: String? = null,
    val profilePicUrl: String? = null,
    val passwordHash: String? = null,
    val createdAt: String? = null
)
