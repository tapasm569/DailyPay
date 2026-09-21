package com.dailypay.app.data.repository

import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.LenderLedgerSummary
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.LoanStatus
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
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

class LenderRepository {

    private val db = SupabaseClientProvider.db
    private val storage = SupabaseClientProvider.storage

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

        if (aadhaarBytes != null && aadhaarBytes.isNotEmpty()) {
            val path = "aadhaar_${mobile}_$timestamp.jpg"
            storage.from("kyc-documents").upload(path = path, data = aadhaarBytes, upsert = true)
            aadhaarUrl = storage.from("kyc-documents").publicUrl(path)
        }

        if (panBytes != null && panBytes.isNotEmpty()) {
            val path = "pan_${mobile}_$timestamp.jpg"
            storage.from("kyc-documents").upload(path = path, data = panBytes, upsert = true)
            panUrl = storage.from("kyc-documents").publicUrl(path)
        }

        if (avatarBytes != null && avatarBytes.isNotEmpty()) {
            val path = "avatar_${mobile}_$timestamp.jpg"
            storage.from("profile-avatars").upload(path = path, data = avatarBytes, upsert = true)
            avatarUrl = storage.from("profile-avatars").publicUrl(path)
        }

        val updatedBorrower = borrower.copy(
            aadhaarCardUrl = aadhaarUrl,
            panCardUrl = panUrl,
            profilePicUrl = avatarUrl
        )

        db.from("borrowers").insert(updatedBorrower)
    }

    suspend fun getBorrowers(lenderId: String): Result<List<Borrower>> = runCatching {
        db.from("borrowers")
            .select {
                filter { eq("lender_id", lenderId) }
            }.decodeList<Borrower>()
    }

    suspend fun updateBorrower(borrower: Borrower): Result<Unit> = runCatching {
        val id = borrower.id ?: throw IllegalArgumentException("Borrower ID cannot be null")
        db.from("borrowers").update(
            buildJsonObject {
                put("name", borrower.name)
                put("mobile_number", borrower.mobileNumber)
                borrower.villageCity?.let { put("village_city", it) }
                borrower.postOffice?.let { put("post_office", it) }
                borrower.policeStation?.let { put("police_station", it) }
                borrower.dist?.let { put("dist", it) }
                put("password_hash", borrower.passwordHash)
            }
        ) {
            filter { eq("id", id) }
        }
    }

    suspend fun deleteBorrower(borrowerId: String): Result<Unit> = runCatching {
        db.from("borrowers").delete {
            filter { eq("id", borrowerId) }
        }
    }

    suspend fun getDailyDues(lenderId: String): Result<List<DailyDueItem>> = runCatching {
        db.from("v_lender_daily_dues")
            .select {
                filter { eq("lender_id", lenderId) }
            }.decodeList<DailyDueItem>()
    }

    suspend fun recordRepayment(repayment: Repayment): Result<Unit> = runCatching {
        db.from("repayments").insert(repayment)
    }

    suspend fun getRepaymentsForLoan(loanId: String): Result<List<Repayment>> = runCatching {
        db.from("repayments")
            .select {
                filter { eq("loan_id", loanId) }
                order("created_at", Order.DESCENDING)
            }.decodeList<Repayment>()
    }

    suspend fun giveLoanManually(loan: Loan): Result<Unit> = runCatching {
        val tenure = if (loan.tenureDays > 0) loan.tenureDays else 30
        val totalInterest = loan.principalAmount * (loan.monthlyInterestRate / 100.0) * (tenure / 30.0)
        val calculatedTotalPayable = if (loan.totalPayable > 0.0) loan.totalPayable else loan.principalAmount + totalInterest
        val calculatedInstallment = if (loan.dailyInstallment > 0.0) loan.dailyInstallment else round((calculatedTotalPayable / tenure) * 100.0) / 100.0
        val startDate = loan.startDate ?: DateUtils.getTodaySqlFormat()
        val endDate = loan.endDate ?: calculateEndDate(startDate, tenure)

        val preparedLoan = loan.copy(
            totalPayable = calculatedTotalPayable,
            dailyInstallment = calculatedInstallment,
            startDate = startDate,
            endDate = endDate,
            status = LoanStatus.ACTIVE
        )

        db.from("loans").insert(preparedLoan)
    }

    suspend fun getPendingLoanRequests(lenderId: String): Result<List<Loan>> = runCatching {
        db.from("loans")
            .select {
                filter {
                    eq("lender_id", lenderId)
                    eq("status", LoanStatus.PENDING.name)
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<Loan>()
    }

    suspend fun approveAndDisburseLoan(
        loanId: String,
        interestRate: Double,
        disbursementMode: PaymentMode,
        disbursementRef: String
    ): Result<Unit> = runCatching {
        val existingLoan = db.from("loans").select {
            filter { eq("id", loanId) }
        }.decodeSingle<Loan>()

        val tenure = if (existingLoan.tenureDays > 0) existingLoan.tenureDays else 30
        val totalInterest = existingLoan.principalAmount * (interestRate / 100.0) * (tenure / 30.0)
        val totalPayable = round((existingLoan.principalAmount + totalInterest) * 100.0) / 100.0
        val dailyInstallment = round((totalPayable / tenure) * 100.0) / 100.0

        val startDate = DateUtils.getTodaySqlFormat()
        val endDate = calculateEndDate(startDate, tenure)

        db.from("loans").update(
            buildJsonObject {
                put("status", LoanStatus.ACTIVE.name)
                put("monthly_interest_rate", interestRate)
                put("total_payable", totalPayable)
                put("daily_installment", dailyInstallment)
                put("start_date", startDate)
                put("end_date", endDate)
                put("disbursement_mode", disbursementMode.name)
                put("disbursement_ref", disbursementRef)
            }
        ) {
            filter { eq("id", loanId) }
        }
    }

    suspend fun getApprovedLoans(lenderId: String): Result<List<ApprovedLoanDetail>> = runCatching {
        val loans = db.from("loans").select {
            filter {
                eq("lender_id", lenderId)
                isIn("status", listOf(LoanStatus.ACTIVE.name, LoanStatus.APPROVED.name))
            }
            order("created_at", Order.DESCENDING)
        }.decodeList<Loan>()

        val borrowers = getBorrowers(lenderId).getOrDefault(emptyList()).associateBy { it.id }
        val duesMap = getDailyDues(lenderId).getOrDefault(emptyList()).associateBy { it.loanId }

        loans.map { loan ->
            val borrower = borrowers[loan.borrowerId]
            val dueItem = duesMap[loan.id]
            val sDate = loan.startDate ?: "Not Set"
            val eDate = loan.endDate ?: calculateEndDate(loan.startDate, loan.tenureDays)
            val todayPaid = dueItem?.todayPaidAmount ?: 0.0
            val todayDue = dueItem?.todayDueBalance ?: maxOf(0.0, loan.dailyInstallment - todayPaid)

            ApprovedLoanDetail(
                loanId = loan.id ?: "",
                borrowerId = loan.borrowerId,
                borrowerName = borrower?.name ?: "Customer",
                borrowerMobile = borrower?.mobileNumber ?: "N/A",
                principalAmount = loan.principalAmount,
                totalPayable = loan.totalPayable,
                dailyInstallment = loan.dailyInstallment,
                tenureDays = loan.tenureDays,
                startDate = sDate,
                endDate = eDate,
                totalPaid = dueItem?.totalPaid ?: 0.0,
                remainingBalance = dueItem?.remainingBalance ?: loan.totalPayable,
                todayDue = maxOf(0.0, todayDue),
                todayPaid = todayPaid,
                status = loan.status.name
            )
        }
    }

    suspend fun getLedgerSummary(lenderId: String): Result<LenderLedgerSummary> = runCatching {
        val dues = getDailyDues(lenderId).getOrThrow()
        val totalDisbursed = dues.sumOf { it.totalPayable }
        val totalDue = dues.sumOf { maxOf(0.0, it.todayDueBalance) }
        val totalPaid = dues.sumOf { it.totalPaid }
        val totalRemaining = dues.sumOf { maxOf(0.0, it.remainingBalance) }

        LenderLedgerSummary(
            lenderId = lenderId,
            totalDisbursed = totalDisbursed,
            totalDueBalance = totalDue,
            totalPaid = totalPaid,
            totalRemainingBalance = totalRemaining
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
