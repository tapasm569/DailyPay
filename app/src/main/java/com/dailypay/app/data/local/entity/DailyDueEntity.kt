package com.dailypay.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dailypay.app.data.model.DailyDueItem

@Entity(tableName = "daily_dues")
data class DailyDueEntity(
    @PrimaryKey
    val loanId: String,
    val lenderId: String,
    val borrowerId: String,
    val borrowerName: String,
    val borrowerMobile: String,
    val dailyInstallment: Double,
    val todayDueBalance: Double,
    val todayPaidAmount: Double,
    val totalPaid: Double,
    val remainingBalance: Double,
    val totalPayable: Double
) {
    fun toDailyDueItem() = DailyDueItem(
        loanId = loanId,
        borrowerId = borrowerId,
        borrowerName = borrowerName,
        borrowerMobile = borrowerMobile,
        dailyInstallment = dailyInstallment,
        todayDueBalance = todayDueBalance,
        todayPaidAmount = todayPaidAmount,
        totalPaid = totalPaid,
        remainingBalance = remainingBalance,
        totalPayable = totalPayable
    )

    companion object {
        fun fromModel(lenderId: String, item: DailyDueItem) = DailyDueEntity(
            loanId = item.loanId,
            lenderId = lenderId,
            borrowerId = item.borrowerId,
            borrowerName = item.borrowerName,
            borrowerMobile = item.borrowerMobile,
            dailyInstallment = item.dailyInstallment,
            todayDueBalance = item.todayDueBalance,
            todayPaidAmount = item.todayPaidAmount,
            totalPaid = item.totalPaid,
            remainingBalance = item.remainingBalance,
            totalPayable = item.totalPayable
        )
    }
}
