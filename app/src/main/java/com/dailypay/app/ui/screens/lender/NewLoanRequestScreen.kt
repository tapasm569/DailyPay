package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.round

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

    var selectedLoanToApprove by remember { mutableStateOf<Loan?>(null) }
    var interestRateText by remember { mutableStateOf("2.0") }
    var disbursementMode by remember { mutableStateOf(PaymentMode.CASH) }
    var disbursementRef by remember { mutableStateOf("Disbursed") }
    var isApproving by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            lenderRepo.getBorrowers(lenderId).onSuccess { list ->
                borrowersMap = list.associateBy { it.id ?: "" }
            }
            lenderRepo.getPendingLoanRequests(lenderId)
                .onSuccess {
                    pendingLoans = it
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
                title = { Text("Pending Loan Requests (${pendingLoans.size})") },
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
        } else if (pendingLoans.isEmpty()) {
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
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No pending loan applications.",
                        style = MaterialTheme.typography.titleMedium,
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
                itemsIndexed(pendingLoans, key = { index, item -> item.id ?: "pending_$index" }) { _, loan ->
                    val borrower = borrowersMap[loan.borrowerId]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = borrower?.name ?: "Customer",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Mobile: +91 ${borrower?.mobileNumber ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderSubtleLight)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Requested Principal", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text("₹${loan.principalAmount}", style = MaterialTheme.typography.titleMedium, color = BrandPrimary)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Requested Tenure", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text("${loan.tenureDays} Days", style = MaterialTheme.typography.titleMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    selectedLoanToApprove = loan
                                    interestRateText = "2.0"
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                            ) {
                                Text("Set Interest & Approve Loan")
                            }
                        }
                    }
                }
            }
        }

        selectedLoanToApprove?.let { loan ->
            val principal = loan.principalAmount
            val tenure = if (loan.tenureDays > 0) loan.tenureDays else 30
            val interestRate = interestRateText.toDoubleOrNull() ?: 0.0
            val totalInterest = round((principal * (interestRate / 100.0) * (tenure / 30.0)) * 100.0) / 100.0
            val totalPayable = principal + totalInterest
            val dailyEmi = round((totalPayable / tenure) * 100.0) / 100.0

            AlertDialog(
                onDismissRequest = { if (!isApproving) selectedLoanToApprove = null },
                title = { Text("Approve & Disburse Loan") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = interestRateText,
                            onValueChange = { interestRateText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Monthly Interest Rate (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Principal: ₹$principal", style = MaterialTheme.typography.bodySmall)
                                Text("Total Interest: ₹$totalInterest", style = MaterialTheme.typography.bodySmall)
                                Text("Total Payable: ₹$totalPayable", style = MaterialTheme.typography.titleSmall)
                                Text("Daily EMI: ₹$dailyEmi / day ($tenure Days)", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = disbursementMode == PaymentMode.CASH,
                                onClick = { disbursementMode = PaymentMode.CASH },
                                label = { Text("Cash") }
                            )
                            FilterChip(
                                selected = disbursementMode == PaymentMode.UPI,
                                onClick = { disbursementMode = PaymentMode.UPI },
                                label = { Text("UPI") }
                            )
                        }

                        OutlinedTextField(
                            value = disbursementRef,
                            onValueChange = { disbursementRef = it },
                            label = { Text("Disbursement Reference / Note") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val loanId = loan.id ?: return@Button
                            isApproving = true
                            scope.launch {
                                lenderRepo.approveAndDisburseLoan(
                                    loanId = loanId,
                                    interestRate = interestRate,
                                    disbursementMode = disbursementMode,
                                    disbursementRef = disbursementRef
                                ).onSuccess {
                                    isApproving = false
                                    selectedLoanToApprove = null
                                    Toast.makeText(context, "Loan approved! Daily EMI schedule has started.", Toast.LENGTH_SHORT).show()
                                    loadData()
                                }.onFailure {
                                    isApproving = false
                                    Toast.makeText(context, "Approval failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isApproving,
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                    ) {
                        Text(if (isApproving) "Processing..." else "Confirm & Disburse")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { selectedLoanToApprove = null },
                        enabled = !isApproving
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
