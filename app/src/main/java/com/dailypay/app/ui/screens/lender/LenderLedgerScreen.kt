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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.LenderLedgerSummary
import com.dailypay.app.data.repository.LenderRepository
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

    var lenderProfile by remember { mutableStateOf<Lender?>(null) }
    var summary by remember { mutableStateOf<LenderLedgerSummary?>(null) }
    var duesList by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isExportingPdf by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            lenderRepo.getLenderProfile(lenderId).onSuccess {
                lenderProfile = it
            }
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

    val sumTodayDue = remember(duesList) { duesList.sumOf { maxOf(0.0, it.todayDueBalance) } }
    val sumTodayPaid = remember(duesList) { duesList.sumOf { it.todayPaidAmount } }
    val sumRemaining = remember(duesList) { duesList.sumOf { maxOf(0.0, it.remainingBalance) } }
    val sumTotalPaid = remember(duesList) { duesList.sumOf { it.totalPaid } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lender Master Ledger")
                        Text(
                            text = lenderProfile?.businessName?.ifBlank { lenderProfile?.name } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (duesList.isNotEmpty()) {
                                isExportingPdf = true
                                PdfExporter.generateAndOpenLenderLedgerPdf(
                                    context = context,
                                    lender = lenderProfile,
                                    summary = summary,
                                    duesList = duesList
                                )
                                isExportingPdf = false
                            } else {
                                Toast.makeText(context, "No ledger entries to export.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isLoading && !isExportingPdf
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Download Ledger PDF",
                            tint = BrandPrimary
                        )
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Lifetime Portfolio Overview
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = lenderProfile?.businessName?.ifBlank { "Portfolio Summary" } ?: "Portfolio Summary",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Proprietor: ${lenderProfile?.ownerName?.ifBlank { lenderProfile?.name } ?: "Lender"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (duesList.isNotEmpty()) {
                                            isExportingPdf = true
                                            PdfExporter.generateAndOpenLenderLedgerPdf(
                                                context = context,
                                                lender = lenderProfile,
                                                summary = summary,
                                                duesList = duesList
                                            )
                                            isExportingPdf = false
                                        } else {
                                            Toast.makeText(context, "No ledger entries to export.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download Ledger", style = MaterialTheme.typography.labelMedium)
                                }
                            }

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
                                Text("Total Recovered", color = MoneyGreen)
                                Text("₹${summary?.totalPaid ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Today's Total Due Remaining", color = AlertOrange)
                                Text("₹${summary?.totalDueBalance ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = AlertOrange)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Outstanding Balance", color = DangerRed)
                                Text("₹${summary?.totalRemainingBalance ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                            }
                        }
                    }
                }

                // Table Section Title
                item {
                    Text(
                        text = "Customer Ledger Table (${duesList.size} Accounts)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Scrollable Table Container
                item {
                    if (duesList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No borrower loan accounts found.", color = TextSecondaryLight)
                        }
                    } else {
                        val horizontalScrollState = rememberScrollState()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtleLight, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                // Table Header Row
                                Row(
                                    modifier = Modifier
                                        .background(BrandPrimary)
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "NAME",
                                        modifier = Modifier.width(140.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Today's deu",
                                        modifier = Modifier.width(105.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = "Emi paid",
                                        modifier = Modifier.width(100.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = "Total Paid",
                                        modifier = Modifier.width(110.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = "remaining Balance",
                                        modifier = Modifier.width(135.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    )
                                }

                                HorizontalDivider(color = BorderSubtleLight)

                                // Table Data Rows
                                duesList.forEachIndexed { index, item ->
                                    val rowBg = if (index % 2 == 1) {
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                                    } else {
                                        Color.Transparent
                                    }

                                    Row(
                                        modifier = Modifier
                                            .background(rowBg)
                                            .padding(vertical = 11.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.width(140.dp)) {
                                            Text(
                                                text = item.borrowerName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "+91 ${item.borrowerMobile}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight,
                                                maxLines = 1
                                            )
                                        }

                                        Text(
                                            text = "₹${maxOf(0.0, item.todayDueBalance)}",
                                            modifier = Modifier.width(105.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (item.todayDueBalance > 0.0) AlertOrange else TextSecondaryLight,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.End
                                        )

                                        Text(
                                            text = "₹${item.todayPaidAmount}",
                                            modifier = Modifier.width(100.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (item.todayPaidAmount > 0.0) MoneyGreen else TextSecondaryLight,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.End
                                        )

                                        Text(
                                            text = "₹${item.totalPaid}",
                                            modifier = Modifier.width(110.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MoneyGreen,
                                            textAlign = TextAlign.End
                                        )

                                        Text(
                                            text = "₹${maxOf(0.0, item.remainingBalance)}",
                                            modifier = Modifier.width(135.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (item.remainingBalance > 0.0) DangerRed else MoneyGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.End
                                        )
                                    }

                                    HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.6f))
                                }

                                // Table Footer Row (Sums of Today's Due, EMI Paid, and Remaining Balance)
                                Row(
                                    modifier = Modifier
                                        .background(BrandPrimary.copy(alpha = 0.12f))
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL SUM",
                                        modifier = Modifier.width(140.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary
                                    )

                                    Text(
                                        text = "₹$sumTodayDue",
                                        modifier = Modifier.width(105.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertOrange,
                                        textAlign = TextAlign.End
                                    )

                                    Text(
                                        text = "₹$sumTodayPaid",
                                        modifier = Modifier.width(100.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MoneyGreen,
                                        textAlign = TextAlign.End
                                    )

                                    Text(
                                        text = "₹$sumTotalPaid",
                                        modifier = Modifier.width(110.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MoneyGreen,
                                        textAlign = TextAlign.End
                                    )

                                    Text(
                                        text = "₹$sumRemaining",
                                        modifier = Modifier.width(135.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}            