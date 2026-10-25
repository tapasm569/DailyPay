package com.dailypay.app.data.repository

import android.content.Context
import com.dailypay.app.data.local.DailyPayDatabase
import com.dailypay.app.data.local.entity.BorrowerEntity
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
import com.dailypay.app.data.remote.FirebaseDbProvider
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.round

class LenderRepository(context: Context? = null) {

    private val localDb = context?.let {
        val appContext = it.applicationContext ?: it
        DailyPayDatabase.getDatabase(appContext)
    }

    suspend fun getLenderProfile(lenderId: String): Result<Lender> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val snapshot = FirebaseDbProvider.lendersRef.child(lenderId.trim()).get().await()
                snapshot.getValue(Lender::class.java) ?: Lender(id = lenderId.trim())
            }
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
            withTimeout(15000L) {
                val updates = mutableMapOf<String, Any?>()
                villageTown?.let { updates["villageTown"] = it.trim() }
                postOffice?.let { updates["postOffice"] = it.trim() }
                dist?.let { updates["dist"] = it.trim() }
                address?.let { updates["address"] = it.trim() }

                if (updates.isNotEmpty()) {
                    FirebaseDbProvider.lendersRef.child(lenderId.trim()).updateChildren(updates).await()
                }
                Unit
            }
        }
    }

    suspend fun updateLenderPassword(
        lenderId: String,
        newPasswordPlain: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                FirebaseDbProvider.lendersRef
                    .child(lenderId.trim())
                    .child("passwordHash")
                    .setValue(newPasswordPlain.trim())
                    .await()
                Unit
            }
        }
    }

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
                withTimeout(15000L) {
                    val snapshot = FirebaseDbProvider.borrowersRef
                        .orderByChild("lenderId")
                        .equalTo(cleanLenderId)
                        .get()
                        .await()

                    val remoteBorrowers = snapshot.children.mapNotNull { it.getValue(Borrower::class.java) }
                    try {
                        localDb?.borrowerDao()?.insertAll(remoteBorrowers.map { BorrowerEntity.fromModel(it) })
                    } catch (_: Exception) {}
                    remoteBorrowers
                }
            } catch (e: Exception) {
                cached ?: throw e
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
            withTimeout(20000L) {
                val mobile = borrower.mobileNumber.trim()

                val aadhaarBase64 = ImageCompressor.compressAndEncodeBase64(aadhaarBytes) ?: borrower.aadhaarCardUrl
                val panBase64 = ImageCompressor.compressAndEncodeBase64(panBytes) ?: borrower.panCardUrl
                val avatarBase64 = ImageCompressor.compressAndEncodeBase64(avatarBytes) ?: borrower.profilePicUrl

                val validId = borrower.id?.trim()?.takeIf { it.isNotBlank() }
                    ?: FirebaseDbProvider.borrowersRef.push().key
                    ?: UUID.randomUUID().toString()

                val cleanPassword = borrower.passwordHash?.trim()?.takeIf { it.isNotBlank() } ?: mobile

                val updatedBorrower = borrower.copy(
                    id = validId,
                    lenderId = borrower.lenderId.trim(),
                    mobileNumber = mobile,
                    passwordHash = cleanPassword,
                    aadhaarCardUrl = aadhaarBase64,
                    panCardUrl = panBase64,
                    profilePicUrl = avatarBase64
                )

                FirebaseDbProvider.borrowersRef.child(validId).setValue(updatedBorrower).await()
                try {
                    localDb?.borrowerDao()?.insertAll(listOf(BorrowerEntity.fromModel(updatedBorrower)))
                } catch (_: Exception) {}
                Unit
            }
        }
    }

    suspend fun updateBorrower(borrower: Borrower): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val id = borrower.id?.trim()?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("Borrower ID cannot be null or blank")

                val updates = mutableMapOf<String, Any?>()
                updates["name"] = borrower.name.trim()
                updates["mobileNumber"] = borrower.mobileNumber.trim()
                borrower.villageCity?.let { updates["villageCity"] = it.trim() }
                borrower.postOffice?.let { updates["postOffice"] = it.trim() }
                borrower.policeStation?.let { updates["policeStation"] = it.trim() }
                borrower.dist?.let { updates["dist"] = it.trim() }
                borrower.profilePicUrl?.let { updates["profilePicUrl"] = it }
                borrower.aadhaarCardUrl?.let { updates["aadhaarCardUrl"] = it }
                borrower.panCardUrl?.let { updates["panCardUrl"] = it }
                if (!borrower.passwordHash.isNullOrBlank()) {
                    updates["passwordHash"] = borrower.passwordHash
                }

                FirebaseDbProvider.borrowersRef.child(id).updateChildren(updates).await()
                Unit
            }
        }
    }

    suspend fun updateBorrowerKycDoc(
        borrowerId: String,
        mobileNumber: String,
        docType: String,
        imageBytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val cleanId = borrowerId.trim()
                val base64Data = ImageCompressor.compressAndEncodeBase64(imageBytes) ?: ""

                val fieldKey = when (docType) {
                    "aadhaar" -> "aadhaarCardUrl"
                    "pan" -> "panCardUrl"
                    else -> "profilePicUrl"
                }

                FirebaseDbProvider.borrowersRef.child(cleanId).child(fieldKey).setValue(base64Data).await()
                base64Data
            }
        }
    }

    suspend fun deleteBorrower(borrowerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val cleanId = borrowerId.trim()
                FirebaseDbProvider.borrowersRef.child(cleanId).removeValue().await()
                localDb?.borrowerDao()?.deleteById(cleanId)
                Unit
            }
        }
    }

    suspend fun giveLoanManually(loan: Loan): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
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

                val validLoanId = loan.id?.trim()?.takeIf { it.isNotBlank() }
                    ?: FirebaseDbProvider.loansRef.push().key
                    ?: UUID.randomUUID().toString()

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

                FirebaseDbProvider.loansRef.child(validLoanId).setValue(preparedLoan).await()
                Unit
            }
        }
    }

    suspend fun getPendingLoanRequests(lenderId: String): Result<List<Loan>> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val snapshot = FirebaseDbProvider.loansRef
                    .orderByChild("lenderId")
                    .equalTo(lenderId.trim())
                    .get()
                    .await()

                snapshot.children.mapNotNull { it.getValue(Loan::class.java) }
                    .filter { it.status == LoanStatus.PENDING }
            }
        }
    }

    suspend fun approveAndDisburseLoan(
        loanId: String,
        interestRate: Double,
        disbursementMode: PaymentMode,
        disbursementRef: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val cleanLoanId = loanId.trim()
                val snapshot = FirebaseDbProvider.loansRef.child(cleanLoanId).get().await()
                val existingLoan = snapshot.getValue(Loan::class.java)
                    ?: throw IllegalArgumentException("Loan not found")

                val tenure = if (existingLoan.tenureDays > 0) existingLoan.tenureDays else 30
                val totalInterest = existingLoan.principalAmount * (interestRate / 100.0) * (tenure / 30.0)
                val totalPayable = round((existingLoan.principalAmount + totalInterest) * 100.0) / 100.0
                val dailyInstallment = round((totalPayable / tenure) * 100.0) / 100.0

                val startDate = DateUtils.getTodaySqlFormat()
                val endDate = calculateEndDate(startDate, tenure)

                val updates = mapOf(
                    "status" to LoanStatus.ACTIVE.name,
                    "monthlyInterestRate" to interestRate,
                    "totalPayable" to totalPayable,
                    "dailyInstallment" to dailyInstallment,
                    "startDate" to startDate,
                    "endDate" to endDate,
                    "disbursementMode" to disbursementMode.name,
                    "disbursementRef" to disbursementRef.trim()
                )

                FirebaseDbProvider.loansRef.child(cleanLoanId).updateChildren(updates).await()
                Unit
            }
        }
    }

    suspend fun rejectLoan(loanId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                FirebaseDbProvider.loansRef
                    .child(loanId.trim())
                    .child("status")
                    .setValue(LoanStatus.REJECTED.name)
                    .await()
                Unit
            }
        }
    }

    suspend fun getApprovedLoans(lenderId: String): Result<List<ApprovedLoanDetail>> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val cleanLenderId = lenderId.trim()
                val snapshot = FirebaseDbProvider.loansRef
                    .orderByChild("lenderId")
                    .equalTo(cleanLenderId)
                    .get()
                    .await()

                val loans = snapshot.children.mapNotNull { it.getValue(Loan::class.java) }
                    .filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.APPROVED }

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
                        status = loan.status.name,
                        remainingBalance = maxOf(0.0, loan.totalPayable - (dueItem?.totalPaid ?: 0.0)),
                        todayPaid = todayPaid,
                        todayDue = todayDue
                    )
                }
            }
        }
    }

    suspend fun getDailyDues(lenderId: String): Result<List<DailyDueItem>> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val cleanLenderId = lenderId.trim()
                val todaySql = DateUtils.getTodaySqlFormat()

                val loansSnapshot = FirebaseDbProvider.loansRef
                    .orderByChild("lenderId")
                    .equalTo(cleanLenderId)
                    .get()
                    .await()

                val activeLoans = loansSnapshot.children.mapNotNull { it.getValue(Loan::class.java) }
                    .filter { it.status == LoanStatus.ACTIVE }

                val borrowers = getBorrowers(cleanLenderId).getOrDefault(emptyList()).associateBy { it.id }

                val repaymentsSnapshot = FirebaseDbProvider.repaymentsRef
                    .orderByChild("lenderId")
                    .equalTo(cleanLenderId)
                    .get()
                    .await()

                val repayments = repaymentsSnapshot.children.mapNotNull { it.getValue(Repayment::class.java) }
                    .filter { it.status == RepaymentStatus.VERIFIED || it.status == null }

                activeLoans.map { loan ->
                    val borrower = borrowers[loan.borrowerId]
                    val loanRepayments = repayments.filter { it.loanId == loan.id }

                    val totalPaid = loanRepayments.sumOf { it.amountPaid }
                    val paidToday = loanRepayments.filter { it.paymentDate == todaySql }.sumOf { it.amountPaid }
                    val remainingBalance = maxOf(0.0, loan.totalPayable - totalPaid)
                    val todayDueBalance = maxOf(0.0, minOf(loan.dailyInstallment - paidToday, remainingBalance))

                    DailyDueItem(
                        loanId = loan.id.orEmpty(),
                        borrowerId = loan.borrowerId,
                        borrowerName = borrower?.name ?: "Customer",
                        borrowerMobile = borrower?.mobileNumber ?: "",
                        dailyInstallment = loan.dailyInstallment,
                        todayDueBalance = todayDueBalance,
                        todayPaidAmount = paidToday,
                        remainingBalance = remainingBalance,
                        totalPaid = totalPaid
                    )
                }
            }
        }
    }

    suspend fun recordRepayment(repayment: Repayment): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val validId = repayment.id?.takeIf { it.isNotBlank() }
                    ?: FirebaseDbProvider.repaymentsRef.push().key
                    ?: UUID.randomUUID().toString()

                val finalRepayment = repayment.copy(
                    id = validId,
                    paymentMode = PaymentMode.CASH,
                    status = RepaymentStatus.VERIFIED
                )

                FirebaseDbProvider.repaymentsRef.child(validId).setValue(finalRepayment).await()
                Unit
            }
        }
    }

    suspend fun getPendingVerifications(lenderId: String): Result<List<PendingPaymentItem>> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val cleanLenderId = lenderId.trim()
                val snapshot = FirebaseDbProvider.repaymentsRef
                    .orderByChild("lenderId")
                    .equalTo(cleanLenderId)
                    .get()
                    .await()

                val pendingRepayments = snapshot.children.mapNotNull { it.getValue(Repayment::class.java) }
                    .filter { it.status == RepaymentStatus.PENDING }

                val borrowers = getBorrowers(cleanLenderId).getOrDefault(emptyList()).associateBy { it.id }

                pendingRepayments.map { repayment ->
                    val borrower = borrowers[repayment.borrowerId]
                    PendingPaymentItem(
                        repayment = repayment,
                        borrowerName = borrower?.name ?: "Customer",
                        borrowerMobile = borrower?.mobileNumber ?: "N/A"
      )
                }
            }
        }
    }

    suspend fun verifyRepayment(
        repaymentId: String,
        accept: Boolean,
        lenderId: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val newStatus = if (accept) RepaymentStatus.VERIFIED.name else RepaymentStatus.REJECTED.name
                FirebaseDbProvider.repaymentsRef
                    .child(repaymentId.trim())
                    .child("status")
                    .setValue(newStatus)
                    .await()
                Unit
            }
        }
    }

    suspend fun getRepaymentsForLoan(loanId: String): Result<List<Repayment>> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val snapshot = FirebaseDbProvider.repaymentsRef
                    .orderByChild("loanId")
                    .equalTo(loanId.trim())
                    .get()
                    .await()

                snapshot.children.mapNotNull { it.getValue(Repayment::class.java) }
                    .sortedByDescending { it.paymentDate }
            }
        }
    }

    suspend fun getTrackPayments(lenderId: String): Result<List<Repayment>> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val snapshot = FirebaseDbProvider.repaymentsRef
                    .orderByChild("lenderId")
                    .equalTo(lenderId.trim())
                    .get()
                    .await()

                snapshot.children.mapNotNull { it.getValue(Repayment::class.java) }
                    .sortedByDescending { it.paymentDate }
            }
        }
    }

    suspend fun getLedgerSummary(lenderId: String): Result<LenderLedgerSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val dues = getDailyDues(lenderId).getOrDefault(emptyList())
            val totalDisbursed = dues.sumOf { it.dailyInstallment * 30 }
            val totalRecovered = dues.sumOf { it.totalPaid }
            val totalRemaining = dues.sumOf { it.remainingBalance }
            val todayTotalDue = dues.sumOf { it.todayDueBalance }

            LenderLedgerSummary(
                lenderId = lenderId.trim(),
                totalDisbursed = totalDisbursed,
                totalDueBalance = todayTotalDue,
                totalPaid = totalRecovered,
                totalRemainingBalance = totalRemaining
            )
        }
    }

    suspend fun getLenderLedgerSummary(lenderId: String): Result<LenderLedgerSummary> = getLedgerSummary(lenderId)

    private fun calculateEndDate(startDateStr: String?, tenureDays: Int): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = if (!startDateStr.isNullOrBlank()) format.parse(startDateStr) else java.util.Date()
            val cal = Calendar.getInstance()
            cal.time = date ?: java.util.Date()
            cal.add(Calendar.DAY_OF_YEAR, tenureDays)
            format.format(cal.time)
        } catch (_: Exception) {
            DateUtils.getTodaySqlFormat()
        }
    }
}
