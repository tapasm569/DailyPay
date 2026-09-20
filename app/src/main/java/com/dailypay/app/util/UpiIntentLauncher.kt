package com.dailypay.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale

object UpiIntentLauncher {

    fun initiateUpiPayment(
        context: Context,
        payeeUpiId: String,
        payeeName: String,
        amount: Double,
        transactionNote: String = "DailyPay Loan Settlement"
    ) {
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        val encodedName = Uri.encode(payeeName)
        val encodedNote = Uri.encode(transactionNote)

        val upiUri = Uri.parse(
            "upi://pay?pa=$payeeUpiId&pn=$encodedName&am=$formattedAmount&cu=INR&tn=$encodedNote"
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = upiUri
        }

        val chooser = Intent.createChooser(intent, "Pay ₹$formattedAmount via UPI")
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No UPI application (GPay, PhonePe, Paytm) found on device.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
