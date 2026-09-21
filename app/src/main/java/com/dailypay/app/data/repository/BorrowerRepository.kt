package com.dailypay.app.data.repository

import com.dailypay.app.data.model.*
import com.dailypay.app.data.remote.SupabaseClientProvider
import com.dailypay.app.util.DateUtils
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.round

class BorrowerRepository {

    private val db = SupabaseClientProvider.db

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
        db.from("repayments").insert(repayment.copy(status = RepaymentStatus.PENDING))
    }

    suspend fun getRepaymentHistory(borrowerId: String): Result<List<Repayment>> = runCatching {
        db.from("repayments")
            .select {
                filter {
                    eq("borrower_id", borrowerId)
                    eq("status", RepaymentStatus.VERIFIED.name)
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
                    eq("status", RepaymentStatus.VERIFIED.name)
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