package com.dailypay.app.ui.screens.borrower

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.UpiIntentLauncher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerPayEmiScreen(
    borrowerId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val borrowerRepo = remember { BorrowerRepository() }

    var activeLoans by remember { mutableStateOf<List<ApprovedLoanDetail>>(emptyList()) }
    var selectedLoan by remember { mutableStateOf<ApprovedLoanDetail?>(null) }
    var lenderUpiId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var amountText by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.UPI) }
    var utrReferenceText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(borrowerId) {
        borrowerRepo.getCustomerApprovedLoans(borrowerId)
            .onSuccess { loans ->
                activeLoans = loans.filter { it.remainingBalance > 0.0 }
                val current = activeLoans.firstOrNull()
                selectedLoan = current
                if (current != null) {
                    amountText = if (current.todayDue > 0.0) current.todayDue.toString() else current.dailyInstallment.toString()
                    val profile = borrowerRepo.getBorrowerProfile(borrowerId).getOrNull()
                    val lenderId = profile?.lenderId
                    if (!lenderId.isNullOrBlank()) {
                        borrowerRepo.getLenderProfile(lenderId).onSuccess { lender ->
                            lenderUpiId = lender.upiId
                        }
                    }
                }
                isLoading = false
            }
            .onFailure {
                isLoading = false
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay EMI") },
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
        } else if (activeLoans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No active loan dues found.", style = MaterialTheme.typography.bodyLarge, color = TextSecondaryLight)
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Due Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AlertOrangeSubtle),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Today's Installment Due", style = MaterialTheme.typography.labelSmall, color = AlertOrange)
                        Text("₹${selectedLoan?.todayDue ?: 0.0}", style = MaterialTheme.typography.headlineMedium, color = AlertOrange)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Daily Rate: ₹${selectedLoan?.dailyInstallment}/day | Remaining: ₹${selectedLoan?.remainingBalance}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                }

                // Amount to pay
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Payment Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Payment Mode", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = selectedPaymentMode == PaymentMode.UPI,
                        onClick = { selectedPaymentMode = PaymentMode.UPI },
                        label = { Text("UPI (Online)") }
                    )
                    FilterChip(
                        selected = selectedPaymentMode == PaymentMode.CASH,
                        onClick = { selectedPaymentMode = PaymentMode.CASH },
                        label = { Text("Cash (Handed to Lender)") }
                    )
                }

                if (selectedPaymentMode == PaymentMode.UPI) {
                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtleLight, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Pay Directly via UPI App", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Lender UPI: ${lenderUpiId ?: "Will open default UPI dialer"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )

                            Button(
                                onClick = {
                                    val amount = amountText.toDoubleOrNull() ?: 0.0
                                    if (amount <= 0.0) {
                                        Toast.makeText(context, "Enter amount first", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    UpiIntentLauncher.initiateUpiPayment(
                                        context = context,
                                        payeeUpiId = lenderUpiId ?: "dailypay@upi",
                                        payeeName = "Lender Payment",
                                        amount = amount,
                                        transactionNote = "EMI-${selectedLoan?.borrowerName}"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                            ) {
                                Text("Open UPI App (GPay / PhonePe / Paytm)")
                            }

                            OutlinedTextField(
                                value = utrReferenceText,
                                onValueChange = { utrReferenceText = it },
                                label = { Text("Enter UPI Reference / UTR Number *") },
                                placeholder = { Text("12-digit UTR from payment receipt") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Text(
                            text = "Note: Submit after handing over cash to your lender. The lender will verify it to clear your due balance.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        val loan = selectedLoan

                        if (amount <= 0.0 || loan == null) {
                            Toast.makeText(context, "Enter a valid payment amount.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (selectedPaymentMode == PaymentMode.UPI && utrReferenceText.isBlank()) {
                            Toast.makeText(context, "Please enter the UPI Reference/UTR number.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSubmitting = true
                        scope.launch {
                            val profile = borrowerRepo.getBorrowerProfile(borrowerId).getOrNull()
                            val targetLenderId = profile?.lenderId ?: ""

                            val repayment = Repayment(
                                loanId = loan.loanId,
                                borrowerId = borrowerId,
                                lenderId = targetLenderId,
                                paymentDate = DateUtils.getTodaySqlFormat(),
                                amountPaid = amount,
                                paymentMode = selectedPaymentMode,
                                utrReference = if (selectedPaymentMode == PaymentMode.UPI) utrReferenceText.trim() else null,
                                notes = "Customer submitted payment",
                                status = com.dailypay.app.data.model.RepaymentStatus.PENDING
                            )

                            borrowerRepo.submitEmiPayment(repayment)
                                .onSuccess {
                                    isSubmitting = false
                                    Toast.makeText(context, "Payment submitted! Awaiting lender verification.", Toast.LENGTH_LONG).show()
                                    onNavigateBack()
                                }
                                .onFailure {
                                    isSubmitting = false
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                ) {
                    Text(if (isSubmitting) "Submitting Payment..." else "Submit for Lender Verification")
                }
            }
        }
    }
}