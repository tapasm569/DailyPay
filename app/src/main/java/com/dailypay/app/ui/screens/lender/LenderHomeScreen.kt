package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.components.DailyPayTopBar
import com.dailypay.app.ui.components.DueBorrowerCard
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.UpiIntentLauncher
import kotlinx.coroutines.launch

@Composable
fun LenderHomeScreen(
    lenderId: String,
    onNavigateToDashboard: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var allDuesList by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedDueItem by remember { mutableStateOf<DailyDueItem?>(null) }
    var collectAmount by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var isSubmittingPayment by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            lenderRepo.getDailyDues(lenderId)
                .onSuccess {
                    allDuesList = it
                    isLoading = false
                }
                .onFailure { isLoading = false }
        }
    }

    LaunchedEffect(lenderId) {
        loadData()
    }

    // Top metrics reflect overall totals
    val totalPendingDue = allDuesList.sumOf { maxOf(0.0, it.todayDueBalance - it.todayPaidAmount) }
    val totalReceivedToday = allDuesList.sumOf { it.todayPaidAmount }

    // FILTER: Only include customers who have NOT cleared today's due
    val pendingBorrowersOnly = allDuesList.filter { item ->
        val hasPendingDue = item.todayPaidAmount < item.todayDueBalance && item.todayDueBalance > 0
        val hasRemainingLoan = item.remainingBalance > 0
        hasPendingDue && hasRemainingLoan
    }

    // Apply search filter on the pending list
    val filteredList = if (searchQuery.isBlank()) {
        pendingBorrowersOnly
    } else {
        pendingBorrowersOnly.filter {
            it.borrowerName.contains(searchQuery, ignoreCase = true) ||
            it.borrowerMobile.contains(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            Column {
                DailyPayTopBar(
                    businessName = "DailyPay Lender",
                    pendingDue = totalPendingDue,
                    receivedPayment = totalReceivedToday,
                    onSearchClick = { isSearchActive = !isSearchActive },
                    onAccountClick = onNavigateToDashboard,
                    onEditProfileClick = {
                        Toast.makeText(context, "Edit profile from Dashboard", Toast.LENGTH_SHORT).show()
                    },
                    onResetPasswordClick = {
                        Toast.makeText(context, "Reset request logged", Toast.LENGTH_SHORT).show()
                    },
                    onLogoutClick = onLogoutClick
                )

                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by pending customer name or mobile...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search")
                            }
                        },
                        singleLine = true
                    )
                }
            }
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
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pending Dues Today",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (filteredList.isEmpty()) MoneyGreenSubtle else AlertOrangeSubtle
                        ) {
                            Text(
                                text = "${filteredList.size} Pending",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (filteredList.isEmpty()) MoneyGreen else AlertOrange
                            )
                        }
                    }
                }

                if (filteredList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
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
                                    text = "All Dues Collected!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MoneyGreen
                                )
                                Text(
                                    text = "No pending payments remaining for today.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.loanId }) { item ->
                        DueBorrowerCard(
                            item = item,
                            onCollectPaymentClick = {
                                selectedDueItem = it
                                // Pre-fill with the exact remaining balance for today
                                val remainingDueToday = maxOf(0.0, it.todayDueBalance - it.todayPaidAmount)
                                collectAmount = if (remainingDueToday > 0.0) remainingDueToday.toString() else it.dailyInstallment.toString()
                            }
                        )
                    }
                }
            }
        }

        // Collect Payment Dialog
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
                                    notes = "Daily payment"
                                )

                                lenderRepo.recordRepayment(repayment)
                                    .onSuccess {
                                        isSubmittingPayment = false
                                        selectedDueItem = null
                                        Toast.makeText(context, "Payment recorded! Cleared from list.", Toast.LENGTH_SHORT).show()
                                        loadData() // Refreshes list; customer is now filtered out immediately
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
