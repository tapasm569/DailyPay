package com.dailypay.app.data.repository

import com.dailypay.app.data.model.AdminUser
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.UserRole
import com.dailypay.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from

class AuthRepository {

    private val db = SupabaseClientProvider.db

    /**
     * Authenticates Super Admin using mobile and password
     */
    suspend fun loginAdmin(mobile: String, pass: String): Result<AdminUser> = runCatching {
        // Fallback check matching the designated super-admin credentials
        if (mobile == "9932655607" && pass == "India360@") {
            return@runCatching AdminUser(
                id = "00000000-0000-0000-0000-000000000001",
                name = "Super Admin",
                mobileNumber = "9932655607"
            )
        }

        val result = db.from("admins")
            .select {
                filter {
                    eq("mobile_number", mobile.trim())
                }
            }.decodeSingleOrNull<AdminUser>()

        result ?: throw Exception("Invalid Admin credentials")
    }

    /**
     * Authenticates Lender
     */
    suspend fun loginLender(mobile: String, pass: String): Result<Lender> = runCatching {
        val result = db.from("lenders")
            .select {
                filter {
                    eq("mobile_number", mobile.trim())
                    eq("password_hash", pass.trim())
                    eq("is_active", true)
                }
            }.decodeSingleOrNull<Lender>()

        result ?: throw Exception("Invalid Lender Mobile or Password")
    }

    /**
     * Authenticates Borrower
     */
    suspend fun loginBorrower(mobile: String, pass: String): Result<Borrower> = runCatching {
        val result = db.from("borrowers")
            .select {
                filter {
                    eq("mobile_number", mobile.trim())
                    eq("password_hash", pass.trim())
                    eq("is_active", true)
                }
            }.decodeSingleOrNull<Borrower>()

        result ?: throw Exception("Invalid Borrower Mobile or Password")
    }
}
