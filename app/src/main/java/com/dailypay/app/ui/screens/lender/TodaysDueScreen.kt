package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.dailypay.app.ui.theme.AlertOrange
import com.dailypay.app.ui.theme.BorderSubtleLight
import com.dailypay.app.ui.theme.BrandPrimary
import com.dailypay.app.ui.theme.CallBlue
import com.dailypay.app.ui.theme.DangerRed
import com.dailypay.app.ui.theme.MoneyGreen
import com.dailypay.app.ui.theme.TextSecondaryLight
import com.dailypay.app.ui.theme.WhatsAppGreen
import com.dailypay.app.util.CommunicationUtils
import com.dailypay.app.util.DateUtils
import kotlinx.coroutines.launch
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
    val lenderRepo = remember { LenderRepository(context.applicationContext) }

    var duesList by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedDueItem by remember { mutableStateOf<DailyDueItem?>(null) }
    var collectAmount by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var isSubmittingPayment by remember { mutableStateOf(false) }

    val currentDateText = remember { DateUtils.getTodayDisplayFormat() }
    val cleanLenderId = remember(lenderId) { lenderId.trim() }

    fun loadData() {
        if (cleanLenderId.isBlank() || cleanLenderId == "{lenderId}") {
            isLoading = false
            return
        }
        scope.launch {
            lenderRepo.getDailyDues(cleanLenderId)
                .onSuccess { list ->
                    duesList = list.filter { it.todayDueBalance > 0.0 && it.remainingBalance > 0.0 }
                    isLoading = false
                }
                .onFailure { error ->
                    isLoading = false
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(cleanLenderId) {
        loadData()
    }

    val totalPendingToday = remember(duesList) {
        duesList.sumOf { it.todayDueBalance }
    }
    val formattedTotalPending = remember(totalPendingToday) {
        String.format(Locale.US, "%.2f", totalPendingToday)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today's Dues (${duesList.size})")
                        Text(
                            text = "Pending: ₹$formattedTotalPending • $currentDateText",
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
                itemsIndexed(
                    items = duesList,
                    key = { index, item -> "${item.loanId}_${item.borrowerId}_$index" }
                ) { _, item ->
                    val hasOverdue = item.todayDueBalance > item.dailyInstallment
                    val borrowerName = if (item.borrowerName.isNotBlank()) item.borrowerName.trim() else "Borrower"
                    val formattedTodayDue = remember(item.todayDueBalance) {
                        String.format(Locale.US, "%.2f", maxOf(0.0, item.todayDueBalance))
                    }
                    val formattedInstallment = remember(item.dailyInstallment) {
                        String.format(Locale.US, "%.2f", item.dailyInstallment)
                    }
                    val formattedRemaining = remember(item.remainingBalance) {
                        String.format(Locale.US, "%.2f", maxOf(0.0, item.remainingBalance))
                    }

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
                                        text = borrowerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "+91 ${item.borrowerMobile}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight
                                    )

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
                                            try {
                                                val whatsAppMessage = "Hello $borrowerName, your daily due of ₹$formattedTodayDue for $currentDateText is pending."
                                                CommunicationUtils.openWhatsAppChat(
                                                    context = context,
                                                    rawMobileNumber = item.borrowerMobile,
                                                    message = whatsAppMessage
                                                )
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
                                            }
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
                                            try {
                                                CommunicationUtils.openPhoneDialer(context, item.borrowerMobile)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Could not open phone dialer", Toast.LENGTH_SHORT).show()
                                            }
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
                                        text = "₹$formattedTodayDue",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertOrange
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Daily EMI", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text(
                                        text = "₹$formattedInstallment",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Balance Left", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text(
                                        text = "₹$formattedRemaining",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedDueItem = item
                                        collectAmount = String.format(Locale.US, "%.2f", item.todayDueBalance)
                                        selectedPaymentMode = PaymentMode.CASH
                                    },
                                    enabled = !isSubmittingPayment,
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
            val borrowerDisplayName = if (dueItem.borrowerName.isNotBlank()) dueItem.borrowerName.trim() else "Borrower"
            val dueDailyText = String.format(Locale.US, "%.2f", dueItem.dailyInstallment)
            val dueTotalText = String.format(Locale.US, "%.2f", dueItem.todayDueBalance)
            val overdueNotice = if (dueItem.todayDueBalance > dueItem.dailyInstallment) " (Includes Overdue)" else ""
            val dialogDetailsText = "Date: $currentDateText\nDaily Due: ₹$dueDailyText | Total Due for Today: ₹$dueTotalText$overdueNotice"

            AlertDialog(
                onDismissRequest = { if (!isSubmittingPayment) selectedDueItem = null },
                title = { Text("Collect Due - $borrowerDisplayName") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = dialogDetailsText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )

                        OutlinedTextField(
                            value = collectAmount,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    collectAmount = input
                                }
                            },
                            label = { Text("Amount Received (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = !isSubmittingPayment
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedPaymentMode == PaymentMode.CASH,
                                onClick = { if (!isSubmittingPayment) selectedPaymentMode = PaymentMode.CASH },
                                label = { Text("Cash") }
                            )
                            FilterChip(
                                selected = selectedPaymentMode == PaymentMode.UPI,
                                onClick = { if (!isSubmittingPayment) selectedPaymentMode = PaymentMode.UPI },
                                label = { Text("UPI Received") }
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

                            isSubmittingPayment = true
                            scope.launch {
                                val repayment = Repayment(
                                    loanId = dueItem.loanId.trim(),
                                    borrowerId = dueItem.borrowerId.trim(),
                                    lenderId = cleanLenderId,
                                    paymentDate = DateUtils.getTodaySqlFormat(),
                                    amountPaid = parsedAmount,
                                    paymentMode = selectedPaymentMode,
                                    notes = "Daily installment collected on $currentDateText (${selectedPaymentMode.name})"
                                )

                                lenderRepo.recordRepayment(repayment)
                                    .onSuccess {
                                        isSubmittingPayment = false
                                        selectedDueItem = null
                                        Toast.makeText(context, "Payment recorded successfully!", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure { error ->
                                        isSubmittingPayment = false
                                        Toast.makeText(context, "Failed: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        },
                        enabled = !isSubmittingPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        if (isSubmittingPayment) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...")
                        } else {
                            Text("Confirm Collection")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { selectedDueItem = null },
                        enabled = !isSubmittingPayment
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}