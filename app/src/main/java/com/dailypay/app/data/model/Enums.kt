package com.dailypay.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    ADMIN,
    LENDER,
    BORROWER
}

@Serializable
enum class LoanStatus {
    PENDING,
    APPROVED,
    REJECTED,
    ACTIVE,
    CLOSED
}

@Serializable
enum class PaymentMode {
    CASH,
    UPI
}

@Serializable
enum class RepaymentStatus {
    PENDING,
    VERIFIED,
    REJECTED
}

@Serializable
enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    PENDING_VERIFICATION
}

@Serializable
enum class ResetStatus {
    PENDING,
    SENT,
    COMPLETED,
    EXPIRED
}