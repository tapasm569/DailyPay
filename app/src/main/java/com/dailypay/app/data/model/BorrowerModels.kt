package com.dailypay.app.data.model

import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.serialization.Serializable

@IgnoreExtraProperties
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

@IgnoreExtraProperties
@Serializable
data class BorrowerDashboardSummary(
    val borrowerId: String = "",
    val activeLoansCount: Int = 0,
    val totalBorrowed: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val todayDue: Double = 0.0,
    val todayPaid: Double = 0.0
)
