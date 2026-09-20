package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.components.DueBorrowerCard
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.UpiIntentLauncher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysDueScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var duesList by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedDueItem by remember { mutableStateOf<DailyDueItem?>(null) }
    var collectAmount by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var isSubmittingPayment by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            lenderRepo.getDailyDues(lenderId)
                .onSuccess { allDues ->
                    // Show only borrowers with unpaid dues for today
                    duesList = allDues.filter { item ->
                        item.todayPaidAmount < item.todayDueBalance && item.todayDueBalance > 0 && item.remainingBalance > 0
                    }
                    isLoading = false
                }
                .onFailure { isLoading = false }
        }
    }

    LaunchedEffect(lenderId) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Due (${duesList.size} Pending)") },
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
        } else if (duesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MoneyGreenSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MoneyGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "All Dues Cleared!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MoneyGreen
                        )
                        Text(
                            text = "No pending dues remaining for today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
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
                items(duesList, key = { it.loanId }) { item ->
                    DueBorrowerCard(
                        item = item,
                        onCollectPaymentClick = {
                            selectedDueItem = it
                            val remainingDueToday = maxOf(0.0, it.todayDueBalance - it.todayPaidAmount)
                            collectAmount = if (remainingDueToday > 0.0) remainingDueToday.toString() else it.dailyInstallment.toString()
                        }
                    )
                }
            }
        }

        selectedDueItem?.let { dueItem ->
            val pendingAmountToday = maxOf(0.0, dueItem.todayDueBalance - dueItem.todayPaidAmount)

            AlertDialog(
                onDismissRequest = { if (!isSubmittingPayment) selectedDueItem = null },
                title = { Text("Collect Due - ${dueItem.borrowerName}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Remaining Due Today: ₹$pendingAmountToday | Total Left: ₹${dueItem.remainingBalance}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )

                        OutlinedTextField(
                            value = collectAmount,
                            onValueChange = { collectAmount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Amount Received (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedPaymentMode == PaymentMode.CASH,
                                onClick = { selectedPaymentMode = PaymentMode.CASH },
                                label = { Text("Cash") }
                            )
                            FilterChip(
                                selected = selectedPaymentMode == PaymentMode.UPI,
                                onClick = { selectedPaymentMode = PaymentMode.UPI },
                                label = { Text("UPI Intent") }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsedAmount = collectAmount.toDoubleOrNull() ?: 0.0
                            if (parsedAmount <= 0.0) {
                                Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (selectedPaymentMode == PaymentMode.UPI) {
                                UpiIntentLauncher.initiateUpiPayment(
                                    context = context,
                                    payeeUpiId = "${dueItem.borrowerMobile}@upi",
                                    payeeName = dueItem.borrowerName,
                                    amount = parsedAmount,
                                    transactionNote = "Repayment-${dueItem.borrowerName}"
                                )
                            }

                            isSubmittingPayment = true
                            scope.launch {
                                val repayment = Repayment(
                                    loanId = dueItem.loanId,
                                    borrowerId = dueItem.borrowerId,
                                    lenderId = lenderId,
                                    paymentDate = DateUtils.getTodaySqlFormat(),
                                    amountPaid = parsedAmount,
                                    paymentMode = selectedPaymentMode,
                                    notes = "Collected from Today Due screen"
                                )

                                lenderRepo.recordRepayment(repayment)
                                    .onSuccess {
                                        isSubmittingPayment = false
                                        selectedDueItem = null
                                        Toast.makeText(context, "Payment recorded! Cleared from list.", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure {
                                        isSubmittingPayment = false
                                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isSubmittingPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isSubmittingPayment) "Saving..." else "Confirm Payment")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedDueItem = null }, enabled = !isSubmittingPayment) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}