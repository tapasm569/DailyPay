package com.dailypay.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    // Indian Standard Time (IST) zone used across DailyPay
    val ZONE_IST: ZoneId = ZoneId.of("Asia/Kolkata")
    val ZONE_UTC: ZoneId = ZoneOffset.UTC

    // Indian standard date formatter (e.g. 30/09/2026)
    val indianDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("en", "IN"))

    // Card badge & UI display formatter (e.g. 30 Sep 2026)
    val displayDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    // WhatsApp digital receipt timestamp formatter (e.g. 30 September 2026, 08:30 AM)
    val receiptDateTimeFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a", Locale.ENGLISH)

    // Standard SQL ISO date formatter (yyyy-MM-dd)
    private val isoDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    /**
     * Returns today's date in Indian standard format (e.g. 30/09/2026)
     */
    fun getTodayIndianFormat(): String {
        return LocalDate.now(ZONE_IST).format(indianDateFormatter)
    }

    /**
     * Returns today's date formatted for card badges and headers (e.g. 30 Sep 2026)
     */
    fun getTodayDisplayFormat(): String {
        return LocalDate.now(ZONE_IST).format(displayDateFormatter)
    }

    /**
     * Returns today's date in standard SQL format (e.g. 2026-09-30) locked to IST
     */
    fun getTodaySqlFormat(): String {
        return LocalDate.now(ZONE_IST).format(isoDateFormatter)
    }

    /**
     * Returns current UTC date string (e.g. 2026-09-30) for strict UTC database fields
     */
    fun getUtcDateString(): String {
        return LocalDate.now(ZONE_UTC).format(isoDateFormatter)
    }

    /**
     * Returns the current date and time formatted for digital WhatsApp receipts (e.g. 30 September 2026, 08:30 AM)
     */
    fun getCurrentReceiptDateTime(): String {
        return LocalDateTime.now(ZONE_IST).format(receiptDateTimeFormatter)
    }

    /**
     * Converts Supabase ISO timestamp or SQL Date string to DD/MM/YYYY
     */
    fun formatToIndianDate(rawDateStr: String?): String {
        if (rawDateStr.isNullOrBlank()) return "-"
        val cleanStr = rawDateStr.trim()
        return try {
            when {
                cleanStr.contains("T") -> {
                    // Try parsing with timezone offset, convert to IST
                    val odt = try {
                        OffsetDateTime.parse(cleanStr)
                    } catch (_: Exception) {
                        Instant.parse(cleanStr).atOffset(ZoneOffset.UTC)
                    }
                    odt.atZoneSameInstant(ZONE_IST).format(indianDateFormatter)
                }
                cleanStr.length >= 10 -> {
                    val parsed = LocalDate.parse(cleanStr.substring(0, 10), isoDateFormatter)
                    parsed.format(indianDateFormatter)
                }
                else -> cleanStr
            }
        } catch (e: Exception) {
            cleanStr.take(10)
        }
    }

    /**
     * Converts Supabase ISO timestamp or SQL Date to display format (e.g. 30 Sep 2026)
     */
    fun formatToDisplayDate(rawDateStr: String?): String {
        if (rawDateStr.isNullOrBlank()) return "-"
        val cleanStr = rawDateStr.trim()
        return try {
            when {
                cleanStr.contains("T") -> {
                    val odt = try {
                        OffsetDateTime.parse(cleanStr)
                    } catch (_: Exception) {
                        Instant.parse(cleanStr).atOffset(ZoneOffset.UTC)
                    }
                    odt.atZoneSameInstant(ZONE_IST).format(displayDateFormatter)
                }
                cleanStr.length >= 10 -> {
                    val parsed = LocalDate.parse(cleanStr.substring(0, 10), isoDateFormatter)
                    parsed.format(displayDateFormatter)
                }
                else -> cleanStr
            }
        } catch (e: Exception) {
            cleanStr.take(10)
        }
    }
}
