package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Pending Dues, 1 = Paid Today
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

    // Top metrics
    val totalPendingDue = allDuesList.sumOf { maxOf(0.0, it.todayDueBalance - it.todayPaidAmount) }
    val totalReceivedToday = allDuesList.sumOf { it.todayPaidAmount }

    // 1. Pending List: Only borrowers who have not cleared today's installment
    val pendingBorrowers = allDuesList.filter { item ->
        item.todayPaidAmount < item.todayDueBalance && item.todayDueBalance > 0 && item.remainingBalance > 0
    }

    // 2. Paid List: Borrowers who made payments today
    val paidBorrowers = allDuesList.filter { item ->
        item.todayPaidAmount > 0
    }

    // Filter by search query based on active tab
    val currentDisplayList = if (selectedTab == 0) {
        if (searchQuery.isBlank()) pendingBorrowers else pendingBorrowers.filter {
            it.borrowerName.contains(searchQuery, ignoreCase = true) || it.borrowerMobile.contains(searchQuery)
        }
    } else {
        if (searchQuery.isBlank()) paidBorrowers else paidBorrowers.filter {
            it.borrowerName.contains(searchQuery, ignoreCase = true) || it.borrowerMobile.contains(searchQuery)
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
                        Toast.makeText(context, "Password reset request submitted", Toast.LENGTH_SHORT).show()
                    },
                    onLogoutClick = onLogoutClick
                )

                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search customer name or mobile...") },
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

                // Segmented Tab Selector (Pending Due vs Paid Today)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        // Pending Dues Tab
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

                        // Paid Today Tab
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
                // TAB 0: PENDING DUES
                if (selectedTab == 0) {
                    if (currentDisplayList.isEmpty()) {
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
                        items(currentDisplayList, key = { it.loanId }) { item ->
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

                // TAB 1: PAID TODAY SEPARATE LIST
                else {
                    if (currentDisplayList.isEmpty()) {
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
                        items(currentDisplayList, key = { it.loanId }) { item ->
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

                                        // WhatsApp & Dial shortcuts
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
                                                onClick = {
                                                    CommunicationUtils.openPhoneDialer(context, item.borrowerMobile)
                                                },
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

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = BorderSubtleLight)
                                    Spacer(modifier = Modifier.height(12.dp))

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
                                    