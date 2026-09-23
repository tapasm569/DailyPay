package com.dailypay.app.data.repository

import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.Subscription
import com.dailypay.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AdminRepository {

    private val db = SupabaseClientProvider.db

    suspend fun getAllLenders(): Result<List<Lender>> = runCatching {
        db.from("lenders")
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Lender>()
    }

    suspend fun createLender(lender: Lender): Result<Unit> = runCatching {
        db.from("lenders").insert(lender)
    }

    suspend fun updateLender(lender: Lender): Result<Unit> = runCatching {
        val lenderId = lender.id ?: throw IllegalArgumentException("Lender ID cannot be null")
        db.from("lenders").update(
            buildJsonObject {
                put("name", lender.name.trim())
                lender.ownerName?.let { put("owner_name", it.trim()) }
                lender.businessName?.let { put("business_name", it.trim()) }
                put("mobile_number", lender.mobileNumber.trim())
                lender.upiId?.let { put("upi_id", it.trim()) }
                lender.villageTown?.let { put("village_town", it.trim()) }
                lender.postOffice?.let { put("post_office", it.trim()) }
                lender.dist?.let { put("dist", it.trim()) }
                lender.address?.let { put("address", it.trim()) }
                put("status", lender.status.uppercase())
                if (!lender.passwordHash.isNullOrBlank()) {
                    put("password_hash", lender.passwordHash)
                }
            }
        ) {
            filter { eq("id", lenderId) }
        }
    }

    suspend fun updateLenderStatus(lenderId: String, status: String): Result<Unit> = runCatching {
        db.from("lenders").update(
            buildJsonObject {
                put("status", status.uppercase())
            }
        ) {
            filter { eq("id", lenderId) }
        }
    }

    suspend fun deleteLender(lenderId: String): Result<Unit> = runCatching {
        db.from("lenders").delete {
            filter { eq("id", lenderId) }
        }
    }

    suspend fun getAllSubscriptions(): Result<List<Subscription>> = runCatching {
        db.from("subscriptions")
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Subscription>()
    }
}
