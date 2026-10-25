package com.dailypay.app.data.repository

import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.BorrowerDashboardSummary
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.LoanStatus
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.model.RepaymentStatus
import com.dailypay.app.data.remote.FirebaseDbProvider
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID

class BorrowerRepository {

    suspend fun getBorrowerProfile(borrowerId: String): Result<Borrower> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val snapshot = FirebaseDbProvider.borrowersRef.child(borrowerId.trim()).get().await()
                snapshot.getValue(Borrower::class.java) ?: Borrower(id = borrowerId.trim())
            }
        }
    }

    suspend fun getLenderProfile(lenderId: String): Result<Lender> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val snapshot = FirebaseDbProvider.lendersRef.child(lenderId.trim()).get().await()
                snapshot.getValue(Lender::class.java) ?: Lender(id = lenderId.trim())
            }
        }
    }

    suspend fun updateProfilePicture(borrowerId: String, mobile: String, bytes: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val base64 = ImageCompressor.compressAndEncodeBase64(bytes) ?: ""
                FirebaseDbProvider.borrowersRef.child(borrowerId.trim()).child("profilePicUrl").setValue(base64).await()
                base64
            }
        }
    }

    suspend fun getCustomerApprovedLoans(borrowerId: String): Result<List<ApprovedLoanDetail>> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val cleanBorrowerId = borrowerId.trim()
                val todaySql = DateUtils.getTodaySqlFormat()

                val loansSnapshot = FirebaseDbProvider.loansRef
                    .orderByChild("borrowerId")
                    .equalTo(cleanBorrowerId)
                    .get()
                    .await()

                val loans = loansSnapshot.children.mapNotNull { it.getValue(Loan::class.java) }
                    .filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.APPROVED }

                val repaymentsSnapshot = FirebaseDbProvider.repaymentsRef
                    .orderByChild("borrowerId")
                    .equalTo(cleanBorrowerId)
                    .get()
                    .await()

                val repayments = repaymentsSnapshot.children.mapNotNull { it.getValue(Repayment::class.java) }
                    .filter { it.status == RepaymentStatus.VERIFIED || it.status == null }

                loans.map { loan ->
                    val loanRepayments = repayments.filter { it.loanId == loan.id }
                    val totalPaid = loanRepayments.sumOf { it.amountPaid }
                    val todayPaid = loanRepayments.filter { it.paymentDate == todaySql }.sumOf { it.amountPaid }
                    val remaining = maxOf(0.0, loan.totalPayable - totalPaid)

                    val isStarted = loan.startDate.isNullOrBlank() || loan.startDate <= todaySql
                    val todayDue = if (isStarted) {
                        maxOf(0.0, minOf(loan.dailyInstallment - todayPaid, remaining))
                    } else {
                        0.0
                    }

                    ApprovedLoanDetail(
                        loanId = loan.id.orEmpty(),
                        borrowerId = cleanBorrowerId,
                        borrowerName = "Customer",
                        borrowerMobile = "",
                        principalAmount = loan.principalAmount,
                        totalPayable = loan.totalPayable,
                        dailyInstallment = loan.dailyInstallment,
                        tenureDays = loan.tenureDays,
                        startDate = loan.startDate ?: "Not Set",
                        endDate = loan.endDate ?: "Not Set",
                        status = loan.status.name,
                        totalPaid = totalPaid,
                        remainingBalance = remaining,
                        todayPaid = todayPaid,
                        todayDue = todayDue
                    )
                }
            }
        }
    }

    suspend fun getDashboardSummary(borrowerId: String): Result<BorrowerDashboardSummary> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val cleanBorrowerId = borrowerId.trim()
                val todaySql = DateUtils.getTodaySqlFormat()

                val loansSnapshot = FirebaseDbProvider.loansRef
                    .orderByChild("borrowerId")
                    .equalTo(cleanBorrowerId)
                    .get()
                    .await()

                val loans = loansSnapshot.children.mapNotNull { it.getValue(Loan::class.java) }
                    .filter { it.status == LoanStatus.ACTIVE }

                val repaymentsSnapshot = FirebaseDbProvider.repaymentsRef
                    .orderByChild("borrowerId")
                    .equalTo(cleanBorrowerId)
                    .get()
                    .await()

                val repayments = repaymentsSnapshot.children.mapNotNull { it.getValue(Repayment::class.java) }
                    .filter { it.status == RepaymentStatus.VERIFIED || it.status == null }

                val totalBorrowed = loans.sumOf { it.totalPayable }
                val totalPaid = repayments.sumOf { it.amountPaid }
                val totalRemaining = maxOf(0.0, totalBorrowed - totalPaid)
                val todayPaid = repayments.filter { it.paymentDate == todaySql }.sumOf { it.amountPaid }

                val activeStartedLoans = loans.filter { it.startDate.isNullOrBlank() || it.startDate <= todaySql }
                val todayDue = maxOf(0.0, activeStartedLoans.sumOf { it.dailyInstallment } - todayPaid)

                BorrowerDashboardSummary(
                    borrowerId = cleanBorrowerId,
                    activeLoansCount = loans.size,
                    totalBorrowed = totalBorrowed,
                    totalPaid = totalPaid,
                    totalRemaining = totalRemaining,
                    todayDue = todayDue,
                    todayPaid = todayPaid
                )
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

    suspend fun submitEmiPayment(repayment: Repayment): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val key = repayment.id?.takeIf { it.isNotBlank() }
                    ?: FirebaseDbProvider.repaymentsRef.push().key
                    ?: UUID.randomUUID().toString()

                val finalPayment = repayment.copy(
                    id = key,
                    paymentMode = PaymentMode.CASH,
                    status = RepaymentStatus.PENDING
                )

                FirebaseDbProvider.repaymentsRef.child(key).setValue(finalPayment).await()
                Unit
            }
        }
    }

    suspend fun applyLoan(loan: Loan): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val key = loan.id?.takeIf { it.isNotBlank() }
                    ?: FirebaseDbProvider.loansRef.push().key
                    ?: UUID.randomUUID().toString()

                val newLoan = loan.copy(
                    id = key,
                    status = LoanStatus.PENDING
                )
                FirebaseDbProvider.loansRef.child(key).setValue(newLoan).await()
                Unit
            }
        }
    }

    suspend fun getRepaymentHistory(borrowerId: String): Result<List<Repayment>> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeout(15000L) {
                val snapshot = FirebaseDbProvider.repaymentsRef
                    .orderByChild("borrowerId")
                    .equalTo(borrowerId.trim())
                    .get()
                    .await()

                snapshot.children.mapNotNull { it.getValue(Repayment::class.java) }
                    .sortedByDescending { it.paymentDate }
            }
        }
    }
}
