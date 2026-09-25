package com.dailypay.app.data.repository

import com.dailypay.app.data.model.*
import com.dailypay.app.data.remote.SupabaseClientProvider
import com.dailypay.app.util.DateUtils
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.round

class BorrowerRepository {

    private val db = SupabaseClientProvider.db
    private val storage = SupabaseClientProvider.storage

    suspend fun getBorrowerProfile(borrowerId: String): Result<Borrower> = runCatching {
        db.from("borrowers")
            .select {
                filter { eq("id", borrowerId) }
            }.decodeSingle<Borrower>()
    }

    suspend fun getLenderProfile(lenderId: String): Result<Lender> = runCatching {
        db.from("lenders")
            .select {
                filter { eq("id", lenderId) }
            }.decodeSingle<Lender>()
    }

    suspend fun updateProfilePicture(
        borrowerId: String,
        mobileNumber: String,
        avatarBytes: ByteArray
    ): Result<String> = runCatching {
        val timestamp = System.currentTimeMillis()
        val path = "avatar_${mobileNumber}_$timestamp.jpg"

        storage.from("profile-avatars")
            .upload(path = path, data = avatarBytes, upsert = true)

        val publicUrl = storage.from("profile-avatars").publicUrl(path)

        db.from("borrowers").update(
            buildJsonObject {
                put("profile_pic_url", publicUrl)
            }
        ) {
            filter { eq("id", borrowerId) }
        }

        publicUrl
    }

    suspend fun applyLoan(loan: Loan): Result<Unit> = runCatching {
        val tenure = if (loan.tenureDays > 0) loan.tenureDays else 30
        val prepared = loan.copy(
            status = LoanStatus.PENDING,
            totalPayable = loan.principalAmount,
            dailyInstallment = round((loan.principalAmount / tenure) * 100.0) / 100.0
        )
        db.from("loans").insert(prepared)
    }

    suspend fun submitEmiPayment(repayment: Repayment): Result<Unit> = runCatching {
        db.from("repayments").insert(repayment.copy(status = "PENDING"))
    }

    /**
     * Submits a UPI repayment with transaction UTR reference awaiting lender verification.
     */
    suspend fun submitUpiPayment(
        loanId: String,
        borrowerId: String,
        lenderId: String,
        amount: Double,
        transactionRef: String,
        notes: String? = null
    ): Result<Unit> = runCatching {
        val repayment = Repayment(
            loanId = loanId,
            borrowerId = borrowerId,
            lenderId = lenderId,
            amountPaid = amount,
            paymentDate = DateUtils.getTodaySqlFormat(),
            paymentMode = PaymentMode.UPI,
            transactionRef = transactionRef.trim(),
            status = "PENDING",
            notes = notes ?: "UPI Repayment submitted by borrower"
        )
        db.from("repayments").insert(repayment)
    }

    /**
     * Fetches all repayments made by the borrower that are awaiting lender approval.
     */
    suspend fun getPendingRepayments(borrowerId: String): Result<List<Repayment>> = runCatching {
        db.from("repayments")
            .select {
                filter {
                    eq("borrower_id", borrowerId)
                    eq("status", "PENDING")
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<Repayment>()
    }

    suspend fun getRepaymentHistory(borrowerId: String): Result<List<Repayment>> = runCatching {
        db.from("repayments")
            .select {
                filter {
                    eq("borrower_id", borrowerId)
                    eq("status", "VERIFIED")
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<Repayment>()
    }

    suspend fun getCustomerApprovedLoans(borrowerId: String): Result<List<ApprovedLoanDetail>> = runCatching {
        val loans = db.from("loans").select {
            filter {
                eq("borrower_id", borrowerId)
                isIn("status", listOf(LoanStatus.ACTIVE.name, LoanStatus.APPROVED.name))
            }
            order("created_at", Order.DESCENDING)
        }.decodeList<Loan>()

        val borrower = getBorrowerProfile(borrowerId).getOrNull()
        val verifiedRepayments = getRepaymentHistory(borrowerId).getOrDefault(emptyList())
        val todayStr = DateUtils.getTodaySqlFormat()

        loans.map { loan ->
            val loanRepayments = verifiedRepayments.filter { it.loanId == loan.id }
            val totalPaid = loanRepayments.sumOf { it.amountPaid }
            val remainingBalance = maxOf(0.0, loan.totalPayable - totalPaid)

            val todayPaid = loanRepayments
                .filter { it.paymentDate == todayStr }
                .sumOf { it.amountPaid }

            val todayDue = if (remainingBalance <= 0.0) {
                0.0
            } else if (todayPaid >= loan.dailyInstallment) {
                0.0
            } else {
                maxOf(0.0, loan.dailyInstallment - todayPaid)
            }

            val sDate = loan.startDate ?: "Not Set"
            val eDate = loan.endDate ?: calculateEndDate(loan.startDate, loan.tenureDays)

            ApprovedLoanDetail(
                loanId = loan.id ?: "",
                borrowerId = borrowerId,
                borrowerName = borrower?.name ?: "Customer",
                borrowerMobile = borrower?.mobileNumber ?: "",
                principalAmount = loan.principalAmount,
                totalPayable = loan.totalPayable,
                dailyInstallment = loan.dailyInstallment,
                tenureDays = loan.tenureDays,
                startDate = sDate,
                endDate = eDate,
                totalPaid = totalPaid,
                remainingBalance = remainingBalance,
                todayDue = todayDue,
                todayPaid = todayPaid,
                status = loan.status.name
            )
        }
    }

    suspend fun getRepaymentsForLoan(loanId: String): Result<List<Repayment>> = runCatching {
        db.from("repayments")
            .select {
                filter {
                    eq("loan_id", loanId)
                    eq("status", "VERIFIED")
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<Repayment>()
    }

    suspend fun getDashboardSummary(borrowerId: String): Result<BorrowerDashboardSummary> = runCatching {
        val approvedLoans = getCustomerApprovedLoans(borrowerId).getOrThrow()
        val activeLoans = approvedLoans.filter { it.remainingBalance > 0.0 }

        val totalBorrowed = approvedLoans.sumOf { it.totalPayable }
        val totalPaid = approvedLoans.sumOf { it.totalPaid }
        val totalRemaining = approvedLoans.sumOf { it.remainingBalance }
        val todayDue = activeLoans.sumOf { it.todayDue }
        val todayPaid = approvedLoans.sumOf { it.todayPaid }

        BorrowerDashboardSummary(
            borrowerId = borrowerId,
            totalBorrowed = totalBorrowed,
            totalPaid = totalPaid,
            totalRemaining = totalRemaining,
            todayDue = todayDue,
            todayPaid = todayPaid,
            activeLoansCount = activeLoans.size
        )
    }

    private fun calculateEndDate(startDateStr: String?, tenureDays: Int): String {
        if (startDateStr.isNullOrBlank()) return "Pending"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(startDateStr) ?: return "Pending"
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, tenureDays)
            }
            sdf.format(calendar.time)
        } catch (e: Exception) {
            "Pending"
        }
    }
}
