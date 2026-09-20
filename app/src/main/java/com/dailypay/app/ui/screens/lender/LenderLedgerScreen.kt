package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.LenderLedgerSummary
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.components.MetricCard
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.PdfExporter
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

    var ledgerSummary by remember { mutableStateOf<LenderLedgerSummary?>(null) }
    var duesList by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(lenderId) {
        scope.launch {
            lenderRepo.getLedgerSummary(lenderId).onSuccess { ledgerSummary = it }
            lenderRepo.getDailyDues(lenderId).onSuccess { duesList = it }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Ledger Book") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        PdfExporter.generateLenderLedgerPdf(
                            context = context,
                            businessName = "DailyPay Ledger",
                            duesList = duesList
                        )
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = BrandPrimary)
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
                    .padding(16.dp)
            ) {
                // Summary Cards Grid (2x2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Disbursed",
                        value = "₹${ledgerSummary?.totalDisbursed ?: 0.0}",
                        icon = Icons.Default.AccountBalance,
                        accentColor = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Due Balance",
                        value = "₹${ledgerSummary?.totalDueBalance ?: 0.0}",
                        icon = Icons.Default.PendingActions,
                        accentColor = AlertOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Paid",
                        value = "₹${ledgerSummary?.totalPaid ?: 0.0}",
                        icon = Icons.Default.CheckCircle,
                        accentColor = MoneyGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Remaining",
                        value = "₹${ledgerSummary?.totalRemainingBalance ?: 0.0}",
                        icon = Icons.Default.MonetizationOn,
                        accentColor = DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Scrollable Data Table
                Text(
                    text = "Borrower Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                val horizontalScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .background(BrandPrimary.copy(alpha = 0.08f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SL", modifier = Modifier.width(40.dp), style = MaterialTheme.typography.labelMedium)
                            Text("Borrower Name", modifier = Modifier.width(140.dp), style = MaterialTheme.typography.labelMedium)
                            Text("Today Due", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.labelMedium)
                            Text("Today Paid", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.labelMedium)
                            Text("Daily Due", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.labelMedium)
                            Text("Remaining", modifier = Modifier.width(100.dp), style = MaterialTheme.typography.labelMedium)
                        }

                        HorizontalDivider(color = BorderSubtleLight)

                        // Table Rows
                        LazyColumn {
                            itemsIndexed(duesList) { index, item ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${index + 1}", modifier = Modifier.width(40.dp), style = MaterialTheme.typography.bodyMedium)
                                    Text(item.borrowerName, modifier = Modifier.width(140.dp), style = MaterialTheme.typography.bodyMedium)
                                    Text("₹${item.todayDueBalance}", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.bodyMedium, color = AlertOrange)
                                    Text("₹${item.todayPaidAmount}", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.bodyMedium, color = MoneyGreen)
                                    Text("₹${item.dailyInstallment}", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.bodyMedium)
                                    Text("₹${item.remainingBalance}", modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodyMedium)
                                }
                                HorizontalDivider(color = BorderSubtleLight)
                            }
                        }
                    }
                }
            }
        }
    }
}
