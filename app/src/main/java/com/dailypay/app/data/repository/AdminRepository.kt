package com.dailypay.app.data.repository

import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.PasswordResetRequest
import com.dailypay.app.data.model.ResetStatus
import com.dailypay.app.data.model.SubscriptionMonitorItem
import com.dailypay.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from

class AdminRepository {

    private val db = SupabaseClientProvider.db

    /**
     * Creates new lender profile (triggers 30-day trial automatically)
     */
    suspend fun createLender(lender: Lender): Result<Unit> = runCatching {
        db.from("lenders").insert(lender)
    }

    /**
     * Retrieves all registered lenders
     */
    suspend fun getAllLenders(): Result<List<Lender>> = runCatching {
        db.from("lenders").select().decodeList<Lender>()
    }

    /**
     * Updates an existing lender profile
     */
    suspend fun updateLender(lender: Lender): Result<Unit> = runCatching {
        lender.id ?: throw IllegalArgumentException("Lender ID is required for updates")
        db.from("lenders").update(lender) {
            filter {
                eq("id", lender.id)
            }
        }
    }

    /**
     * Retrieves subscriptions sorted by remaining days
     */
    suspend fun getSubscriptionMonitor(): Result<List<SubscriptionMonitorItem>> = runCatching {
        db.from("v_admin_subscription_monitor").select().decodeList<SubscriptionMonitorItem>()
    }

    /**
     * Retrieves active password reset requests
     */
    suspend fun getPasswordResetRequests(): Result<List<PasswordResetRequest>> = runCatching {
        db.from("password_reset_requests")
            .select {
                filter {
                    eq("status", "PENDING")
                }
            }.decodeList<PasswordResetRequest>()
    }

    /**
     * Updates reset status after sending WhatsApp verification code
     */
    suspend fun markResetCodeSent(requestId: String, code: String): Result<Unit> = runCatching {
        db.from("password_reset_requests").update(
            mapOf("status" to ResetStatus.SENT.name, "reset_code" to code)
        ) {
            filter {
                eq("id", requestId)
            }
        }
    }
}
