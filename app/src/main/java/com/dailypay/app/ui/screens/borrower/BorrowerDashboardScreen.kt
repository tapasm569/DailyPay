package com.dailypay.app.ui.screens.borrower

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.BorrowerDashboardSummary
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerDashboardScreen(
    borrowerId: String,
    onApplyLoanClick: () -> Unit,
    onApprovedLoansClick: () -> Unit,
    onViewLedgerClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val borrowerRepo = remember { BorrowerRepository() }

    var borrowerProfile by remember { mutableStateOf<Borrower?>(null) }
    var summary by remember { mutableStateOf<BorrowerDashboardSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            borrowerRepo.getBorrowerProfile(borrowerId).onSuccess {
                borrowerProfile = it
            }
            borrowerRepo.getDashboardSummary(borrowerId)
                .onSuccess {
                    summary = it
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
                title = {
                    Column {
                        Text(
                            text = borrowerProfile?.name ?: "Customer Portal",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (borrowerProfile != null) {
                            Text(
                                text = "+91 ${borrowerProfile?.mobileNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = DangerRed
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
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Status Dual-Action Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Today's Due Box
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
                                Text("Today's Due", style = MaterialTheme.typography.labelMedium, color = AlertOrange)
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = AlertOrange, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${summary?.todayDue ?: 0.0}",
                                style = MaterialTheme.typography.titleLarge,
                                color = AlertOrange
                            )
                        }
                    }

                    // 2. Today's Payment Box
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
                                Text("Paid Today", style = MaterialTheme.typography.labelMedium, color = MoneyGreen)
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${summary?.todayPaid ?: 0.0}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MoneyGreen
                            )
                        }
                    }
                }

                // Overall Loan Overview
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
                            Text("Total Loan Overview", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${summary?.activeLoansCount ?: 0} Active Loans",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandPrimary
                            )
                        }
                        HorizontalDivider(color = BorderSubtleLight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Borrowed", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                            Text("₹${summary?.totalBorrowed ?: 0.0}", style = MaterialTheme.typography.titleMedium)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Lifetime Paid", style = MaterialTheme.typography.bodyMedium, color = MoneyGreen)
                            Text("₹${summary?.totalPaid ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remaining Balance", style = MaterialTheme.typography.bodyMedium, color = DangerRed)
                            Text("₹${summary?.totalRemaining ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                        }
                    }
                }

                Text("Quick Actions", style = MaterialTheme.typography.titleMedium)

                // 1. Approved Loans
                CustomerActionCard(
                    title = "Approved Loans",
                    subtitle = "Track today's due, paid today, tenure & end date",
                    icon = Icons.Default.CheckCircle,
                    accentColor = MoneyGreen,
                    onClick = onApprovedLoansClick
                )

                // 2. Apply for New Loan
                CustomerActionCard(
                    title = "Apply For Loan",
                    subtitle = "Submit a new daily repayment loan application",
                    icon = Icons.Default.PostAdd,
                    accentColor = BrandPrimary,
                    onClick = onApplyLoanClick
                )

                // 3. View Ledger / History
                CustomerActionCard(
                    title = "My Ledger & Receipts",
                    subtitle = "View full date-wise payment history and statements",
                    icon = Icons.Default.ReceiptLong,
                    accentColor = CallBlue,
                    onClick = onViewLedgerClick
                )
            }
        }
    }
}

@Composable
private fun CustomerActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondaryLight
            )
        }
    }
}