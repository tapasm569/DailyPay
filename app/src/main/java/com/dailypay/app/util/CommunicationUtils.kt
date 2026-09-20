package com.dailypay.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object CommunicationUtils {

    /**
     * Opens WhatsApp chat directly with a pre-composed message.
     */
    fun openWhatsAppChat(
        context: Context,
        rawMobileNumber: String,
        message: String = "Hello from DailyPay"
    ) {
        val cleanNumber = rawMobileNumber.filter { it.isDigit() }
        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
        val encodedMessage = Uri.encode(message)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens Phone dialer with number pre-filled.
     */
    fun openPhoneDialer(context: Context, rawMobileNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$rawMobileNumber")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch phone dialer.", Toast.LENGTH_SHORT).show()
        }
    }
}
