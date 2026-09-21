package com.dailypay.app.ui.screens.borrower

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.BorrowerDashboardSummary
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.theme.*
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

    var summary by remember { mutableStateOf<BorrowerDashboardSummary?>(null) }
    var repayments by remember { mutableStateOf<List<Repayment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            borrowerRepo.getDashboardSummary(borrowerId).onSuccess {
                summary = it
            }
            borrowerRepo.getRepaymentHistory(borrowerId)
                .onSuccess {
                    repayments = it
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(borrowerId) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Passbook & Ledger") },
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
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Today's Payment & Today's Due Highlight
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, AlertOrange.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AlertOrangeSubtle)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Today's Due", style = MaterialTheme.typography.labelSmall, color = AlertOrange)
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = AlertOrange, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹${summary?.todayDue ?: 0.0}", style = MaterialTheme.typography.titleLarge, color = AlertOrange)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MoneyGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MoneyGreenSubtle)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Paid Today", style = MaterialTheme.typography.labelSmall, color = MoneyGreen)
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹${summary?.todayPaid ?: 0.0}", style = MaterialTheme.typography.titleLarge, color = MoneyGreen)
                            }
                        }
                    }
                }

                // Balance Totals Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Loan Amount", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                                Text("₹${summary?.totalBorrowed ?: 0.0}", style = MaterialTheme.typography.titleMedium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount Repaid", style = MaterialTheme.typography.bodyMedium, color = MoneyGreen)
                                Text("₹${summary?.totalPaid ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining Outstanding", style = MaterialTheme.typography.bodyMedium, color = DangerRed)
                                Text("₹${summary?.totalRemaining ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                            }
                        }
                    }
                }

                item {
                    Text("Payment Transactions (${repayments.size})", style = MaterialTheme.typography.titleMedium)
                }

                if (repayments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No repayments made yet.", color = TextSecondaryLight)
                        }
                    }
                } else {
                    itemsIndexed(repayments, key = { index, item -> item.id ?: "${item.paymentDate}_${item.amountPaid}_$index" }) { _, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("₹${item.amountPaid}", style = MaterialTheme.typography.titleLarge, color = MoneyGreen)
                                    Text(item.paymentDate ?: "-", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                    if (!item.notes.isNullOrBlank()) {
                                        Text(item.notes, style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    }
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
    }
}