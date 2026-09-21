package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.dailypay.app.R
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.components.DailyPayTopBar
import com.dailypay.app.ui.components.DueBorrowerCard
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils
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
    var allBorrowersList by remember { mutableStateOf<List<Borrower>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Pending Dues, 1 = Paid Today
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Payment Collection Dialog State
    var selectedDueItem by remember { mutableStateOf<DailyDueItem?>(null) }
    var collectAmount by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var isSubmittingPayment by remember { mutableStateOf(false) }

    // Profile Inspection, Update, and Delete States
    var selectedBorrowerForDetails by remember { mutableStateOf<Borrower?>(null) }
    var borrowerToEdit by remember { mutableStateOf<Borrower?>(null) }
    var borrowerToDelete by remember { mutableStateOf<Borrower?>(null) }
    var isProcessingAction by remember { mutableStateOf(false) }

    // Repayment History Dialog States
    var inspectingRepaymentLoanId by remember { mutableStateOf<String?>(null) }
    var inspectingRepaymentBorrowerName by remember { mutableStateOf("") }
    var repaymentHistoryList by remember { mutableStateOf<List<Repayment>>(emptyList()) }
    var isLoadingRepayments by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            lenderRepo.getBorrowers(lenderId).onSuccess {
                allBorrowersList = it
            }
            lenderRepo.getDailyDues(lenderId)
                .onSuccess {
                    allDuesList = it
                    isLoading = false
                }
                .onFailure { isLoading = false }
        }
    }

    // Auto-refresh when returning back to this screen
    LifecycleResumeEffect(lenderId) {
        loadData()
        onPauseOrDispose { }
    }

    // Top metrics with value clamping
    val totalPendingDue = allDuesList.sumOf { maxOf(0.0, it.todayDueBalance) }
    val totalReceivedToday = allDuesList.sumOf { it.todayPaidAmount }

    // Tab lists
    val pendingBorrowers = allDuesList.filter { item ->
        item.todayDueBalance > 0.0 && item.remainingBalance > 0.0
    }
    val paidBorrowers = allDuesList.filter { item ->
        item.todayPaidAmount > 0.0
    }

    // Search Filter
    val isSearching = isSearchActive && searchQuery.isNotBlank()
    val searchResults = if (isSearching) {
        allBorrowersList.filter { b ->
            b.name.contains(searchQuery.trim(), ignoreCase = true) ||
            b.mobileNumber.contains(searchQuery.trim())
        }
    } else {
        emptyList()
    }

    Scaffold(
        topBar = {
            Column {
                DailyPayTopBar(
                    businessName = "DailyPay Lender",
                    pendingDue = totalPendingDue,
                    receivedPayment = totalReceivedToday,
                    onSearchClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    },
                    onAccountClick = onNavigateToDashboard,
                    onEditProfileClick = {
                        Toast.makeText(context, "Edit profile from Dashboard", Toast.LENGTH_SHORT).show()
                    },
                    onResetPasswordClick = {
                        Toast.makeText(context, "Password reset request submitted", Toast.LENGTH_SHORT).show()
                    },
                    onLogoutClick = onLogoutClick
                )

                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by borrower name or mobile...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = BrandPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                if (!isSearching) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtleLight, RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedTab == 0) AlertOrange else Color.Transparent,
                                onClick = { selectedTab = 0 }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PendingActions,
                                        contentDescription = null,
                                        tint = if (selectedTab == 0) Color.White else TextSecondaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pending (${pendingBorrowers.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selectedTab == 0) Color.White else TextSecondaryLight
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedTab == 1) MoneyGreen else Color.Transparent,
                                onClick = { selectedTab = 1 }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = if (selectedTab == 1) Color.White else TextSecondaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Paid Today (${paidBorrowers.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selectedTab == 1) Color.White else TextSecondaryLight
                                    )
                                }
                            }
                        }
                    }
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isSearching) {
                    item {
                        Text(
                            text = "Search Results (${searchResults.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (searchResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No borrower found matching \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    } else {
                        items(searchResults, key = { it.id ?: it.mobileNumber }) { borrower ->
                            // Prioritize active loan with balance over closed zero-balance loans
                            val dueItem = allDuesList.firstOrNull { it.borrowerId == borrower.id && it.remainingBalance > 0.0 }
                                ?: allDuesList.firstOrNull { it.borrowerId == borrower.id }

                            val approvedLoan = dueItem?.totalPayable ?: 0.0
                            val todayDue = maxOf(0.0, dueItem?.todayDueBalance ?: 0.0)
                            val paidBalance = dueItem?.totalPaid ?: 0.0
                            val remainingBalance = maxOf(0.0, dueItem?.remainingBalance ?: 0.0)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = borrower.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "+91 ${borrower.mobileNumber}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(
                                                onClick = {
                                                    CommunicationUtils.openWhatsAppChat(
                                                        context = context,
                                                        rawMobileNumber = borrower.mobileNumber,
                                                        message = "Hello ${borrower.name}, contacting you regarding your DailyPay loan."
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
                                                onClick = { CommunicationUtils.openPhoneDialer(context, borrower.mobileNumber) },
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

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderSubtleLight)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Approved Loan", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                            Text("₹$approvedLoan", style = MaterialTheme.typography.titleMedium)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Today's Due", style = MaterialTheme.typography.labelSmall, color = AlertOrange)
                                            Text("₹$todayDue", style = MaterialTheme.typography.titleMedium, color = AlertOrange)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Paid Balance", style = MaterialTheme.typography.labelSmall, color = MoneyGreen)
                                            Text("₹$paidBalance", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Remaining Balance", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                                            Text("₹$remainingBalance", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (dueItem != null) {
                                                    selectedDueItem = dueItem
                                                    collectAmount = if (dueItem.todayDueBalance > 0.0) dueItem.todayDueBalance.toString() else dueItem.dailyInstallment.toString()
                                                } else {
                                                    Toast.makeText(context, "No active loan found for this borrower.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Collect")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (dueItem != null) {
                                                    inspectingRepaymentLoanId = dueItem.loanId
                                                    inspectingRepaymentBorrowerName = borrower.name
                                                    isLoadingRepayments = true
                                                    scope.launch {
                                                        lenderRepo.getRepaymentsForLoan(dueItem.loanId)
                                                            .onSuccess {
                                                                repaymentHistoryList = it
                                                                isLoadingRepayments = false
                                                            }
                                                            .onFailure {
                                                                isLoadingRepayments = false
                                                                Toast.makeText(context, "Could not load receipts: ${it.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "No active loan found.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Receipts")
                                        }

                                        OutlinedButton(
                                            onClick = { selectedBorrowerForDetails = borrower },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Profile")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 0) {
                    if (pendingBorrowers.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
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
                                        modifier = Modifier.size(46.dp)
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
                        items(pendingBorrowers, key = { it.loanId }) { item ->
                            DueBorrowerCard(
                                item = item,
                                onCollectPaymentClick = {
                                    selectedDueItem = it
                                    collectAmount = if (it.todayDueBalance > 0.0) it.todayDueBalance.toString() else it.dailyInstallment.toString()
                                }
                            )
                        }
                    }
                } else {
                    if (paidBorrowers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No payments received yet today.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    } else {
                        items(paidBorrowers, key = { it.loanId }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MoneyGreen.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.borrowerName,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "+91 ${item.borrowerMobile}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(
                                                onClick = {
                                                    CommunicationUtils.openWhatsAppChat(
                                                        context = context,
                                                        rawMobileNumber = item.borrowerMobile,
                                                        message = "Thank you ${item.borrowerName}! We received your payment of ₹${item.todayPaidAmount} for today. Remaining balance: ₹${item.remainingBalance}."
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
                                                onClick = { CommunicationUtils.openPhoneDialer(context, item.borrowerMobile) },
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

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderSubtleLight)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Paid Today", style = MaterialTheme.typography.labelMedium, color = MoneyGreen)
                                            Text(
                                                text = "₹${item.todayPaidAmount}",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MoneyGreen
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Daily Installment", style = MaterialTheme.typography.labelMedium, color = TextSecondaryLight)
                                            Text("₹${item.dailyInstallment}", style = MaterialTheme.typography.titleMedium)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Remaining Balance", style = MaterialTheme.typography.labelMedium, color = TextSecondaryLight)
                                            Text("₹${maxOf(0.0, item.remainingBalance)}", style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Collect Payment Dialog
        selectedDueItem?.let { dueItem ->
            AlertDialog(
                onDismissRequest = { if (!isSubmittingPayment) selectedDueItem = null },
                title = { Text("Collect Due - ${dueItem.borrowerName}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Remaining Due Today: ₹${maxOf(0.0, dueItem.todayDueBalance)} | Total Loan Left: ₹${maxOf(0.0, dueItem.remainingBalance)}",
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
                                    notes = "Daily collection"
                                )

                                lenderRepo.recordRepayment(repayment)
                                    .onSuccess {
                                        isSubmittingPayment = false
                                        selectedDueItem = null
                                        Toast.makeText(context, "Payment recorded successfully!", Toast.LENGTH_SHORT).show()
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

        // Repayment Details Dialog for Search Results
        inspectingRepaymentLoanId?.let {
            AlertDialog(
                onDismissRequest = { inspectingRepaymentLoanId = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payment Receipts", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { inspectingRepaymentLoanId = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Borrower: $inspectingRepaymentBorrowerName",
                            style = MaterialTheme.typography.titleSmall,
                            color = BrandPrimary
                        )

                        if (isLoadingRepayments) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = BrandPrimary)
                            }
                        } else if (repaymentHistoryList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No repayments recorded for this loan yet.", color = TextSecondaryLight)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(repaymentHistoryList, key = { it.id ?: "${it.paymentDate}_${it.amountPaid}" }) { r ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = CardDefaults.outlinedCardBorder()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("₹${r.amountPaid}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                                                Text(r.paymentDate, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                            }
                                            SuggestionChip(onClick = {}, label = { Text(r.paymentMode.name) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { inspectingRepaymentLoanId = null },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        // View Borrower Profile Dialog
        selectedBorrowerForDetails?.let { borrower ->
            AlertDialog(
                onDismissRequest = { selectedBorrowerForDetails = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Borrower Profile", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { selectedBorrowerForDetails = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = BrandPrimary.copy(alpha = 0.08f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = borrower.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = BrandPrimary
                                )
                                Text(
                                    text = "Mobile: +91 ${borrower.mobileNumber}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtleLight, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Village / City", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                                    Text(borrower.villageCity ?: "-", style = MaterialTheme.typography.bodyMedium)
                                }
                                HorizontalDivider(color = BorderSubtleLight)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Post Office", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                                    Text(borrower.postOffice ?: "-", style = MaterialTheme.typography.bodyMedium)
                                }
                                HorizontalDivider(color = BorderSubtleLight)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Police Station", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                                    Text(borrower.policeStation ?: "-", style = MaterialTheme.typography.bodyMedium)
                                }
                                HorizontalDivider(color = BorderSubtleLight)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("District", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                                    Text(borrower.dist ?: "-", style = MaterialTheme.typography.bodyMedium)
                                }
                                HorizontalDivider(color = BorderSubtleLight)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Password", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                                    Text(borrower.passwordHash, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Text("KYC Documents Attached:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(if (!borrower.aadhaarCardUrl.isNullOrBlank()) "Aadhaar ✓" else "No Aadhaar") }
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(if (!borrower.panCardUrl.isNullOrBlank()) "PAN ✓" else "No PAN") }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    borrowerToEdit = borrower
                                    selectedBorrowerForDetails = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Update")
                            }

                            Button(
                                onClick = {
                                    borrowerToDelete = borrower
                                    selectedBorrowerForDetails = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete")
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Update Borrower Profile Dialog
        borrowerToEdit?.let { borrower ->
            var editName by remember { mutableStateOf(borrower.name) }
            var editMobile by remember { mutableStateOf(borrower.mobileNumber) }
            var editVillage by remember { mutableStateOf(borrower.villageCity ?: "") }
            var editPostOffice by remember { mutableStateOf(borrower.postOffice ?: "") }
            var editPoliceStation by remember { mutableStateOf(borrower.policeStation ?: "") }
            var editDist by remember { mutableStateOf(borrower.dist ?: "") }
            var editPassword by remember { mutableStateOf(borrower.passwordHash) }

            AlertDialog(
                onDismissRequest = { if (!isProcessingAction) borrowerToEdit = null },
                title = { Text("Update Borrower Profile") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = editMobile,
                            onValueChange = { if (it.length <= 10) editMobile = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Mobile Number *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editVillage,
                                onValueChange = { editVillage = it },
                                label = { Text("Village/City") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = editPostOffice,
                                onValueChange = { editPostOffice = it },
                                label = { Text("Post Office") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editPoliceStation,
                                onValueChange = { editPoliceStation = it },
                                label = { Text("Police Station") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = editDist,
                                onValueChange = { editDist = it },
                                label = { Text("District") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("Password *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editName.isBlank() || editMobile.length != 10 || editPassword.isBlank()) {
                                Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isProcessingAction = true
                            scope.launch {
                                val updated = borrower.copy(
                                    name = editName.trim(),
                                    mobileNumber = editMobile.trim(),
                                    villageCity = editVillage.trim(),
                                    postOffice = editPostOffice.trim(),
                                    policeStation = editPoliceStation.trim(),
                                    dist = editDist.trim(),
                                    passwordHash = editPassword.trim()
                                )

                                lenderRepo.updateBorrower(updated)
                                    .onSuccess {
                                        isProcessingAction = false
                                        borrowerToEdit = null
                                        Toast.makeText(context, "Borrower profile updated successfully!", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure {
                                        isProcessingAction = false
                                        Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isProcessingAction,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isProcessingAction) "Saving..." else "Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { borrowerToEdit = null },
                        enabled = !isProcessingAction
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Borrower Confirmation Dialog
        borrowerToDelete?.let { borrower ->
            AlertDialog(
                onDismissRequest = { if (!isProcessingAction) borrowerToDelete = null },
                title = { Text("Delete Borrower?") },
                text = {
                    Text(
                        text = "Are you sure you want to permanently delete \"${borrower.name}\" (+91 ${borrower.mobileNumber})? Active loan accounts must be settled or removed before deleting.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val borrowerId = borrower.id
                            if (borrowerId.isNullOrBlank()) return@Button

                            isProcessingAction = true
                            scope.launch {
                                lenderRepo.deleteBorrower(borrowerId)
                                    .onSuccess {
                                        isProcessingAction = false
                                        borrowerToDelete = null
                                        Toast.makeText(context, "Borrower deleted successfully.", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure { error ->
                                        isProcessingAction = false
                                        val errorMsg = if (error.message?.contains("violates foreign key constraint", ignoreCase = true) == true) {
                                            "Cannot delete borrower with active loans or transactions. Settle those loans first."
                                        } else {
                                            error.message ?: "Deletion failed"
                                        }
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    }
                            }
                        },
                        enabled = !isProcessingAction,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text(if (isProcessingAction) "Deleting..." else "Confirm Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { borrowerToDelete = null },
                        enabled = !isProcessingAction
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}