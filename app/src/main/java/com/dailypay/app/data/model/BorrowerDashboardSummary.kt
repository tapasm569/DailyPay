package com.dailypay.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BorrowerDashboardSummary(
    val borrowerId: String,
    val totalBorrowed: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val todayDue: Double = 0.0,
    val activeLoansCount: Int = 0
)
