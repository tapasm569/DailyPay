package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.UpiIntentLauncher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLoanRequestScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var pendingLoans by remember { mutableStateOf<List<Loan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Approval Dialog State
    var selectedLoanToApprove by remember { mutableStateOf<Loan?>(null) }
    var customInterestRate by remember { mutableStateOf("10.0") }
    var selectedDisbursementMode by remember { mutableStateOf(PaymentMode.UPI) }
    var isApproving by remember { mutableStateOf(false) }

    fun loadRequests() {
        scope.launch {
            lenderRepo.getPendingLoanRequests(lenderId)
                .onSuccess {
                    pendingLoans = it
                    isLoading = false
                }
                .onFailure { isLoading = false }
        }
    }

    LaunchedEffect(lenderId) {
        loadRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Loan Requests") },
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
        } else if (pendingLoans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    text = "No pending loan requests.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondaryLight
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(pendingLoans) { loan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Principal: ₹${loan.principalAmount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(shape = RoundedCornerShape(8.dp), color = AlertOrangeSubtle) {
                                    Text(
                                        text = "${loan.tenureDays} Days",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AlertOrange
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Requested Interest: ${loan.monthlyInterestRate}% / month",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    selectedLoanToApprove = loan
                                    customInterestRate = loan.monthlyInterestRate.toString()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                            ) {
                                Text("Review & Disburse")
                            }
                        }
                    }
                }
            }
        }

        // Review & Disburse Dialog
        selectedLoanToApprove?.let { loan ->
            AlertDialog(
                onDismissRequest = { if (!isApproving) selectedLoanToApprove = null },
                title = { Text("Approve & Disburse Loan") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Amount: ₹${loan.principalAmount} | Tenure: ${loan.tenureDays} Days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )

                        OutlinedTextField(
                            value = customInterestRate,
                            onValueChange = { customInterestRate = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Modify Interest Rate (%/Month)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(text = "Select Payment Method:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedDisbursementMode == PaymentMode.UPI,
                                onClick = { selectedDisbursementMode = PaymentMode.UPI },
                                label = { Text("UPI Direct") }
                            )
                            FilterChip(
                                selected = selectedDisbursementMode == PaymentMode.CASH,
                                onClick = { selectedDisbursementMode = PaymentMode.CASH },
                                label = { Text("Cash") }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val rate = customInterestRate.toDoubleOrNull() ?: loan.monthlyInterestRate
                            if (selectedDisbursementMode == PaymentMode.UPI) {
                                UpiIntentLauncher.initiateUpiPayment(
                                    context = context,
                                    payeeUpiId = "borrower@upi",
                                    payeeName = "Borrower",
                                    amount = loan.principalAmount,
                                    transactionNote = "DailyPay Loan Approval"
                                )
                            }

                            isApproving = true
                            scope.launch {
                                lenderRepo.approveAndDisburseLoan(
                                    loanId = loan.id ?: "",
                                    interestRate = rate,
                                    disbursementMode = selectedDisbursementMode,
                                    disbursementRef = "DISB-${System.currentTimeMillis()}"
                                ).onSuccess {
                                    isApproving = false
                                    selectedLoanToApprove = null
                                    Toast.makeText(context, "Loan approved and active!", Toast.LENGTH_SHORT).show()
                                    loadRequests()
                                }.onFailure {
                                    isApproving = false
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isApproving,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isApproving) "Processing..." else "Send Money & Approve")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedLoanToApprove = null }, enabled = !isApproving) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
