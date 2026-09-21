package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.PendingPaymentItem
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyPaymentScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var pendingPayments by remember { mutableStateOf<List<PendingPaymentItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var processingId by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        scope.launch {
            lenderRepo.getPendingVerifications(lenderId)
                .onSuccess {
                    pendingPayments = it
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(lenderId) {
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
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        } else if (pendingPayments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No pending payments awaiting verification.", style = MaterialTheme.typography.titleMedium, color = TextSecondaryLight)
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
                itemsIndexed(pendingPayments, key = { index, item -> item.repayment.id ?: "pending_$index" }) { _, item ->
                    val r = item.repayment

                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.borrowerName, style = MaterialTheme.typography.titleMedium)
                                    Text("+91 ${item.borrowerMobile}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(r.paymentMode.name) }
                                )
                            }

                            HorizontalDivider(color = BorderSubtleLight)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Payment Amount", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text("₹${r.amountPaid}", style = MaterialTheme.typography.headlineSmall, color = MoneyGreen)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Date Submitted", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text(r.paymentDate ?: "-", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            if (!r.utrReference.isNullOrBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = BrandPrimary.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = "UTR / Ref: ${r.utrReference}",
                                        modifier = Modifier.padding(10.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandPrimary
                                    )
                                }
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
                                            lenderRepo.verifyRepayment(id, accept = true)
                                                .onSuccess {
                                                    processingId = null
                                                    Toast.makeText(context, "Payment verified & approved successfully!", Toast.LENGTH_SHORT).show()
                                                    loadData()
                                                }
                                                .onFailure {
                                                    processingId = null
                                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                    enabled = processingId == null,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                                ) {
                                    Text(if (processingId == r.id) "Verifying..." else "Verify & Approve")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val id = r.id ?: return@OutlinedButton
                                        processingId = id
                                        scope.launch {
                                            lenderRepo.verifyRepayment(id, accept = false)
                                                .onSuccess {
                                                    processingId = null
                                                    Toast.makeText(context, "Payment rejected.", Toast.LENGTH_SHORT).show()
                                                    loadData()
                                                }
                                                .onFailure {
                                                    processingId = null
                                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
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
    }
}