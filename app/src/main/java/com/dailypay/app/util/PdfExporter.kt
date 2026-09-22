package com.dailypay.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.LenderLedgerSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generateAndOpenLenderLedgerPdf(
        context: Context,
        lender: Lender?,
        summary: LenderLedgerSummary?,
        duesList: List<DailyDueItem>
    ) {
        try {
            val pdfDocument = PdfDocument()

            // A4 Landscape dimensions in points (72 DPI)
            val pageWidth = 842
            val pageHeight = 595

            val titlePaint = Paint().apply {
                color = Color.rgb(24, 75, 140) // Brand Primary
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val tableHeaderPaint = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val tableTextPaint = Paint().apply {
                color = Color.BLACK
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val tableFooterPaint = Paint().apply {
                color = Color.rgb(20, 20, 20)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(215, 220, 228)
                strokeWidth = 1f
            }

            val rectPaint = Paint().apply {
                style = Paint.Style.FILL
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            val businessName = lender?.businessName?.ifBlank { lender.name }?.ifBlank { "DAILYPAY FINANCE" } ?: "DAILYPAY FINANCE"
            val ownerName = lender?.ownerName?.ifBlank { lender.name } ?: "Lender"
            val currentDate = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            fun drawHeader(cv: Canvas) {
                // Business Name Header
                cv.drawText(businessName.uppercase(Locale.getDefault()), 40f, 45f, titlePaint)
                cv.drawText("MASTER LOAN LEDGER & DAILY COLLECTION REPORT", 40f, 62f, subtitlePaint)
                cv.drawText("Generated On: $currentDate | Proprietor: $ownerName", 40f, 76f, subtitlePaint)

                // Summary Metric Cards
                rectPaint.color = Color.rgb(244, 246, 250)
                cv.drawRoundRect(40f, 88f, 802f, 132f, 8f, 8f, rectPaint)

                val metricPaint = Paint().apply {
                    color = Color.rgb(40, 40, 40)
                    textSize = 9.5f
                    isAntiAlias = true
                }
                val boldMetric = Paint().apply {
                    color = Color.rgb(20, 20, 20)
                    textSize = 10.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }

                cv.drawText("Total Disbursed:", 52f, 106f, metricPaint)
                cv.drawText("₹${summary?.totalDisbursed ?: 0.0}", 52f, 122f, boldMetric)

                cv.drawText("Total Recovered:", 230f, 106f, metricPaint)
                cv.drawText("₹${summary?.totalPaid ?: 0.0}", 230f, 122f, boldMetric)

                cv.drawText("Today's Total Due:", 420f, 106f, metricPaint)
                cv.drawText("₹${summary?.totalDueBalance ?: 0.0}", 420f, 122f, boldMetric)

                cv.drawText("Total Outstanding Bal:", 610f, 106f, metricPaint)
                cv.drawText("₹${summary?.totalRemainingBalance ?: 0.0}", 610f, 122f, boldMetric)

                // Table Header Bar
                rectPaint.color = Color.rgb(24, 75, 140)
                cv.drawRect(40f, 144f, 802f, 168f, rectPaint)

                cv.drawText("SL", 46f, 160f, tableHeaderPaint)
                cv.drawText("BORROWER NAME", 75f, 160f, tableHeaderPaint)
                cv.drawText("MOBILE", 245f, 160f, tableHeaderPaint)
                cv.drawText("TODAY'S DUE (₹)", 370f, 160f, tableHeaderPaint)
                cv.drawText("EMI PAID (₹)", 495f, 160f, tableHeaderPaint)
                cv.drawText("TOTAL PAID (₹)", 605f, 160f, tableHeaderPaint)
                cv.drawText("REMAINING BAL (₹)", 705f, 160f, tableHeaderPaint)
            }

            drawHeader(canvas)

            var y = 188f
            val rowHeight = 22f

            duesList.forEachIndexed { index, item ->
                // Check if page limit reached
                if (y > pageHeight - 55f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    // Simplified top header on next pages
                    canvas.drawText("$businessName - Master Ledger (Page $pageNumber)", 40f, 35f, subtitlePaint)
                    rectPaint.color = Color.rgb(24, 75, 140)
                    canvas.drawRect(40f, 48f, 802f, 72f, rectPaint)

                    canvas.drawText("SL", 46f, 64f, tableHeaderPaint)
                    canvas.drawText("BORROWER NAME", 75f, 64f, tableHeaderPaint)
                    canvas.drawText("MOBILE", 245f, 64f, tableHeaderPaint)
                    canvas.drawText("TODAY'S DUE (₹)", 370f, 64f, tableHeaderPaint)
                    canvas.drawText("EMI PAID (₹)", 495f, 64f, tableHeaderPaint)
                    canvas.drawText("TOTAL PAID (₹)", 605f, 64f, tableHeaderPaint)
                    canvas.drawText("REMAINING BAL (₹)", 705f, 64f, tableHeaderPaint)

                    y = 90f
                }

                // Alternating row background
                if (index % 2 == 1) {
                    rectPaint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(40f, y - 15f, 802f, y + 7f, rectPaint)
                }

                canvas.drawText("${index + 1}", 46f, y, tableTextPaint)
                canvas.drawText(item.borrowerName.take(24), 75f, y, tableTextPaint)
                canvas.drawText("+91 ${item.borrowerMobile}", 245f, y, tableTextPaint)
                canvas.drawText("₹${maxOf(0.0, item.todayDueBalance)}", 370f, y, tableTextPaint)
                canvas.drawText("₹${item.todayPaidAmount}", 495f, y, tableTextPaint)
                canvas.drawText("₹${item.totalPaid}", 605f, y, tableTextPaint)
                canvas.drawText("₹${maxOf(0.0, item.remainingBalance)}", 705f, y, tableTextPaint)

                canvas.drawLine(40f, y + 7f, 802f, y + 7f, linePaint)
                y += rowHeight
            }

            // Draw Totals Footer Bar
            if (y > pageHeight - 45f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }

            val sumTodayDue = duesList.sumOf { maxOf(0.0, it.todayDueBalance) }
            val sumEmiPaid = duesList.sumOf { it.todayPaidAmount }
            val sumTotalPaid = duesList.sumOf { it.totalPaid }
            val sumRemaining = duesList.sumOf { maxOf(0.0, it.remainingBalance) }

            rectPaint.color = Color.rgb(235, 240, 248)
            canvas.drawRect(40f, y - 14f, 802f, y + 14f, rectPaint)

            canvas.drawText("TOTALS (${duesList.size} Accounts)", 75f, y + 2f, tableFooterPaint)
            canvas.drawText("₹$sumTodayDue", 370f, y + 2f, tableFooterPaint)
            canvas.drawText("₹$sumEmiPaid", 495f, y + 2f, tableFooterPaint)
            canvas.drawText("₹$sumTotalPaid", 605f, y + 2f, tableFooterPaint)
            canvas.drawText("₹$sumRemaining", 705f, y + 2f, tableFooterPaint)

            pdfDocument.finishPage(page)

            // Save PDF to Documents storage
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            val safeBusinessName = businessName.replace("\\s+".toRegex(), "_")
            val pdfFile = File(outputDir, "${safeBusinessName}_Master_Ledger_${System.currentTimeMillis()}.pdf")

            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Open the PDF directly using FileProvider
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "Open Master Ledger PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
