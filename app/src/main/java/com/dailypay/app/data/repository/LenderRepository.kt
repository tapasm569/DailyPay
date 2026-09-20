package com.dailypay.app.data.repository

import com.dailypay.app.data.model.*
import com.dailypay.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class LenderRepository {

    private val db = SupabaseClientProvider.db
    private val storage = SupabaseClientProvider.storage

    /**
     * Uploads KYC files to Supabase Storage bucket and creates Borrower record
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

        // 1. Upload Aadhaar Card (if present)
        aadhaarBytes?.let {
            val path = "aadhaar_${mobile}_$timestamp.jpg"
            storage["kyc-documents"].upload(path, it, upsert = true)
            aadhaarUrl = storage["kyc-documents"].publicUrl(path)
        }

        // 2. Upload PAN Card (if present)
        panBytes?.let {
            val path = "pan_${mobile}_$timestamp.jpg"
            storage["kyc-documents"].upload(path, it, upsert = true)
            panUrl = storage["kyc-documents"].publicUrl(path)
        }

        // 3. Upload Profile Avatar (if present)
        avatarBytes?.let {
            val path = "avatar_${mobile}_$timestamp.jpg"
            storage["profile-avatars"].upload(path, it, upsert = true)
            avatarUrl = storage["profile-avatars"].publicUrl(path)
        }

        // 4. Save to Database
        val completeBorrower = borrower.copy(
            aadhaarCardUrl = aadhaarUrl,
            panCardUrl = panUrl,
            profilePicUrl = avatarUrl
        )
        db.from("borrowers").insert(completeBorrower)
    }

    /**
     * Gets all borrowers registered under this lender
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
     * Gets pending loan requests awaiting approval
     */
    suspend fun getPendingLoanRequests(lenderId: String): Result<List<Loan>> = runCatching {
        db.from("loans")
            .select {
                filter {
                    eq("lender_id", lenderId)
                    eq("status", "PENDING")
                }
            }.decodeList<Loan>()
    }

    /**
     * Approves and disburses a loan request
     */
    suspend fun approveAndDisburseLoan(
        loanId: String,
        interestRate: Double,
        disbursementMode: PaymentMode,
        disbursementRef: String?
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
     * Manually creates and activates a loan
     */
    suspend fun giveLoanManually(loan: Loan): Result<Unit> = runCatching {
        db.from("loans").insert(loan.copy(status = LoanStatus.ACTIVE))
    }

    /**
     * Fetches daily dues, rollover arrears, and today's collections
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
     * Records a repayment installment (Database trigger auto-closes loan if paid in full)
     */
    suspend fun recordRepayment(repayment: Repayment): Result<Unit> = runCatching {
        db.from("repayments").insert(repayment)
    }

    /**
     * Gets aggregate ledger card totals
     */
    suspend fun getLedgerSummary(lenderId: String): Result<LenderLedgerSummary?> = runCatching {
        db.from("v_lender_ledger_summary")
            .select {
                filter {
                    eq("lender_id", lenderId)
                }
            }.decodeSingleOrNull<LenderLedgerSummary>()
    }
}
