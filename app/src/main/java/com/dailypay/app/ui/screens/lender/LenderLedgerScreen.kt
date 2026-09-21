package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.LenderLedgerSummary
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderLedgerScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var summary by remember { mutableStateOf<LenderLedgerSummary?>(null) }
    var duesList by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            lenderRepo.getLedgerSummary(lenderId).onSuccess {
                summary = it
            }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lender Master Ledger") },
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Lifetime Portfolio Summary", style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider(color = BorderSubtleLight)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Disbursed", color = TextSecondaryLight)
                                Text("₹${summary?.totalDisbursed ?: 0.0}", style = MaterialTheme.typography.titleMedium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Due Remaining Today", color = AlertOrange)
                                Text("₹${summary?.totalDueBalance ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = AlertOrange)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Recovered", color = MoneyGreen)
                                Text("₹${summary?.totalPaid ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Outstanding", color = DangerRed)
                                Text("₹${summary?.totalRemainingBalance ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                            }
                        }
                    }
                }

                item {
                    Text("All Customer Accounts (${duesList.size})", style = MaterialTheme.typography.titleMedium)
                }

                if (duesList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No borrower accounts found.", color = TextSecondaryLight)
                        }
                    }
                } else {
                    itemsIndexed(duesList, key = { index, item -> item.loanId.ifBlank { "ledger_$index" } }) { _, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.borrowerName, style = MaterialTheme.typography.titleMedium)
                                    Text("+91 ${item.borrowerMobile}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                }

                                HorizontalDivider(color = BorderSubtleLight)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Payable: ₹${item.totalPayable}", style = MaterialTheme.typography.bodySmall)
                                    Text("Paid: ₹${item.totalPaid}", style = MaterialTheme.typography.bodySmall, color = MoneyGreen)
                                    Text("Balance: ₹${maxOf(0.0, item.remainingBalance)}", style = MaterialTheme.typography.bodySmall, color = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
