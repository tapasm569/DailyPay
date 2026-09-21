package com.dailypay.app.data.repository

import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from

sealed class LoginResult {
    object Admin : LoginResult()
    data class LenderUser(val lenderId: String) : LoginResult()
    data class BorrowerUser(val borrowerId: String) : LoginResult()
}

class AuthRepository {

    private val db = SupabaseClientProvider.db

    /**
     * Unified login: Automatically identifies whether the mobile/password belongs to
     * an Admin, a Lender, or a Borrower/Customer.
     */
    suspend fun unifiedLogin(mobile: String, pass: String): Result<LoginResult> = runCatching {
        val cleanMobile = mobile.trim()
        val cleanPass = pass.trim()

        // 1. Check Super-Admin credentials
        if (cleanMobile == "9932655607" && cleanPass == "India360@") {
            return@runCatching LoginResult.Admin
        }

        // Check Admin table (if configured in Supabase)
        val adminRecord = runCatching {
            db.from("admins")
                .select {
                    filter {
                        eq("mobile_number", cleanMobile)
                        eq("password_hash", cleanPass)
                    }
                }.decodeSingleOrNull<Map<String, String>>()
        }.getOrNull()

        if (adminRecord != null) {
            return@runCatching LoginResult.Admin
        }

        // 2. Check Lender table
        val lender = runCatching {
            db.from("lenders")
                .select {
                    filter {
                        eq("mobile_number", cleanMobile)
                        eq("password_hash", cleanPass)
                    }
                }.decodeSingleOrNull<Lender>()
        }.getOrNull()

        if (lender != null && lender.id != null) {
            return@runCatching LoginResult.LenderUser(lender.id)
        }

        // 3. Check Borrower table
        val borrower = runCatching {
            db.from("borrowers")
                .select {
                    filter {
                        eq("mobile_number", cleanMobile)
                        eq("password_hash", cleanPass)
                    }
                }.decodeSingleOrNull<Borrower>()
        }.getOrNull()

        if (borrower != null && borrower.id != null) {
            return@runCatching LoginResult.BorrowerUser(borrower.id)
        }

        throw Exception("Invalid credentials. No account found for this mobile and password.")
    }
}
