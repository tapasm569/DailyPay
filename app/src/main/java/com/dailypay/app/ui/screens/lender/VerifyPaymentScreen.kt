package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.PendingPaymentItem
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.AlertOrange
import com.dailypay.app.ui.theme.BorderSubtleLight
import com.dailypay.app.ui.theme.BrandPrimary
import com.dailypay.app.ui.theme.DangerRed
import com.dailypay.app.ui.theme.MoneyGreen
import com.dailypay.app.ui.theme.TextSecondaryLight
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyPaymentScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val lenderRepo = remember { LenderRepository(context.applicationContext) }

    var pendingPayments by remember { mutableStateOf<List<PendingPaymentItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var processingId by remember { mutableStateOf<String?>(null) }
    var itemToReject by remember { mutableStateOf<PendingPaymentItem?>(null) }

    val cleanLenderId = remember(lenderId) { lenderId.trim() }

    fun loadData() {
        if (cleanLenderId.isBlank() || cleanLenderId == "{lenderId}") {
            isLoading = false
            return
        }
        scope.launch {
            lenderRepo.getPendingVerifications(cleanLenderId)
                .onSuccess {
                    pendingPayments = it
                    isLoading = false
                }
                .onFailure { error ->
                    isLoading = false
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(cleanLenderId) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Payments (${pendingPayments.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        } else if (pendingPayments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MoneyGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No pending payments awaiting verification.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MoneyGreen
                    )
                    Text(
                        text = "All submitted borrower payments are processed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(
                    items = pendingPayments,
                    key = { index, item -> item.repayment.id?.takeIf { it.isNotBlank() } ?: "pending_${item.borrowerMobile}_$index" }
                ) { _, item ->
                    val r = item.repayment
                    val formattedAmount = remember(r.amountPaid) {
                        String.format(Locale.US, "%.2f", r.amountPaid)
                    }
                    val borrowerDisplayName = item.borrowerName.ifBlank { "Customer" }
                    val utrText = r.utrReference?.trim().orEmpty()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = borrowerDisplayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "+91 ${item.borrowerMobile}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight
                                    )
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(r.paymentMode.name) }
                                )
                            }

                            HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.6f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Payment Amount", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text(
                                        text = "₹$formattedAmount",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MoneyGreen
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Date Submitted", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text(
                                        text = r.paymentDate?.trim()?.ifBlank { "Today" } ?: "Today",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // 12-Digit UTR Reference Display with One-Tap Copy
                            if (utrText.isNotBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = BrandPrimary.copy(alpha = 0.08f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Bank Ref / UTR (12-Digit):",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BrandPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = utrText,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(utrText))
                                                Toast.makeText(context, "UTR copied: $utrText", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy UTR",
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (!r.notes.isNullOrBlank()) {
                                Text(
                                    text = "Note: ${r.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val id = r.id ?: return@Button
                                        processingId = id
                                        scope.launch {
                                            lenderRepo.verifyRepayment(id, accept = true, lenderId = cleanLenderId)
                                                .onSuccess {
                                                    processingId = null
                                                    Toast.makeText(context, "Payment verified & approved successfully!", Toast.LENGTH_SHORT).show()
                                                    loadData()
                                                }
                                                .onFailure { error ->
                                                    processingId = null
                                                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                    enabled = processingId == null,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                                ) {
                                    if (processingId == r.id) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Verifying...")
                                    } else {
                                        Text("Verify & Approve")
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        itemToReject = item
                                    },
                                    enabled = processingId == null,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= REJECTION CONFIRMATION DIALOG =================
        itemToReject?.let { item ->
            val repaymentId = item.repayment.id.orEmpty()
            AlertDialog(
                onDismissRequest = { if (processingId == null) itemToReject = null },
                title = { Text("Reject Payment") },
                text = {
                    Text(
                        text = "Are you sure you want to reject the payment of ₹${String.format(Locale.US, "%.2f", item.repayment.amountPaid)} from ${item.borrowerName.ifBlank { "Customer" }}? This will keep their due balance open."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (repaymentId.isBlank()) return@Button
                            processingId = repaymentId
                            scope.launch {
                                lenderRepo.verifyRepayment(repaymentId, accept = false, lenderId = cleanLenderId)
                                    .onSuccess {
                                        processingId = null
                                        itemToReject = null
                                        Toast.makeText(context, "Payment marked as rejected.", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure { error ->
                                        processingId = null
                                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        enabled = processingId == null
                    ) {
                        Text("Confirm Reject")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { itemToReject = null },
                        enabled = processingId == null
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
