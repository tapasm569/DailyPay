package com.dailypay.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale

data class UpiPaymentResult(
    val status: String,           // "SUCCESS", "SUBMITTED", "FAILURE", or "UNKNOWN"
    val txnId: String?,
    val txnRef: String?,
    val approvalRefNo: String?,   // 12-digit UTR / Bank Reference Number
    val responseCode: String?,
    val rawResponse: String?
)

object UpiIntentLauncher {

    /**
     * Builds the UPI payment Intent so Jetpack Compose screens can launch it
     * using rememberLauncherForActivityResult to capture the returned UTR and status.
     */
    fun createUpiIntent(
        payeeUpiId: String,
        payeeName: String,
        amount: Double,
        transactionRef: String = "DP${System.currentTimeMillis()}",
        transactionNote: String = "Loan Repayment"
    ): Intent {
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        val encodedName = Uri.encode(payeeName)
        val encodedNote = Uri.encode(transactionNote)
        val encodedRef = Uri.encode(transactionRef)

        val upiUri = Uri.parse(
            "upi://pay?pa=$payeeUpiId&pn=$encodedName&tr=$encodedRef&am=$formattedAmount&cu=INR&tn=$encodedNote"
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = upiUri
        }
        return Intent.createChooser(intent, "Pay ₹$formattedAmount via UPI")
    }

    /**
     * Direct launch helper if result callback is not required.
     */
    fun initiateUpiPayment(
        context: Context,
        payeeUpiId: String,
        payeeName: String,
        amount: Double,
        transactionNote: String = "DailyPay Loan Settlement"
    ) {
        val chooser = createUpiIntent(
            payeeUpiId = payeeUpiId,
            payeeName = payeeName,
            amount = amount,
            transactionNote = transactionNote
        )
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

    /**
     * Parses the raw data string returned by external UPI apps into a structured UpiPaymentResult.
     */
    fun parseUpiResponse(data: Intent?): UpiPaymentResult {
        val responseStr = data?.getStringExtra("response")
            ?: data?.data?.toString()
            ?: data?.extras?.getString("response")
            ?: ""

        if (responseStr.isBlank()) {
            return UpiPaymentResult(
                status = "UNKNOWN",
                txnId = null,
                txnRef = null,
                approvalRefNo = null,
                responseCode = null,
                rawResponse = null
            )
        }

        val params = responseStr.split("&").associate { param ->
            val parts = param.split("=")
            if (parts.size >= 2) parts[0].trim().lowercase(Locale.ROOT) to parts[1].trim()
            else parts[0].trim().lowercase(Locale.ROOT) to ""
        }

        val statusRaw = (params["status"] ?: "").uppercase(Locale.ROOT)
        val parsedStatus = when {
            statusRaw.contains("SUCCESS") -> "SUCCESS"
            statusRaw.contains("FAIL") -> "FAILURE"
            statusRaw.contains("SUBMIT") -> "SUBMITTED"
            else -> "UNKNOWN"
        }

        return UpiPaymentResult(
            status = parsedStatus,
            txnId = params["txnid"],
            txnRef = params["txnref"],
            approvalRefNo = params["approvalrefno"] ?: params["refno"] ?: params["utr"],
            responseCode = params["responsecode"],
            rawResponse = responseStr
        )
    }
}
