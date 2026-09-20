package com.dailypay.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.dailypay.app.data.model.DailyDueItem
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    /**
     * Builds and exports a ledger statement into PDF format, then launches the device PDF viewer.
     */
    fun generateLenderLedgerPdf(
        context: Context,
        businessName: String,
        duesList: List<DailyDueItem>
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E1B4B")
            textSize = 18f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#4F46E5")
            textSize = 11f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 10f
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        // Header Title
        canvas.drawText("DailyPay - Business Ledger Book", 40f, 50f, titlePaint)
        canvas.drawText("Business: $businessName | Date: ${DateUtils.getTodayIndianFormat()}", 40f, 72f, bodyPaint)

        canvas.drawLine(40f, 85f, 555f, 85f, linePaint)

        // Table Column Headers
        var yPos = 110f
        canvas.drawText("SL", 40f, yPos, headerPaint)
        canvas.drawText("Borrower Name", 70f, yPos, headerPaint)
        canvas.drawText("Today Due", 220f, yPos, headerPaint)
        canvas.drawText("Today Paid", 310f, yPos, headerPaint)
        canvas.drawText("Remaining", 410f, yPos, headerPaint)
        canvas.drawText("Status", 500f, yPos, headerPaint)

        canvas.drawLine(40f, yPos + 8f, 555f, yPos + 8f, linePaint)

        yPos += 26f

        // Table Rows
        duesList.forEachIndexed { index, item ->
            if (yPos < 800f) {
                canvas.drawText("${index + 1}", 40f, yPos, bodyPaint)
                canvas.drawText(item.borrowerName.take(18), 70f, yPos, bodyPaint)
                canvas.drawText("₹${item.todayDueBalance}", 220f, yPos, bodyPaint)
                canvas.drawText("₹${item.todayPaidAmount}", 310f, yPos, bodyPaint)
                canvas.drawText("₹${item.remainingBalance}", 410f, yPos, bodyPaint)
                
                val statusText = if (item.todayPaidAmount >= item.todayDueBalance && item.todayDueBalance > 0) "PAID" else "DUE"
                canvas.drawText(statusText, 500f, yPos, bodyPaint)

                canvas.drawLine(40f, yPos + 6f, 555f, yPos + 6f, linePaint)
                yPos += 22f
            }
        }

        pdfDocument.finishPage(page)

        // Save PDF to cache and open via FileProvider
        try {
            val fileName = "DailyPay_Ledger_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            openPdfFile(context, file)
        } catch (e: Exception) {
            Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPdfFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF Viewer installed to open document.", Toast.LENGTH_LONG).show()
        }
    }
}
