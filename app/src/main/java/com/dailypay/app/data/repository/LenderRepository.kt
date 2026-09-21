package com.dailypay.app.data.repository

import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.LenderLedgerSummary
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.LoanStatus
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class LenderRepository {

    private val db = SupabaseClientProvider.db
    private val storage = SupabaseClientProvider.storage

    /**
     * Registers a new borrower, uploads KYC docs & photo to Supabase Storage,
     * and persists the resulting URLs in the borrowers table.
     */
    suspend fun addBorrower(
        borrower: Borrower,
        aadhaarBytes: ByteArray?,
        panBytes: ByteArray?,
        avatarBytes: ByteArray?
    ): Result<Unit> = runCatching {
        val timestamp = System.currentTimeMillis()
        val mobile = borrower.mobileNumber

        var aadhaarUrl: String? = null
        var panUrl: String? = null
        var avatarUrl: String? = null

        // 1. Upload Aadhaar Card
        if (aadhaarBytes != null && aadhaarBytes.isNotEmpty()) {
            val path = "aadhaar_${mobile}_$timestamp.jpg"
            storage.from("kyc-documents").upload(path = path, data = aadhaarBytes, upsert = true)
            aadhaarUrl = storage.from("kyc-documents").publicUrl(path)
        }

        // 2. Upload PAN Card
        if (panBytes != null && panBytes.isNotEmpty()) {
            val path = "pan_${mobile}_$timestamp.jpg"
            storage.from("kyc-documents").upload(path = path, data = panBytes, upsert = true)
            panUrl = storage.from("kyc-documents").publicUrl(path)
        }

        // 3. Upload Profile Photo
        if (avatarBytes != null && avatarBytes.isNotEmpty()) {
            val path = "avatar_${mobile}_$timestamp.jpg"
            storage.from("profile-avatars").upload(path = path, data = avatarBytes, upsert = true)
            avatarUrl = storage.from("profile-avatars").publicUrl(path)
        }

        // 4. Save Borrower with public image URLs
        val updatedBorrower = borrower.copy(
            aadhaarCardUrl = aadhaarUrl,
            panCardUrl = panUrl,
            profilePicUrl = avatarUrl
        )

        db.from("borrowers").insert(updatedBorrower)
    }

    /**
     * Fetches all registered borrowers under this lender
     */
    suspend fun getBorrowers(lenderId: String): Result<List<Borrower>> = runCatching {
        db.from("borrowers")
            .select {
                filter {
                    eq("lender_id", lenderId)
                }
            }.decodeList<Borrower>()
    }

    /**
     * Updates an existing borrower's profile details
     */
    suspend fun updateBorrower(borrower: Borrower): Result<Unit> = runCatching {
        val id = borrower.id ?: throw IllegalArgumentException("Borrower ID cannot be null")
        db.from("borrowers").update(
            mapOf(
                "name" to borrower.name,
                "mobile_number" to borrower.mobileNumber,
                "village_city" to borrower.villageCity,
                "post_office" to borrower.postOffice,
                "police_station" to borrower.policeStation,
                "dist" to borrower.dist,
                "password_hash" to borrower.passwordHash
            )
        ) {
            filter { eq("id", id) }
        }
    }

    /**
     * Deletes a borrower profile
     */
    suspend fun deleteBorrower(borrowerId: String): Result<Unit> = runCatching {
        db.from("borrowers").delete {
            filter { eq("id", borrowerId) }
        }
    }

    /**
     * Fetches daily dues from the database view
     */
    suspend fun getDailyDues(lenderId: String): Result<List<DailyDueItem>> = runCatching {
        db.from("v_lender_daily_dues")
            .select {
                filter {
                    eq("lender_id", lenderId)
                }
            }.decodeList<DailyDueItem>()
    }

    /**
     * Records a repayment transaction
     */
    suspend fun recordRepayment(repayment: Repayment): Result<Unit> = runCatching {
        db.from("repayments").insert(repayment)
    }

    /**
     * Disburses a loan manually
     */
    suspend fun giveLoanManually(loan: Loan): Result<Unit> = runCatching {
        db.from("loans").insert(loan)
    }

    /**
     * Fetches pending loan applications awaiting lender approval
     */
    suspend fun getPendingLoanRequests(lenderId: String): Result<List<Loan>> = runCatching {
        db.from("loans")
            .select {
                filter {
                    eq("lender_id", lenderId)
                    eq("status", LoanStatus.PENDING.name)
                }
            }.decodeList<Loan>()
    }

    /**
     * Approves and activates a requested loan
     */
    suspend fun approveAndDisburseLoan(
        loanId: String,
        interestRate: Double,
        disbursementMode: PaymentMode,
        disbursementRef: String
    ): Result<Unit> = runCatching {
        db.from("loans").update(
            mapOf(
                "status" to LoanStatus.ACTIVE.name,
                "monthly_interest_rate" to interestRate,
                "disbursement_mode" to disbursementMode.name,
                "disbursement_ref" to disbursementRef
            )
        ) {
            filter {
                eq("id", loanId)
            }
        }
    }

    /**
     * Calculates summary metrics for the ledger screen
     */
    suspend fun getLedgerSummary(lenderId: String): Result<LenderLedgerSummary> = runCatching {
        val dues = getDailyDues(lenderId).getOrThrow()
        val totalDisbursed = dues.sumOf { it.totalPayable }
        val totalDue = dues.sumOf { it.todayDueBalance }
        val totalPaid = dues.sumOf { it.totalPaid }
        val totalRemaining = dues.sumOf { it.remainingBalance }

        LenderLedgerSummary(
            lenderId = lenderId,
            totalDisbursed = totalDisbursed,
            totalDueBalance = totalDue,
            totalPaid = totalPaid,
            totalRemainingBalance = totalRemaining
        )
    }
}
