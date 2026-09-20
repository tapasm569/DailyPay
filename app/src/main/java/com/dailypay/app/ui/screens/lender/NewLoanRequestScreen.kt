package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils
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
    var borrowersMap by remember { mutableStateOf<Map<String, Borrower>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // Approval Dialog State
    var selectedLoanToApprove by remember { mutableStateOf<Loan?>(null) }
    var customInterestRate by remember { mutableStateOf("10.0") }
    var selectedDisbursementMode by remember { mutableStateOf(PaymentMode.CASH) }
    var isApproving by remember { mutableStateOf(false) }

    fun loadRequests() {
        scope.launch {
            lenderRepo.getBorrowers(lenderId).onSuccess { list ->
                borrowersMap = list.mapNotNull { b -> b.id?.let { id -> id to b } }.toMap()
            }
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
                title = { Text("New Loan Requests (${pendingLoans.size})") },
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
                    val borrower = borrowersMap[loan.borrowerId]
                    val borrowerName = borrower?.name ?: "Borrower"
                    val borrowerMobile = borrower?.mobileNumber ?: ""

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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = borrowerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (borrowerMobile.isNotBlank()) {
                                        Text(
                                            text = "+91 $borrowerMobile",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryLight
                                        )
                                    }
                                }

                                if (borrowerMobile.isNotBlank()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = {
                                                CommunicationUtils.openWhatsAppChat(
                                                    context = context,
                                                    rawMobileNumber = borrowerMobile,
                                                    message = "Hello $borrowerName, regarding your loan request of ₹${loan.principalAmount} on DailyPay."
                                                )
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, WhatsAppGreen.copy(alpha = 0.3f), CircleShape)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                                contentDescription = "WhatsApp",
                                                tint = WhatsAppGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { CommunicationUtils.openPhoneDialer(context, borrowerMobile) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, CallBlue.copy(alpha = 0.3f), CircleShape)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_call),
                                                contentDescription = "Call",
                                                tint = CallBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderSubtleLight)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Requested Principal", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                    Text("₹${loan.principalAmount}", style = MaterialTheme.typography.titleLarge, color = BrandPrimary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Tenure", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                    Surface(shape = RoundedCornerShape(8.dp), color = AlertOrangeSubtle) {
                                        Text(
                                            text = "${loan.tenureDays} Days",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AlertOrange
                                        )
                                    }
                                }
                            }

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
                                Text("Review, Adjust Interest & Disburse")
                            }
                        }
                    }
                }
            }
        }

        selectedLoanToApprove?.let { loan ->
            val borrower = borrowersMap[loan.borrowerId]
            val borrowerName = borrower?.name ?: "Borrower"
            val borrowerMobile = borrower?.mobileNumber ?: ""

            AlertDialog(
                onDismissRequest = { if (!isApproving) selectedLoanToApprove = null },
                title = { Text("Approve & Disburse Loan") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "$borrowerName • ₹${loan.principalAmount} • ${loan.tenureDays} Days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )

                        OutlinedTextField(
                            value = customInterestRate,
                            onValueChange = { customInterestRate = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Set Interest Rate (%/Month)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Disbursement Mode:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedDisbursementMode == PaymentMode.CASH,
                                onClick = { selectedDisbursementMode = PaymentMode.CASH },
                                label = { Text("Cash") }
                            )
                            FilterChip(
                                selected = selectedDisbursementMode == PaymentMode.UPI,
                                onClick = { selectedDisbursementMode = PaymentMode.UPI },
                                label = { Text("UPI Intent") }
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
                                    payeeUpiId = "$borrowerMobile@upi",
                                    payeeName = borrowerName,
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
                                    Toast.makeText(context, "Loan approved & active!", Toast.LENGTH_SHORT).show()
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
                        Text(if (isApproving) "Processing..." else "Confirm & Disburse")
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
