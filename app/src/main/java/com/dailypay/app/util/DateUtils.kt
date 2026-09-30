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

    val ZONE_IST: ZoneId = ZoneId.of("Asia/Kolkata")
    val ZONE_UTC: ZoneId = ZoneOffset.UTC

    val indianDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("en", "IN"))

    val displayDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    val receiptDateTimeFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a", Locale.ENGLISH)

    private val isoDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun getTodayIndianFormat(): String {
        return LocalDate.now(ZONE_IST).format(indianDateFormatter)
    }

    fun getTodayDisplayFormat(): String {
        return LocalDate.now(ZONE_IST).format(displayDateFormatter)
    }

    fun getTodaySqlFormat(): String {
        return LocalDate.now(ZONE_IST).format(isoDateFormatter)
    }

    fun getUtcDateString(): String {
        return LocalDate.now(ZONE_UTC).format(isoDateFormatter)
    }

    fun getCurrentReceiptDateTime(): String {
        return LocalDateTime.now(ZONE_IST).format(receiptDateTimeFormatter)
    }

    fun formatToIndianDate(rawDateStr: String?): String {
        if (rawDateStr.isNullOrBlank()) return "-"
        val cleanStr = rawDateStr.trim()
        return try {
            when {
                cleanStr.contains("T") -> {
                    val odt = try {
                        OffsetDateTime.parse(cleanStr)
                    } catch (e: Exception) {
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

    fun formatToDisplayDate(rawDateStr: String?): String {
        if (rawDateStr.isNullOrBlank()) return "-"
        val cleanStr = rawDateStr.trim()
        return try {
            when {
                cleanStr.contains("T") -> {
                    val odt = try {
                        OffsetDateTime.parse(cleanStr)
                    } catch (e: Exception) {
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
