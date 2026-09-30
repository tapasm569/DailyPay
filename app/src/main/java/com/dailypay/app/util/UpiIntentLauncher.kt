package com.dailypay.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale

object UpiIntentLauncher {

    /**
     * Launches a native Android UPI intent chooser (GPay, PhonePe, Paytm, BHIM, etc.)
     * for a borrower to pay their daily installment directly to the lender's VPA.
     *
     * @return true if the UPI chooser was successfully launched; false otherwise.
     */
    fun initiateUpiPayment(
        context: Context,
        payeeUpiId: String?,
        payeeName: String?,
        amount: Double,
        transactionNote: String = "DailyPay Loan Settlement"
    ): Boolean {
        val cleanUpi = payeeUpiId?.trim().orEmpty()
        if (cleanUpi.isBlank()) {
            Toast.makeText(
                context,
                "Lender has not configured a receiving UPI ID yet.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        if (amount <= 0.0) {
            Toast.makeText(context, "Invalid repayment amount.", Toast.LENGTH_SHORT).show()
            return false
        }

        val cleanName = payeeName?.trim()?.takeIf { it.isNotBlank() } ?: "DailyPay Lender"
        val cleanNote = transactionNote.trim().ifBlank { "DailyPay Loan Settlement" }
        val formattedAmount = String.format(Locale.US, "%.2f", amount)

        val upiUri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", cleanUpi)
            .appendQueryParameter("pn", cleanName)
            .appendQueryParameter("am", formattedAmount)
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", cleanNote)
            .build()

        val intent = Intent(Intent.ACTION_VIEW, upiUri)
        val chooser = Intent.createChooser(intent, "Pay ₹$formattedAmount via UPI")

        if (context !is Activity) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No supported UPI app (GPay, PhonePe, Paytm, etc.) found on device.",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
}
