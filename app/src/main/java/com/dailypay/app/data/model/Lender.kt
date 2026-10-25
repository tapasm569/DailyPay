package com.dailypay.app.data.model

import kotlinx.serialization.Serializable

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
