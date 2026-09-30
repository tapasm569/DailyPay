package com.dailypay.app.data.repository

import android.content.Context
import com.dailypay.app.data.local.DailyPayDatabase
import com.dailypay.app.data.local.entity.BorrowerEntity
import com.dailypay.app.data.local.entity.DailyDueEntity
import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.LenderLedgerSummary
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.LoanStatus
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.PendingPaymentItem
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.model.RepaymentStatus
import com.dailypay.app.data.remote.SupabaseClientProvider
import com.dailypay.app.util.DateUtils
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.round

class LenderRepository(context: Context? = null) {

    private val db = SupabaseClientProvider.db
    private val storage = SupabaseClientProvider.storage
    private val localDb = context?.let {
        val appContext = it.applicationContext ?: it
        DailyPayDatabase.getDatabase(appContext)
    }

    // ================= 1. LENDER PROFILE & ACCOUNT =================
    suspend fun getLenderProfile(lenderId: String): Result<Lender> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("lenders")
                .select {
                    filter { eq("id", lenderId.trim()) }
                }.decodeSingle<Lender>()
        }
    }

    suspend fun updateLenderAddress(
        lenderId: String,
        villageTown: String?,
        postOffice: String?,
        dist: String?,
        address: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("lenders").update(
                buildJsonObject {
                    villageTown?.let { put("village_town", it.trim()) }
                    postOffice?.let { put("post_office", it.trim()) }
                    dist?.let { put("dist", it.trim()) }
                    address?.let { put("address", it.trim()) }
                }
            ) {
                filter { eq("id", lenderId.trim()) }
            }
            Unit
        }
    }

    suspend fun updateLenderPassword(
        lenderId: String,
        newPasswordPlain: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("lenders").update(
                buildJsonObject {
                    put("password_hash", newPasswordPlain.trim())
                }
            ) {
                filter { eq("id", lenderId.trim()) }
            }
            Unit
        }
    }

    // ================= 2. BORROWER MANAGEMENT & KYC (CACHE-FIRST) =================
    fun observeBorrowers(lenderId: String): Flow<List<Borrower>>? {
        return localDb?.borrowerDao()?.getBorrowersFlow(lenderId.trim())?.map { list ->
            list.map { it.toBorrower() }
        }
    }

    suspend fun getBorrowers(lenderId: String): Result<List<Borrower>> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanLenderId = lenderId.trim()
            val cached = localDb?.borrowerDao()?.getBorrowersDirect(cleanLenderId)?.map { it.toBorrower() }

            try {
                val remoteBorrowers = db.from("borrowers")
                    .select { filter { eq("lender_id", cleanLenderId) } }
                    .decodeList<Borrower>()

                try {
                    localDb?.borrowerDao()?.insertAll(remoteBorrowers.map { BorrowerEntity.fromModel(it) })
                } catch (cacheError: Exception) {
                    // Suppress local SQLite cache write error to allow remote data return
                }
                remoteBorrowers
            } catch (e: Exception) {
                if (!cached.isNullOrEmpty()) {
                    cached
                } else {
                    throw e
                }
            }
        }
    }

    suspend fun addBorrower(
        borrower: Borrower,
        aadhaarBytes: ByteArray?,
        panBytes: ByteArray?,
        avatarBytes: ByteArray?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val timestamp = System.currentTimeMillis()
            val mobile = borrower.mobileNumber.trim()

            var aadhaarUrl: String? = borrower.aadhaarCardUrl
            var panUrl: String? = borrower.panCardUrl
            var avatarUrl: String? = borrower.profilePicUrl

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

            val validId = borrower.id?.trim()?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

            val updatedBorrower = borrower.copy(
                id = validId,
                lenderId = borrower.lenderId.trim(),
                mobileNumber = mobile,
                aadhaarCardUrl = aadhaarUrl,
                panCardUrl = panUrl,
                profilePicUrl = avatarUrl
            )

            db.from("borrowers").insert(updatedBorrower)
            try {
                getBorrowers(borrower.lenderId)
            } catch (ignored: Exception) {
            }
            Unit
        }
    }

    suspend fun updateBorrower(borrower: Borrower): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val id = borrower.id?.trim()?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Borrower ID cannot be null or blank")

            db.from("borrowers").update(
                buildJsonObject {
                    put("name", borrower.name.trim())
                    put("mobile_number", borrower.mobileNumber.trim())
                    borrower.villageCity?.let { put("village_city", it.trim()) }
                    borrower.postOffice?.let { put("post_office", it.trim()) }
                    borrower.policeStation?.let { put("police_station", it.trim()) }
                    borrower.dist?.let { put("dist", it.trim()) }
                    borrower.profilePicUrl?.let { put("profile_pic_url", it) }
                    borrower.aadhaarCardUrl?.let { put("aadhaar_card_url", it) }
                    borrower.panCardUrl?.let { put("pan_card_url", it) }
                    if (borrower.passwordHash.isNotBlank()) {
                        put("password_hash", borrower.passwordHash)
                    }
                }
            ) {
                filter { eq("id", id) }
            }
            try {
                getBorrowers(borrower.lenderId)
            } catch (ignored: Exception) {
            }
            Unit
        }
    }

    suspend fun updateBorrowerKycDoc(
        borrowerId: String,
        mobileNumber: String,
        docType: String,
        imageBytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val timestamp = System.currentTimeMillis()
            val cleanMobile = mobileNumber.trim()
            val cleanId = borrowerId.trim()
            val bucket = if (docType == "avatar") "profile-avatars" else "kyc-documents"
            val path = "${docType}_${cleanMobile}_$timestamp.jpg"

            storage.from(bucket).upload(path = path, data = imageBytes, upsert = true)
            val publicUrl = storage.from(bucket).publicUrl(path)

            val columnKey = when (docType) {
                "aadhaar" -> "aadhaar_card_url"
                "pan" -> "pan_card_url"
                else -> "profile_pic_url"
            }

            db.from("borrowers").update(
                buildJsonObject {
                    put(columnKey, publicUrl)
                }
            ) {
                filter { eq("id", cleanId) }
            }

            publicUrl
        }
    }

    suspend fun deleteBorrower(borrowerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanId = borrowerId.trim()
            db.from("borrowers").delete { filter { eq("id", cleanId) } }
            localDb?.borrowerDao()?.deleteById(cleanId)
            Unit
        }
    }

    // ================= 3. LOAN MANAGEMENT & ACTIONS =================
    suspend fun giveLoanManually(loan: Loan): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val tenure = if (loan.tenureDays > 0) loan.tenureDays else 30
            val totalInterest = loan.principalAmount * (loan.monthlyInterestRate / 100.0) * (tenure / 30.0)
            val calculatedTotalPayable = if (loan.totalPayable > 0.0) {
                loan.totalPayable
            } else {
                round((loan.principalAmount + totalInterest) * 100.0) / 100.0
            }
            val calculatedInstallment = if (loan.dailyInstallment > 0.0) {
                loan.dailyInstallment
            } else {
                round((calculatedTotalPayable / tenure) * 100.0) / 100.0
            }
            val startDate = loan.startDate?.trim()?.takeIf { it.isNotBlank() } ?: DateUtils.getTodaySqlFormat()
            val endDate = loan.endDate?.trim()?.takeIf { it.isNotBlank() } ?: calculateEndDate(startDate, tenure)

            val validLoanId = loan.id?.trim()?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

            val preparedLoan = loan.copy(
                id = validLoanId,
                lenderId = loan.lenderId.trim(),
                borrowerId = loan.borrowerId.trim(),
                totalPayable = calculatedTotalPayable,
                dailyInstallment = calculatedInstallment,
                startDate = startDate,
                endDate = endDate,
                status = LoanStatus.ACTIVE
            )

            db.from("loans").insert(preparedLoan)
            try {
                getDailyDues(loan.lenderId)
            } catch (ignored: Exception) {
            }
            Unit
        }
    }

    suspend fun getPendingLoanRequests(lenderId: String): Result<List<Loan>> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("loans").select {
                filter {
                    eq("lender_id", lenderId.trim())
                    eq("status", LoanStatus.PENDING.name)
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<Loan>()
        }
    }

    suspend fun approveAndDisburseLoan(
        loanId: String,
        interestRate: Double,
        disbursementMode: PaymentMode,
        disbursementRef: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanLoanId = loanId.trim()
            val existingLoan = db.from("loans").select {
                filter { eq("id", cleanLoanId) }
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
                    put("disbursement_ref", disbursementRef.trim())
                }
            ) {
                filter { eq("id", cleanLoanId) }
            }

            try {
                getDailyDues(existingLoan.lenderId)
            } catch (ignored: Exception) {
            }
            Unit
        }
    }

    suspend fun rejectLoan(loanId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("loans").update(
                buildJsonObject {
                    put("status", LoanStatus.REJECTED.name)
                }
            ) {
                filter { eq("id", loanId.trim()) }
            }
            Unit
        }
    }

    suspend fun getApprovedLoans(lenderId: String): Result<List<ApprovedLoanDetail>> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanLenderId = lenderId.trim()
            val loans = db.from("loans").select {
                filter {
                    eq("lender_id", cleanLenderId)
                    isIn("status", listOf(LoanStatus.ACTIVE.name, LoanStatus.APPROVED.name))
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<Loan>()

            val borrowers = getBorrowers(cleanLenderId).getOrDefault(emptyList()).associateBy { it.id }
            val duesMap = getDailyDues(cleanLenderId).getOrDefault(emptyList()).associateBy { it.loanId }

            loans.map { loan ->
                val borrower = borrowers[loan.borrowerId]
                val dueItem = duesMap[loan.id]
                val sDate = loan.startDate?.trim()?.takeIf { it.isNotBlank() } ?: "Not Set"
                val eDate = loan.endDate?.trim()?.takeIf { it.isNotBlank() } ?: calculateEndDate(loan.startDate, loan.tenureDays)
                val todayPaid = dueItem?.todayPaidAmount ?: 0.0
                val todayDue = dueItem?.todayDueBalance ?: maxOf(0.0, loan.dailyInstallment - todayPaid)

                ApprovedLoanDetail(
                    loanId = loan.id.orEmpty(),
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
    }

    // ================= 4. PAYMENTS & COLLECTIONS (CACHE-FIRST) =================
    fun observeDailyDues(lenderId: String): Flow<List<DailyDueItem>>? {
        return localDb?.dailyDueDao()?.getDailyDuesFlow(lenderId.trim())?.map { list ->
            list.map { it.toDailyDueItem() }
        }
    }

    suspend fun getDailyDues(lenderId: String): Result<List<DailyDueItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanLenderId = lenderId.trim()
            val cached = localDb?.dailyDueDao()?.getDailyDuesDirect(cleanLenderId)?.map { it.toDailyDueItem() }

            try {
                val remoteDues = db.from("v_lender_daily_dues")
                    .select { filter { eq("lender_id", cleanLenderId) } }
                    .decodeList<DailyDueItem>()

                try {
                    localDb?.dailyDueDao()?.insertAll(remoteDues.map { DailyDueEntity.fromModel(cleanLenderId, it) })
                } catch (cacheError: Exception) {
                    // Suppress local SQLite cache write error to allow remote data return
                }
                remoteDues
            } catch (e: Exception) {
                if (!cached.isNullOrEmpty()) {
                    cached
                } else {
                    throw e
                }
            }
        }
    }

    suspend fun recordRepayment(repayment: Repayment): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPaymentDate = repayment.paymentDate.trim().takeIf { it.isNotBlank() } ?: DateUtils.getTodaySqlFormat()
            val validId = repayment.id?.trim()?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            val cleanLenderId = repayment.lenderId.trim()

            val sanitizedRepayment = repayment.copy(
                id = validId,
                loanId = repayment.loanId.trim(),
                borrowerId = repayment.borrowerId.trim(),
                lenderId = cleanLenderId,
                paymentDate = cleanPaymentDate,
                status = RepaymentStatus.VERIFIED
            )

            // Insert verified repayment into Supabase
            db.from("repayments").insert(sanitizedRepayment)

            // Refresh dues immediately so Room & UI reflect the updated paid amount
            if (cleanLenderId.isNotBlank() && cleanLenderId != "{lenderId}") {
                try {
                    val remoteDues = db.from("v_lender_daily_dues")
                        .select { filter { eq("lender_id", cleanLenderId) } }
                        .decodeList<DailyDueItem>()
                    localDb?.dailyDueDao()?.insertAll(remoteDues.map { DailyDueEntity.fromModel(cleanLenderId, it) })
                } catch (cacheSyncError: Exception) {
                    // Cache sync failure does not fail the primary payment recording
                }
            }
            Unit
        }
    }

    suspend fun getPendingVerifications(lenderId: String): Result<List<PendingPaymentItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanLenderId = lenderId.trim()
            val pendingRepayments = db.from("repayments").select {
                filter {
                    eq("lender_id", cleanLenderId)
                    eq("status", RepaymentStatus.PENDING.name)
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<Repayment>()

            val borrowers = getBorrowers(cleanLenderId).getOrDefault(emptyList()).associateBy { it.id }

            pendingRepayments.map { r ->
                val b = borrowers[r.borrowerId]
                PendingPaymentItem(
                    repayment = r,
                    borrowerName = b?.name ?: "Customer",
                    borrowerMobile = b?.mobileNumber ?: "N/A"
                )
            }
        }
    }

    suspend fun verifyRepayment(repaymentId: String, accept: Boolean, lenderId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val newStatus = if (accept) RepaymentStatus.VERIFIED.name else RepaymentStatus.REJECTED.name
            db.from("repayments").update(
                buildJsonObject { put("status", newStatus) }
            ) {
                filter { eq("id", repaymentId.trim()) }
            }

            val cleanLender = lenderId?.trim().orEmpty()
            if (cleanLender.isNotBlank() && cleanLender != "{lenderId}") {
                try {
                    getDailyDues(cleanLender)
                } catch (ignored: Exception) {
                }
            }
            Unit
        }
    }

    suspend fun getTrackPayments(lenderId: String): Result<List<Repayment>> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("repayments").select {
                filter {
                    eq("lender_id", lenderId.trim())
                    eq("status", RepaymentStatus.VERIFIED.name)
                }
                order("payment_date", Order.DESCENDING)
                order("created_at", Order.DESCENDING)
            }.decodeList<Repayment>()
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

    // ================= 5. PORTFOLIO LEDGER SUMMARY =================
    suspend fun getLedgerSummary(lenderId: String): Result<LenderLedgerSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val dues = getDailyDues(lenderId.trim()).getOrThrow()
            LenderLedgerSummary(
                lenderId = lenderId.trim(),
                totalDisbursed = dues.sumOf { it.totalPayable },
                totalDueBalance = dues.sumOf { maxOf(0.0, it.todayDueBalance) },
                totalPaid = dues.sumOf { it.todayPaidAmount },
                totalRemainingBalance = dues.sumOf { maxOf(0.0, it.remainingBalance) }
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