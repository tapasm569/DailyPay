package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.UpiIntentLauncher
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysDueScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository(context) }

    var duesList by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedDueItem by remember { mutableStateOf<DailyDueItem?>(null) }
    var collectAmount by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var isSubmittingPayment by remember { mutableStateOf(false) }

    val currentDateText = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    fun loadData() {
        scope.launch {
            lenderRepo.getDailyDues(lenderId)
                .onSuccess { list ->
                    // Filter borrowers who have an unpaid balance today and an active loan balance
                    duesList = list.filter { it.todayDueBalance > 0.0 && it.remainingBalance > 0.0 }
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

    val totalPendingToday = duesList.sumOf { it.todayDueBalance }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today's Dues (${duesList.size})")
                        Text(
                            text = "Pending: ₹$totalPendingToday • $currentDateText",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlertOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
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
        } else if (duesList.isEmpty()) {
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
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "All customer dues are cleared for today!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MoneyGreen
                    )
                    Text(
                        text = "As of $currentDateText",
                        style = MaterialTheme.typography.bodySmall,
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(duesList, key = { index, item -> item.loanId.ifBlank { "due_$index" } }) { _, item ->
                    val hasOverdue = item.todayDueBalance > item.dailyInstallment

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

                                    // Current Date Badge & Cumulative Overdue Indicator
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.background
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Event,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = TextSecondaryLight
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = currentDateText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondaryLight
                                                )
                                            }
                                        }

                                        if (hasOverdue) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = DangerRed.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "Includes Overdue",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = DangerRed,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            CommunicationUtils.openWhatsAppChat(
                                                context = context,
                                                rawMobileNumber = item.borrowerMobile,
                                                message = "Hello ${item.borrowerName}, your daily due of ₹${item.todayDueBalance} for $currentDateText is pending."
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
                                    Text("Daily EMI", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text(
                                        text = "₹${item.dailyInstallment}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
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
                                    onClick = {
                                        selectedDueItem = item
                                        collectAmount = item.todayDueBalance.toString()
                                    },
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

        // ================= COLLECT DUE DIALOG =================
        selectedDueItem?.let { dueItem ->
            AlertDialog(
                onDismissRequest = { if (!isSubmittingPayment) selectedDueItem = null },
                title = { Text("Collect Due - ${dueItem.borrowerName}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Date: $currentDateText\nDaily Due: ₹${dueItem.dailyInstallment} | Total Due for Today: ₹${dueItem.todayDueBalance}" +
                                    if (dueItem.todayDueBalance > dueItem.dailyInstallment) " (Includes Overdue)" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )

                        OutlinedTextField(
                            value = collectAmount,
                            onValueChange = { collectAmount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Amount Received (₹) *") },
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
                                    notes = "Daily installment collected on $currentDateText"
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
                        Text(if (isSubmittingPayment) "Saving..." else "Confirm Collection")
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