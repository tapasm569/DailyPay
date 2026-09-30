package com.dailypay.app.data.repository

import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.BorrowerDashboardSummary
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.LoanStatus
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.model.RepaymentStatus
import com.dailypay.app.data.remote.SupabaseClientProvider
import com.dailypay.app.util.DateUtils
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.round

class BorrowerRepository {

    private val db = SupabaseClientProvider.db
    private val storage = SupabaseClientProvider.storage

    suspend fun getBorrowerProfile(borrowerId: String): Result<Borrower> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("borrowers")
                .select {
                    filter { eq("id", borrowerId.trim()) }
                }.decodeSingle<Borrower>()
        }
    }

    suspend fun getLenderProfile(lenderId: String): Result<Lender> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("lenders")
                .select {
                    filter { eq("id", lenderId.trim()) }
                }.decodeSingle<Lender>()
        }
    }

    suspend fun updateProfilePicture(
        borrowerId: String,
        mobileNumber: String,
        avatarBytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val timestamp = System.currentTimeMillis()
            val cleanMobile = mobileNumber.trim()
            val cleanId = borrowerId.trim()
            val path = "avatar_${cleanMobile}_$timestamp.jpg"

            storage.from("profile-avatars")
                .upload(path = path, data = avatarBytes, upsert = true)

            val publicUrl = storage.from("profile-avatars").publicUrl(path)

            db.from("borrowers").update(
                buildJsonObject {
                    put("profile_pic_url", publicUrl)
                }
            ) {
                filter { eq("id", cleanId) }
            }

            publicUrl
        }
    }

    suspend fun applyLoan(loan: Loan): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val tenure = if (loan.tenureDays > 0) loan.tenureDays else 30
            val validId = loan.id?.trim()?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

            val prepared = loan.copy(
                id = validId,
                lenderId = loan.lenderId.trim(),
                borrowerId = loan.borrowerId.trim(),
                status = LoanStatus.PENDING,
                totalPayable = loan.principalAmount,
                dailyInstallment = round((loan.principalAmount / tenure) * 100.0) / 100.0
            )

            db.from("loans").insert(prepared)
            Unit
        }
    }

    /**
     * Submits an EMI payment with status = PENDING and records the 12-digit UTR reference
     * so the lender can cross-verify against their bank/UPI account before approving.
     */
    suspend fun submitEmiPayment(repayment: Repayment): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPaymentDate = repayment.paymentDate?.trim()?.takeIf { it.isNotBlank() } ?: DateUtils.getTodaySqlFormat()
            val validId = repayment.id?.trim()?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            val cleanUtr = repayment.utrReference?.trim()?.takeIf { it.isNotBlank() }

            val prepared = repayment.copy(
                id = validId,
                loanId = repayment.loanId.trim(),
                borrowerId = repayment.borrowerId.trim(),
                lenderId = repayment.lenderId.trim(),
                paymentDate = cleanPaymentDate,
                amountPaid = repayment.amountPaid,
                paymentMode = repayment.paymentMode,
                status = RepaymentStatus.PENDING,
                utrReference = cleanUtr,
                notes = repayment.notes?.trim()?.takeIf { it.isNotBlank() } ?: "Online EMI payment submitted via UPI"
            )

            db.from("repayments").insert(prepared)
            Unit
        }
    }

    suspend fun getRepaymentHistory(borrowerId: String): Result<List<Repayment>> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("repayments")
                .select {
                    filter {
                        eq("borrower_id", borrowerId.trim())
                        eq("status", RepaymentStatus.VERIFIED.name)
                    }
                    order("created_at", Order.DESCENDING)
                }.decodeList<Repayment>()
        }
    }

    suspend fun getCustomerApprovedLoans(borrowerId: String): Result<List<ApprovedLoanDetail>> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanBorrowerId = borrowerId.trim()
            val loans = db.from("loans").select {
                filter {
                    eq("borrower_id", cleanBorrowerId)
                    isIn("status", listOf(LoanStatus.ACTIVE.name, LoanStatus.APPROVED.name))
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<Loan>()

            val borrower = getBorrowerProfile(cleanBorrowerId).getOrNull()
            val verifiedRepayments = getRepaymentHistory(cleanBorrowerId).getOrDefault(emptyList())
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

                val sDate = loan.startDate?.trim()?.takeIf { it.isNotBlank() } ?: "Not Set"
                val eDate = loan.endDate?.trim()?.takeIf { it.isNotBlank() } ?: calculateEndDate(loan.startDate, loan.tenureDays)

                ApprovedLoanDetail(
                    loanId = loan.id.orEmpty(),
                    borrowerId = cleanBorrowerId,
                    borrowerName = borrower?.name.orEmpty().ifBlank { "Customer" },
                    borrowerMobile = borrower?.mobileNumber.orEmpty(),
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
    }

    suspend fun getRepaymentsForLoan(loanId: String): Result<List<Repayment>> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("repayments")
                .select {
                    filter {
                        eq("loan_id", loanId.trim())
                        eq("status", RepaymentStatus.VERIFIED.name)
                    }
                    order("created_at", Order.DESCENDING)
                }.decodeList<Repayment>()
        }
    }

    suspend fun getDashboardSummary(borrowerId: String): Result<BorrowerDashboardSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val approvedLoans = getCustomerApprovedLoans(borrowerId.trim()).getOrThrow()
            val activeLoans = approvedLoans.filter { it.remainingBalance > 0.0 }

            val totalBorrowed = approvedLoans.sumOf { it.totalPayable }
            val totalPaid = approvedLoans.sumOf { it.totalPaid }
            val totalRemaining = approvedLoans.sumOf { it.remainingBalance }
            val todayDue = activeLoans.sumOf { it.todayDue }
            val todayPaid = approvedLoans.sumOf { it.todayPaid }

            BorrowerDashboardSummary(
                borrowerId = borrowerId.trim(),
                totalBorrowed = totalBorrowed,
                totalPaid = totalPaid,
                totalRemaining = totalRemaining,
                todayDue = todayDue,
                todayPaid = todayPaid,
                activeLoansCount = activeLoans.size
            )
        }
    }

    private fun calculateEndDate(startDateStr: String?, tenureDays: Int): String {
        val cleanDate = startDateStr?.trim().orEmpty()
        if (cleanDate.isEmpty()) return "Pending"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(cleanDate) ?: return "Pending"
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
