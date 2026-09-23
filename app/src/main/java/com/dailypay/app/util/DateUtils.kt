package com.dailypay.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    // Indian standard date formatter (e.g. 23/09/2026)
    val indianDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("en", "IN"))

    // Card badge & UI display formatter (e.g. 23 Sep 2026)
    val displayDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    // WhatsApp digital receipt timestamp formatter (e.g. 23 September 2026, 08:30 AM)
    val receiptDateTimeFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a", Locale.getDefault())

    private val isoDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    /**
     * Returns today's date in Indian standard format (e.g. 23/09/2026)
     */
    fun getTodayIndianFormat(): String {
        return LocalDate.now().format(indianDateFormatter)
    }

    /**
     * Returns today's date formatted for card badges and headers (e.g. 23 Sep 2026)
     */
    fun getTodayDisplayFormat(): String {
        return LocalDate.now().format(displayDateFormatter)
    }

    /**
     * Returns today's date in SQL format (e.g. 2026-09-23) for database queries
     */
    fun getTodaySqlFormat(): String {
        return LocalDate.now().format(isoDateFormatter)
    }

    /**
     * Returns the current date and time formatted for digital WhatsApp receipts (e.g. 23 September 2026, 08:30 AM)
     */
    fun getCurrentReceiptDateTime(): String {
        return LocalDateTime.now().format(receiptDateTimeFormatter)
    }

    /**
     * Converts Supabase ISO timestamp or SQL Date to DD/MM/YYYY
     */
    fun formatToIndianDate(rawDateStr: String?): String {
        if (rawDateStr.isNullOrBlank()) return "-"
        return try {
            if (rawDateStr.contains("T")) {
                val parsed = OffsetDateTime.parse(rawDateStr)
                parsed.format(indianDateFormatter)
            } else {
                val parsed = LocalDate.parse(rawDateStr.take(10), isoDateFormatter)
                parsed.format(indianDateFormatter)
            }
        } catch (e: Exception) {
            rawDateStr.take(10)
        }
    }

    /**
     * Converts Supabase ISO timestamp or SQL Date to display format (e.g. 23 Sep 2026)
     */
    fun formatToDisplayDate(rawDateStr: String?): String {
        if (rawDateStr.isNullOrBlank()) return "-"
        return try {
            if (rawDateStr.contains("T")) {
                val parsed = OffsetDateTime.parse(rawDateStr)
                parsed.format(displayDateFormatter)
            } else {
                val parsed = LocalDate.parse(rawDateStr.take(10), isoDateFormatter)
                parsed.format(displayDateFormatter)
            }
        } catch (e: Exception) {
            rawDateStr.take(10)
        }
    }
}
