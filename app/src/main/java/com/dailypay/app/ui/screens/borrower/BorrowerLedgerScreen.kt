package com.dailypay.app.ui.screens.borrower

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerLedgerScreen(
    borrowerId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val borrowerRepo = remember { BorrowerRepository() }

    var dueDetails by remember { mutableStateOf<DailyDueItem?>(null) }
    var repayments by remember { mutableStateOf<List<Repayment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(borrowerId) {
        scope.launch {
            borrowerRepo.getBorrowerDueDetails(borrowerId).onSuccess { due ->
                dueDetails = due
                due?.loanId?.let { loanId ->
                    borrowerRepo.getRepaymentHistory(loanId).onSuccess { list ->
                        repayments = list
                    }
                }
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Passbook / Ledger") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        dueDetails?.let { due ->
                            PdfExporter.generateLenderLedgerPdf(
                                context = context,
                                businessName = "Borrower Passbook - ${due.borrowerName}",
                                duesList = listOf(due)
                            )
                        }
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Download PDF", tint = BrandPrimary)
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
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Summary Pod
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Payable", style = MaterialTheme.typography.labelMedium, color = TextSecondaryLight)
                            Text("₹${dueDetails?.totalPayable ?: 0.0}", style = MaterialTheme.typography.titleMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Paid", style = MaterialTheme.typography.labelMedium, color = MoneyGreen)
                            Text("₹${dueDetails?.totalPaid ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", style = MaterialTheme.typography.labelMedium, color = DangerRed)
                            Text("₹${dueDetails?.remainingBalance ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                        }
                    }
                }

                Text("Payment Transactions", style = MaterialTheme.typography.titleMedium)

                if (repayments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No repayments recorded yet.", color = TextSecondaryLight)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtleLight, RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        itemsIndexed(repayments) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Installment #${index + 1}", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        DateUtils.formatToIndianDate(item.paymentDate),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondaryLight
                                    )
                                }
                                Text(
                                    "+ ₹${item.amountPaid}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MoneyGreen
                                )
                            }
                            HorizontalDivider(color = BorderSubtleLight)
                        }
                    }
                }
            }
        }
    }
}
