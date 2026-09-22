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

            // Standard A4 Portrait dimensions in points (72 DPI)
            val pageWidth = 595
            val pageHeight = 842

            val titlePaint = Paint().apply {
                color = Color.rgb(24, 75, 140) // Brand Primary
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val tableHeaderPaint = Paint().apply {
                color = Color.WHITE
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val nameTextPaint = Paint().apply {
                color = Color.rgb(20, 20, 20)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subTextPaint = Paint().apply {
                color = Color.GRAY
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val amountTextPaint = Paint().apply {
                color = Color.rgb(30, 30, 30)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val tableFooterPaint = Paint().apply {
                color = Color.rgb(20, 20, 20)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(225, 230, 238)
                strokeWidth = 0.8f
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
            val currentDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            fun drawHeader(cv: Canvas) {
                // Business Name Header (Top)
                cv.drawText(businessName.uppercase(Locale.getDefault()), 28f, 42f, titlePaint)
                cv.drawText("MASTER LOAN LEDGER & COLLECTION STATEMENT", 28f, 57f, subtitlePaint)
                cv.drawText("Generated: $currentDate | Proprietor: $ownerName", 28f, 69f, subtitlePaint)

                // 2x2 Summary Metric Card in Portrait
                rectPaint.color = Color.rgb(244, 247, 252)
                cv.drawRoundRect(28f, 80f, 567f, 138f, 8f, 8f, rectPaint)

                val labelPaint = Paint().apply {
                    color = Color.rgb(80, 80, 80)
                    textSize = 8.5f
                    isAntiAlias = true
                }
                val valuePaint = Paint().apply {
                    color = Color.rgb(20, 20, 20)
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }

                // Row 1
                cv.drawText("Total Disbursed:", 42f, 98f, labelPaint)
                cv.drawText("₹${summary?.totalDisbursed ?: 0.0}", 125f, 98f, valuePaint)

                cv.drawText("Total Recovered:", 310f, 98f, labelPaint)
                cv.drawText("₹${summary?.totalPaid ?: 0.0}", 395f, 98f, valuePaint)

                // Row 2
                cv.drawText("Today's Total Due:", 42f, 122f, labelPaint)
                cv.drawText("₹${summary?.totalDueBalance ?: 0.0}", 125f, 122f, valuePaint)

                cv.drawText("Total Outstanding:", 310f, 122f, labelPaint)
                cv.drawText("₹${summary?.totalRemainingBalance ?: 0.0}", 395f, 122f, valuePaint)

                // Table Header Bar (Width: 28f to 567f)
                rectPaint.color = Color.rgb(24, 75, 140)
                cv.drawRect(28f, 150f, 567f, 174f, rectPaint)

                cv.drawText("SL", 34f, 166f, tableHeaderPaint)
                cv.drawText("NAME / CLIENT", 60f, 166f, tableHeaderPaint)
                cv.drawText("TODAY DUE", 230f, 166f, tableHeaderPaint)
                cv.drawText("EMI PAID", 315f, 166f, tableHeaderPaint)
                cv.drawText("TOTAL PAID", 398f, 166f, tableHeaderPaint)
                cv.drawText("REMAINING BAL", 482f, 166f, tableHeaderPaint)
            }

            drawHeader(canvas)

            var y = 194f
            val rowHeight = 25f

            duesList.forEachIndexed { index, item ->
                // Check if page bottom limit is reached in Portrait
                if (y > pageHeight - 55f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    canvas.drawText("$businessName - Master Ledger (Page $pageNumber)", 28f, 32f, subtitlePaint)
                    rectPaint.color = Color.rgb(24, 75, 140)
                    canvas.drawRect(28f, 44f, 567f, 68f, rectPaint)

                    canvas.drawText("SL", 34f, 60f, tableHeaderPaint)
                    canvas.drawText("NAME / CLIENT", 60f, 60f, tableHeaderPaint)
                    canvas.drawText("TODAY DUE", 230f, 60f, tableHeaderPaint)
                    canvas.drawText("EMI PAID", 315f, 60f, tableHeaderPaint)
                    canvas.drawText("TOTAL PAID", 398f, 60f, tableHeaderPaint)
                    canvas.drawText("REMAINING BAL", 482f, 60f, tableHeaderPaint)

                    y = 86f
                }

                // Alternating zebra row background
                if (index % 2 == 1) {
                    rectPaint.color = Color.rgb(249, 251, 254)
                    canvas.drawRect(28f, y - 14f, 567f, y + 11f, rectPaint)
                }

                // Row values
                canvas.drawText("${index + 1}", 34f, y + 2f, amountTextPaint)

                // Borrower Name & Mobile stacked cleanly
                canvas.drawText(item.borrowerName.take(22), 60f, y - 2f, nameTextPaint)
                canvas.drawText("+91 ${item.borrowerMobile}", 60f, y + 9f, subTextPaint)

                canvas.drawText("₹${maxOf(0.0, item.todayDueBalance)}", 230f, y + 2f, amountTextPaint)
                canvas.drawText("₹${item.todayPaidAmount}", 315f, y + 2f, amountTextPaint)
                canvas.drawText("₹${item.totalPaid}", 398f, y + 2f, amountTextPaint)
                canvas.drawText("₹${maxOf(0.0, item.remainingBalance)}", 482f, y + 2f, amountTextPaint)

                canvas.drawLine(28f, y + 11f, 567f, y + 11f, linePaint)
                y += rowHeight
            }

            // Draw Totals Footer Bar
            if (y > pageHeight - 45f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 48f
            }

            val sumTodayDue = duesList.sumOf { maxOf(0.0, it.todayDueBalance) }
            val sumEmiPaid = duesList.sumOf { it.todayPaidAmount }
            val sumTotalPaid = duesList.sumOf { it.totalPaid }
            val sumRemaining = duesList.sumOf { maxOf(0.0, it.remainingBalance) }

            rectPaint.color = Color.rgb(235, 241, 250)
            canvas.drawRect(28f, y - 13f, 567f, y + 15f, rectPaint)

            canvas.drawText("TOTALS (${duesList.size})", 42f, y + 3f, tableFooterPaint)
            canvas.drawText("₹$sumTodayDue", 230f, y + 3f, tableFooterPaint)
            canvas.drawText("₹$sumEmiPaid", 315f, y + 3f, tableFooterPaint)
            canvas.drawText("₹$sumTotalPaid", 398f, y + 3f, tableFooterPaint)
            canvas.drawText("₹$sumRemaining", 482f, y + 3f, tableFooterPaint)

            pdfDocument.finishPage(page)

            // Save PDF to Documents storage
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val safeBusinessName = businessName.replace("\\s+".toRegex(), "_")
            val pdfFile = File(outputDir, "${safeBusinessName}_Master_Ledger_${System.currentTimeMillis()}.pdf")

            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Open via FileProvider
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

            val chooser = Intent.createChooser(viewIntent, "Open Master Ledger PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
