package com.dailypay.app.util

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    // Indian standard date formatter
    val indianDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("en", "IN"))

    private val isoDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    /**
     * Returns today's date in Indian format (e.g. 20/09/2026)
     */
    fun getTodayIndianFormat(): String {
        return LocalDate.now().format(indianDateFormatter)
    }

    /**
     * Returns today's date in SQL format (e.g. 2026-09-20) for database queries
     */
    fun getTodaySqlFormat(): String {
        return LocalDate.now().format(isoDateFormatter)
    }

    /**
     * Converts Supabase ISO timestamp or SQL Date to DD/MM/YYYY
     */
    fun formatToIndianDate(rawDateStr: String?): String {
        if (rawDateStr.isNullOrBlank()) return "-"
        return try {
            if (rawDateStr.contains("T")) {
                // ISO timestamp like 2026-09-20T10:15:30Z
                val parsed = OffsetDateTime.parse(rawDateStr)
                parsed.format(indianDateFormatter)
            } else {
                // Standard SQL date like 2026-09-20
                val parsed = LocalDate.parse(rawDateStr.take(10), isoDateFormatter)
                parsed.format(indianDateFormatter)
            }
        } catch (e: Exception) {
            rawDateStr.take(10)
        }
    }
}
