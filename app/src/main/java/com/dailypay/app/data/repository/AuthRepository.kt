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
            val cleanPass = pass.trim()
            val rawMobile = mobile.trim()
            val normalized10Digit = extractLast10Digits(rawMobile)

            if (normalized10Digit.length < 10) {
                throw Exception("Please enter a valid 10-digit mobile number.")
            }

            // 1. Super-Admin hardcoded credentials
            if (normalized10Digit == "9932655607" && cleanPass == "India360@") {
                return@runCatching LoginResult.Admin
            }

            // 2. Check Admin node in Firebase (/admins)
            val adminsSnapshot = FirebaseDbProvider.db.child("admins").get().await()
            for (child in adminsSnapshot.children) {
                val dbMobile = child.child("mobileNumber").getValue(String::class.java)
                    ?: child.child("mobile_number").getValue(String::class.java)
                    ?: ""
                if (extractLast10Digits(dbMobile) == normalized10Digit) {
                    val passHash = child.child("passwordHash").getValue(String::class.java)
                        ?: child.child("password_hash").getValue(String::class.java)
                        ?: ""
                    if (passHash == cleanPass) {
                        return@runCatching LoginResult.Admin
                    }
                }
            }

            // 3. Check Lenders node (/lenders)
            val lendersSnapshot = FirebaseDbProvider.lendersRef.get().await()
            for (child in lendersSnapshot.children) {
                val dbMobile = child.child("mobileNumber").getValue(String::class.java)
                    ?: child.child("mobile_number").getValue(String::class.java)
                    ?: ""
                if (extractLast10Digits(dbMobile) == normalized10Digit) {
                    val passHash = child.child("passwordHash").getValue(String::class.java)
                        ?: child.child("password_hash").getValue(String::class.java)
                        ?: ""
                    if (passHash == cleanPass) {
                        val id = child.child("id").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                            ?: child.key.orEmpty()
                        if (id.isNotBlank()) {
                            return@runCatching LoginResult.LenderUser(id)
                        }
                    }
                }
            }

            // 4. Check Borrowers node (/borrowers)
            val borrowersSnapshot = FirebaseDbProvider.borrowersRef.get().await()
            for (child in borrowersSnapshot.children) {
                val dbMobile = child.child("mobileNumber").getValue(String::class.java)
                    ?: child.child("mobile_number").getValue(String::class.java)
                    ?: ""

                if (extractLast10Digits(dbMobile) == normalized10Digit) {
                    val passHash = child.child("passwordHash").getValue(String::class.java)
                        ?: child.child("password_hash").getValue(String::class.java)
                        ?: ""

                    // Password validation rule for borrowers:
                    // 1) If password was explicitly set, match it.
                    // 2) If password was left blank during creation by lender,
                    //    allow login with their mobile number or "123456".
                    val isPasswordValid = when {
                        passHash.isNotBlank() -> passHash == cleanPass
                        else -> cleanPass == normalized10Digit || cleanPass == "123456"
                    }

                    if (isPasswordValid) {
                        val id = child.child("id").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                            ?: child.key.orEmpty()

                        // If borrower had no password, save the password they just entered so it works next time
                        if (passHash.isBlank() && id.isNotBlank()) {
                            try {
                                child.ref.child("passwordHash").setValue(cleanPass)
                            } catch (_: Exception) {}
                        }

                        if (id.isNotBlank()) {
                            return@runCatching LoginResult.BorrowerUser(id)
                        }
                    }
                }
            }

            throw Exception("Account not found or password incorrect for this mobile number.")
        }
    }

    private fun extractLast10Digits(phone: String): String {
        val digitsOnly = phone.filter { it.isDigit() }
        return if (digitsOnly.length >= 10) {
            digitsOnly.takeLast(10)
        } else {
            digitsOnly
        }
    }
}
