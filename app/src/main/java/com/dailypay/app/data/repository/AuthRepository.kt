package com.dailypay.app.data.repository

import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.remote.FirebaseDbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class LoginResult {
    object Admin : LoginResult()
    data class LenderUser(val lenderId: String) : LoginResult()
    data class BorrowerUser(val borrowerId: String) : LoginResult()
}

class AuthRepository {

    suspend fun unifiedLogin(mobile: String, pass: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanMobile = mobile.trim()
            val cleanPass = pass.trim()

            // 1. Check Super-Admin credentials
            if (cleanMobile == "9932655607" && cleanPass == "India360@") {
                return@runCatching LoginResult.Admin
            }

            // Check Admin node
            val adminSnapshot = FirebaseDbProvider.db.child("admins")
                .orderByChild("mobileNumber")
                .equalTo(cleanMobile)
                .get()
                .await()

            for (child in adminSnapshot.children) {
                val passHash = child.child("passwordHash").getValue(String::class.java)
                    ?: child.child("password_hash").getValue(String::class.java)
                if (passHash == cleanPass) {
                    return@runCatching LoginResult.Admin
                }
            }

            // 2. Check Lender node
            val lenderSnapshot = FirebaseDbProvider.lendersRef
                .orderByChild("mobileNumber")
                .equalTo(cleanMobile)
                .get()
                .await()

            for (child in lenderSnapshot.children) {
                val lender = child.getValue(Lender::class.java)
                val passHash = lender?.passwordHash?.ifBlank { null }
                    ?: child.child("password_hash").getValue(String::class.java)
                if (passHash == cleanPass) {
                    val id = lender?.id?.takeIf { it.isNotBlank() } ?: child.key.orEmpty()
                    if (id.isNotBlank()) {
                        return@runCatching LoginResult.LenderUser(id)
                    }
                }
            }

            // 3. Check Borrower node
            val borrowerSnapshot = FirebaseDbProvider.borrowersRef
                .orderByChild("mobileNumber")
                .equalTo(cleanMobile)
                .get()
                .await()

            for (child in borrowerSnapshot.children) {
                val borrower = child.getValue(Borrower::class.java)
                val passHash = borrower?.passwordHash?.ifBlank { null }
                    ?: child.child("password_hash").getValue(String::class.java)
                if (passHash == cleanPass) {
                    val id = borrower?.id?.takeIf { it.isNotBlank() } ?: child.key.orEmpty()
                    if (id.isNotBlank()) {
                        return@runCatching LoginResult.BorrowerUser(id)
                    }
                }
            }

            throw Exception("Invalid credentials. No account found for this mobile and password.")
        }
    }
}
