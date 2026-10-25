package com.dailypay.app.util

import android.content.Context
import android.widget.Toast

object UpiIntentLauncher {
    fun initiateUpiPayment(
        context: Context,
        payeeUpiId: String = "",
        payeeName: String = "",
        amount: Double = 0.0,
        transactionNote: String = "Cash Only"
    ) {
        Toast.makeText(context, "Cash payments only.", Toast.LENGTH_SHORT).show()
    }
}
