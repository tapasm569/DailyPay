package com.dailypay.app.data.repository

import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from

class BorrowerRepository {

    private val db = SupabaseClientProvider.db

    /**
     * Borrower submits a loan request
     */
    suspend fun applyLoan(loan: Loan): Result<Unit> = runCatching {
        db.from("loans").insert(loan)
    }

    /**
     * Fetches current active loan for the borrower
     */
    suspend fun getActiveLoan(borrowerId: String): Result<Loan?> = runCatching {
        db.from("loans")
            .select {
                filter {
                    eq("borrower_id", borrowerId)
                    eq("status", "ACTIVE")
                }
            }.decodeSingleOrNull<Loan>()
    }

    /**
     * Fetches borrower's current daily due and remaining balance
     */
    suspend fun getBorrowerDueDetails(borrowerId: String): Result<DailyDueItem?> = runCatching {
        db.from("v_lender_daily_dues")
            .select {
                filter {
                    eq("borrower_id", borrowerId)
                }
            }.decodeSingleOrNull<DailyDueItem>()
    }

    /**
     * Fetches full payment history log for a loan
     */
    suspend fun getRepaymentHistory(loanId: String): Result<List<Repayment>> = runCatching {
        db.from("repayments")
            .select {
                filter {
                    eq("loan_id", loanId)
                }
            }.decodeList<Repayment>()
    }
}
