package com.dailypay.app.ui.screens.borrower

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.model.RepaymentStatus
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.DateUtils
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
    var isLoading by remember { mutableStateOf(true) }

    var amountText by remember { mutableStateOf("") }
    var cashNoteText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(borrowerId) {
        borrowerRepo.getCustomerApprovedLoans(borrowerId)
            .onSuccess { loans ->
                activeLoans = loans.filter { it.remainingBalance > 0.0 }
                val current = activeLoans.firstOrNull()
                selectedLoan = current
                if (current != null) {
                    amountText = if (current.todayDue > 0.0) current.todayDue.toString() else current.dailyInstallment.toString()
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
                title = { Text("Pay Cash EMI") },
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

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Cash Amount Handed Over (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MoneyGreen.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MoneyGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Payment Mode: Cash Handed to Lender",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MoneyGreen
                        )
                    }
                }

                OutlinedTextField(
                    value = cashNoteText,
                    onValueChange = { cashNoteText = it },
                    label = { Text("Handover Note (Optional)") },
                    placeholder = { Text("e.g. Paid cash at shop / to collection agent") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = "Note: Submit this entry after handing over physical cash to your lender. The lender will verify receipt to clear your due balance.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        val loan = selectedLoan

                        if (amount <= 0.0 || loan == null) {
                            Toast.makeText(context, "Enter a valid cash amount.", Toast.LENGTH_SHORT).show()
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
                                paymentMode = PaymentMode.CASH,
                                utrReference = null,
                                notes = cashNoteText.trim().ifBlank { "Cash handed over by customer" },
                                status = RepaymentStatus.PENDING
                            )

                            borrowerRepo.submitEmiPayment(repayment)
                                .onSuccess {
                                    isSubmitting = false
                                    Toast.makeText(context, "Cash payment submitted! Awaiting lender verification.", Toast.LENGTH_LONG).show()
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
                    Text(if (isSubmitting) "Submitting..." else "Submit Cash Handover")
                }
            }
        }
    }
}
