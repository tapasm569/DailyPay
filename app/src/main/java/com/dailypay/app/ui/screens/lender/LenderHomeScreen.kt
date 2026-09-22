@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils
import com.dailypay.app.util.DateUtils
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

    var lenderProfile by remember { mutableStateOf<Lender?>(null) }
    var duesList by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Repayment dialog states
    var selectedItemForPayment by remember { mutableStateOf<DailyDueItem?>(null) }
    var paymentAmountText by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var isSubmittingPayment by remember { mutableStateOf(false) }

    // Multi-version compatible PagerState
    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    fun loadData() {
        scope.launch {
            lenderRepo.getLenderProfile(lenderId).onSuccess { lenderProfile = it }
            lenderRepo.getDailyDues(lenderId)
                .onSuccess {
                    duesList = it
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

    val filteredList = duesList.filter { item ->
        item.borrowerName.contains(searchQuery, ignoreCase = true) ||
                item.borrowerMobile.contains(searchQuery)
    }

    val pendingDues = filteredList.filter { it.todayDueBalance > 0.0 }
    val paidDues = filteredList.filter { it.todayPaidAmount > 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = lenderProfile?.businessName?.ifBlank { lenderProfile?.name } ?: "DailyPay Lender",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Proprietor: ${lenderProfile?.ownerName?.ifBlank { lenderProfile?.name } ?: "Lender"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = BrandPrimary)
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = DangerRed)
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
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Client Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search client name or mobile...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // TabRow synchronized with Pager
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = BrandPrimary
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Pending",
                                    fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (pagerState.currentPage == 0) BrandPrimary else TextSecondaryLight
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (pagerState.currentPage == 0) AlertOrange else AlertOrange.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${pendingDues.size}",
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (pagerState.currentPage == 0) Color.White else AlertOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    )

                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Paid Today",
                                    fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (pagerState.currentPage == 1) BrandPrimary else TextSecondaryLight
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (pagerState.currentPage == 1) MoneyGreen else MoneyGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${paidDues.size}",
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (pagerState.currentPage == 1) Color.White else MoneyGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    )
                }

                // Horizontal Swipeable Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> PendingListTab(
                            pendingList = pendingDues,
                            onCollectClick = { item ->
                                selectedItemForPayment = item
                                paymentAmountText = item.todayDueBalance.toString()
                                selectedPaymentMode = PaymentMode.CASH
                            }
                        )
                        1 -> PaidTodayListTab(
                            paidList = paidDues
                        )
                    }
                }
            }
        }

        // Collection Payment Dialog
        selectedItemForPayment?.let { item ->
            AlertDialog(
                onDismissRequest = { if (!isSubmittingPayment) selectedItemForPayment = null },
                title = { Text("Collect EMI Payment") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Borrower: ${item.borrowerName}")
                        Text(
                            text = "Today's Due: ₹${maxOf(0.0, item.todayDueBalance)}",
                            fontWeight = FontWeight.Bold,
                            color = AlertOrange
                        )

                        OutlinedTextField(
                            value = paymentAmountText,
                            onValueChange = { paymentAmountText = it },
                            label = { Text("Amount Collected (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Payment Method:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedPaymentMode == PaymentMode.CASH,
                                onClick = { selectedPaymentMode = PaymentMode.CASH },
                                label = { Text("Cash") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedPaymentMode == PaymentMode.UPI,
                                onClick = { selectedPaymentMode = PaymentMode.UPI },
                                label = { Text("UPI") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = paymentAmountText.toDoubleOrNull() ?: 0.0
                            if (amount <= 0.0) {
                                Toast.makeText(context, "Enter a valid payment amount.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSubmittingPayment = true
                            scope.launch {
                                val repayment = Repayment(
                                    loanId = item.loanId,
                                    borrowerId = item.borrowerId,
                                    lenderId = lenderId,
                                    amountPaid = amount,
                                    paymentDate = DateUtils.getTodaySqlFormat(),
                                    paymentMode = selectedPaymentMode
                                )
                                lenderRepo.recordRepayment(repayment)
                                    .onSuccess {
                                        isSubmittingPayment = false
                                        selectedItemForPayment = null
                                        Toast.makeText(context, "Payment recorded successfully!", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure {
                                        isSubmittingPayment = false
                                        Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
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
                    TextButton(
                        onClick = { selectedItemForPayment = null },
                        enabled = !isSubmittingPayment
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ----------------- TAB 0: PENDING DUES -----------------
@Composable
private fun PendingListTab(
    pendingList: List<DailyDueItem>,
    onCollectClick: (DailyDueItem) -> Unit
) {
    val context = LocalContext.current

    if (pendingList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
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
                    text = "No pending dues for today!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "All scheduled collections are cleared.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pendingList, key = { it.loanId }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = AlertOrange.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = AlertOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.borrowerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
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
                                            message = "Hello ${item.borrowerName}, your daily installment of ₹${item.todayDueBalance} is due today."
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
                        HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Today Due", style = MaterialTheme.typography.labelSmall, color = AlertOrange)
                                Text(
                                    text = "₹${maxOf(0.0, item.todayDueBalance)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertOrange
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Balance Left", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                Text(
                                    text = "₹${maxOf(0.0, item.remainingBalance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { onCollectClick(item) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Collect")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- TAB 1: PAID TODAY -----------------
@Composable
private fun PaidTodayListTab(
    paidList: List<DailyDueItem>
) {
    val context = LocalContext.current

    if (paidList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = TextSecondaryLight,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No payments cleared today yet.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Approved payments will show here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(paidList, key = { it.loanId }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = MoneyGreen.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MoneyGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.borrowerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
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
                                            message = "Thank you ${item.borrowerName}, your payment of ₹${item.todayPaidAmount} has been received and verified."
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
                        HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Paid Today", style = MaterialTheme.typography.labelSmall, color = MoneyGreen)
                                Text(
                                    text = "₹${item.todayPaidAmount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MoneyGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Recovered", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                Text(
                                    text = "₹${item.totalPaid}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining Bal", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                Text(
                                    text = "₹${maxOf(0.0, item.remainingBalance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.remainingBalance > 0.0) DangerRed else MoneyGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}