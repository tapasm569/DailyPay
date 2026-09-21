package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.dailypay.app.R
import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovedLoansScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var approvedLoans by remember { mutableStateOf<List<ApprovedLoanDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Repayment History Dialog States
    var selectedLoanForHistory by remember { mutableStateOf<ApprovedLoanDetail?>(null) }
    var repaymentHistory by remember { mutableStateOf<List<Repayment>>(emptyList()) }
    var isLoadingHistory by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            lenderRepo.getApprovedLoans(lenderId)
                .onSuccess {
                    approvedLoans = it
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LifecycleResumeEffect(lenderId) {
        loadData()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approved Loans (${approvedLoans.size})") },
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
        } else if (approvedLoans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No approved active loans found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondaryLight
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(approvedLoans, key = { it.loanId }) { loan ->
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
                            // Borrower Header & Call/WhatsApp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = loan.borrowerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "+91 ${loan.borrowerMobile}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondaryLight
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            CommunicationUtils.openWhatsAppChat(
                                                context = context,
                                                rawMobileNumber = loan.borrowerMobile,
                                                message = "Hello ${loan.borrowerName}, regarding your approved loan of ₹${loan.principalAmount}."
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
                                        onClick = { CommunicationUtils.openPhoneDialer(context, loan.borrowerMobile) },
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

                            // Principal & Total Payable
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Approved Principal", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text("₹${loan.principalAmount}", style = MaterialTheme.typography.titleLarge, color = BrandPrimary)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Payable", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text("₹${loan.totalPayable}", style = MaterialTheme.typography.titleMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Paid vs Remaining
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = MoneyGreen)
                                    Text("₹${loan.totalPaid}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Remaining Balance", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                                    Text("₹${maxOf(0.0, loan.remainingBalance)}", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Daily Installment", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text("₹${loan.dailyInstallment}/day", style = MaterialTheme.typography.bodyMedium)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Tenure", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text("${loan.tenureDays} Days", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Date Tracking Badge
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Start: ${loan.startDate}", style = MaterialTheme.typography.bodySmall)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MoneyGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "End: ${loan.endDate}", style = MaterialTheme.typography.bodySmall, color = MoneyGreen)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    selectedLoanForHistory = loan
                                    isLoadingHistory = true
                                    scope.launch {
                                        lenderRepo.getRepaymentsForLoan(loan.loanId)
                                            .onSuccess {
                                                repaymentHistory = it
                                                isLoadingHistory = false
                                            }
                                            .onFailure {
                                                isLoadingHistory = false
                                                Toast.makeText(context, "Could not load history: ${it.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("View Repayment History")
                            }
                        }
                    }
                }
            }
        }

        // Repayment Details Dialog
        selectedLoanForHistory?.let { loan ->
            AlertDialog(
                onDismissRequest = { selectedLoanForHistory = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Repayment Details", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { selectedLoanForHistory = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = BrandPrimary.copy(alpha = 0.08f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(loan.borrowerName, style = MaterialTheme.typography.titleMedium, color = BrandPrimary)
                                Text("Paid: ₹${loan.totalPaid} | Balance: ₹${maxOf(0.0, loan.remainingBalance)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        if (isLoadingHistory) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = BrandPrimary)
                            }
                        } else if (repaymentHistory.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No repayments recorded yet.", color = TextSecondaryLight)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(repaymentHistory, key = { it.id ?: "${it.paymentDate}_${it.amountPaid}" }) { item ->
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
                                                Text("₹${item.amountPaid}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                                                Text(item.paymentDate, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                            }
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(item.paymentMode.name) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedLoanForHistory = null },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}