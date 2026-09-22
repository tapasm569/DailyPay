package com.dailypay.app.data.repository

import com.dailypay.app.data.model.*
import com.dailypay.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AdminRepository {

    private val db = SupabaseClientProvider.db

    // Create a new lender
    suspend fun createLender(lender: Lender): Result<Unit> = runCatching {
        db.from("lenders").insert(lender)
    }

    // Get all registered lenders for Manage Lender screen
    suspend fun getAllLenders(): Result<List<Lender>> = runCatching {
        db.from("lenders")
            .select {
                order("created_at", Order.DESCENDING)
            }.decodeList<Lender>()
    }

    // Update lender profile details & password
    suspend fun updateLender(lender: Lender): Result<Unit> = runCatching {
        val id = lender.id ?: throw IllegalArgumentException("Lender ID cannot be null")
        db.from("lenders").update(
            buildJsonObject {
                put("name", lender.name)
                put("owner_name", lender.ownerName)
                put("business_name", lender.businessName)
                put("mobile_number", lender.mobileNumber)
                lender.villageTown?.let { put("village_town", it) }
                lender.postOffice?.let { put("post_office", it) }
                lender.dist?.let { put("dist", it) }
                lender.address?.let { put("address", it) }
                lender.upiId?.let { put("upi_id", it) }
                if (lender.passwordHash.isNotBlank()) {
                    put("password_hash", lender.passwordHash)
                }
            }
        ) {
            filter { eq("id", id) }
        }
    }

    // Delete lender account
    suspend fun deleteLender(lenderId: String): Result<Unit> = runCatching {
        db.from("lenders").delete {
            filter { eq("id", lenderId) }
        }
    }

    // Subscriptions monitor list
    suspend fun getSubscriptionMonitor(): Result<List<SubscriptionMonitorItem>> = runCatching {
        db.from("v_subscription_monitors")
            .select()
            .decodeList<SubscriptionMonitorItem>()
    }

    // Update subscription plan
    suspend fun updateSubscription(subscription: Subscription): Result<Unit> = runCatching {
        val id = subscription.id ?: throw IllegalArgumentException("Subscription ID cannot be null")
        db.from("subscriptions").update(
            buildJsonObject {
                put("status", subscription.status.name)
                subscription.startDate?.let { put("start_date", it) }
                subscription.endDate?.let { put("end_date", it) }
            }
        ) {
            filter { eq("id", id) }
        }
    }

    // Password reset requests
    suspend fun getPasswordResetRequests(): Result<List<PasswordResetRequest>> = runCatching {
        db.from("password_reset_requests")
            .select {
                order("created_at", Order.DESCENDING)
            }.decodeList<PasswordResetRequest>()
    }

    // Resolve password reset
    suspend fun resolvePasswordResetRequest(requestId: String, status: ResetStatus): Result<Unit> = runCatching {
        db.from("password_reset_requests").update(
            buildJsonObject {
                put("status", status.name)
            }
        ) {
            filter { eq("id", requestId) }
        }
    }
}
