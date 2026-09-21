package com.dailypay.app.data.repository

import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.BorrowerDashboardSummary
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.LoanStatus
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.remote.SupabaseClientProvider
import com.dailypay.app.util.DateUtils
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BorrowerRepository {

    private val db = SupabaseClientProvider.db

    suspend fun getBorrowerProfile(borrowerId: String): Result<Borrower> = runCatching {
        db.from("borrowers")
            .select {
                filter { eq("id", borrowerId) }
            }.decodeSingle<Borrower>()
    }

    suspend fun applyLoan(loan: Loan): Result<Unit> = runCatching {
        db.from("loans").insert(loan)
    }

    suspend fun requestLoan(loan: Loan): Result<Unit> = applyLoan(loan)

    suspend fun getBorrowerDueDetails(borrowerId: String): Result<DailyDueItem?> = runCatching {
        db.from("v_lender_daily_dues")
            .select {
                filter { eq("borrower_id", borrowerId) }
            }.decodeList<DailyDueItem>().firstOrNull()
    }

    suspend fun getRepaymentHistory(borrowerId: String): Result<List<Repayment>> = runCatching {
        db.from("repayments")
            .select {
                filter { eq("borrower_id", borrowerId) }
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
        val allDues = db.from("v_lender_daily_dues").select {
            filter { eq("borrower_id", borrowerId) }
        }.decodeList<DailyDueItem>().associateBy { it.loanId }

        val todayStr = DateUtils.getTodaySqlFormat()
        val todayRepayments = db.from("repayments").select {
            filter {
                eq("borrower_id", borrowerId)
                eq("payment_date", todayStr)
            }
        }.decodeList<Repayment>().groupBy { it.loanId }

        loans.map { loan ->
            val dueItem = allDues[loan.id]
            val sDate = loan.startDate ?: "Not Set"
            val eDate = loan.endDate ?: calculateEndDate(loan.startDate, loan.tenureDays)
            val todayPaidForLoan = todayRepayments[loan.id]?.sumOf { it.amountPaid } ?: dueItem?.todayPaidAmount ?: 0.0
            val calculatedTodayDue = dueItem?.todayDueBalance ?: maxOf(0.0, loan.dailyInstallment - todayPaidForLoan)

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
                totalPaid = dueItem?.totalPaid ?: 0.0,
                remainingBalance = dueItem?.remainingBalance ?: loan.totalPayable,
                todayDue = maxOf(0.0, calculatedTodayDue),
                todayPaid = todayPaidForLoan,
                status = loan.status.name
            )
        }
    }

    suspend fun getRepaymentsForLoan(loanId: String): Result<List<Repayment>> = runCatching {
        db.from("repayments")
            .select {
                filter { eq("loan_id", loanId) }
                order("created_at", Order.DESCENDING)
            }.decodeList<Repayment>()
    }

    suspend fun getDashboardSummary(borrowerId: String): Result<BorrowerDashboardSummary> = runCatching {
        val approvedLoans = getCustomerApprovedLoans(borrowerId).getOrThrow()
        val totalBorrowed = approvedLoans.sumOf { it.totalPayable }
        val totalPaid = approvedLoans.sumOf { it.totalPaid }
        val totalRemaining = approvedLoans.sumOf { maxOf(0.0, it.remainingBalance) }
        val totalTodayDue = approvedLoans.sumOf { it.todayDue }
        val totalTodayPaid = approvedLoans.sumOf { it.todayPaid }

        BorrowerDashboardSummary(
            borrowerId = borrowerId,
            totalBorrowed = totalBorrowed,
            totalPaid = totalPaid,
            totalRemaining = totalRemaining,
            todayDue = totalTodayDue,
            todayPaid = totalTodayPaid,
            activeLoansCount = approvedLoans.size
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