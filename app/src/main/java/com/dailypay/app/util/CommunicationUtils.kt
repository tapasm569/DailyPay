package com.dailypay.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object CommunicationUtils {

    fun openWhatsAppChat(context: Context, rawMobileNumber: String, message: String = "") {
        val cleanNumber = rawMobileNumber.filter { it.isDigit() }
        if (cleanNumber.isBlank()) {
            Toast.makeText(context, "Invalid mobile number.", Toast.LENGTH_SHORT).show()
            return
        }

        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
        val encodedMessage = Uri.encode(message)
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage")

        // 1. Try launching WhatsApp directly
        val directIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(directIntent)
        } catch (_: ActivityNotFoundException) {
            // 2. Fallback to WhatsApp Business or default web browser
            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "No app found to open WhatsApp link.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPhoneDialer(context: Context, rawMobileNumber: String) {
        val cleanNumber = rawMobileNumber.filter { it.isDigit() }
        if (cleanNumber.isBlank()) {
            Toast.makeText(context, "Invalid phone number.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(dialIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No phone dialer application found on this device.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch dialer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
