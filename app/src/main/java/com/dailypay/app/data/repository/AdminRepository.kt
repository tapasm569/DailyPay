package com.dailypay.app.data.repository

import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.Subscription
import com.dailypay.app.data.remote.FirebaseDbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class AdminRepository {

    suspend fun getAllLenders(): Result<List<Lender>> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = FirebaseDbProvider.lendersRef.get().await()
            snapshot.children.mapNotNull { it.getValue(Lender::class.java) }
        }
    }

    suspend fun createLender(lender: Lender): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val key = lender.id?.takeIf { it.isNotBlank() }
                ?: FirebaseDbProvider.lendersRef.push().key
                ?: UUID.randomUUID().toString()
            val finalLender = lender.copy(id = key)
            FirebaseDbProvider.lendersRef.child(key).setValue(finalLender).await()
            Unit
        }
    }

    suspend fun updateLender(lender: Lender): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val lenderId = lender.id?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Lender ID cannot be null")

            val updates = mutableMapOf<String, Any?>()
            updates["name"] = lender.name.trim()
            lender.ownerName?.let { updates["ownerName"] = it.trim() }
            lender.businessName?.let { updates["businessName"] = it.trim() }
            updates["mobileNumber"] = lender.mobileNumber.trim()
            lender.villageTown?.let { updates["villageTown"] = it.trim() }
            lender.postOffice?.let { updates["postOffice"] = it.trim() }
            lender.dist?.let { updates["dist"] = it.trim() }
            lender.address?.let { updates["address"] = it.trim() }
            updates["status"] = lender.status.uppercase()
            if (!lender.passwordHash.isNullOrBlank()) {
                updates["passwordHash"] = lender.passwordHash
            }

            FirebaseDbProvider.lendersRef.child(lenderId).updateChildren(updates).await()
            Unit
        }
    }

    suspend fun updateLenderStatus(lenderId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            FirebaseDbProvider.lendersRef
                .child(lenderId.trim())
                .child("status")
                .setValue(status.uppercase())
                .await()
            Unit
        }
    }

    suspend fun deleteLender(lenderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            FirebaseDbProvider.lendersRef.child(lenderId.trim()).removeValue().await()
            Unit
        }
    }

    suspend fun getAllSubscriptions(): Result<List<Subscription>> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = FirebaseDbProvider.db.child("subscriptions").get().await()
            snapshot.children.mapNotNull { it.getValue(Subscription::class.java) }
        }
    }
}
