package com.dailypay.app.ui.screens.borrower

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.components.MetricCard
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.UpiIntentLauncher
import kotlinx.coroutines.launch

@Composable
fun BorrowerDashboardScreen(
    borrowerId: String,
    onApplyLoanClick: () -> Unit,
    onViewLedgerClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val borrowerRepo = remember { BorrowerRepository() }

    var activeLoan by remember { mutableStateOf<Loan?>(null) }
    var dueDetails by remember { mutableStateOf<DailyDueItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(borrowerId) {
        scope.launch {
            borrowerRepo.getActiveLoan(borrowerId).onSuccess { activeLoan = it }
            borrowerRepo.getBorrowerDueDetails(borrowerId).onSuccess { dueDetails = it }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DailyPay Customer",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = DateUtils.getTodayIndianFormat(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }

                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DangerRedSubtle)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = DangerRed)
                    }
                }
            }
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Dues Hero Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Today's Installment Due",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC7D2FE)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹${dueDetails?.todayDueBalance ?: 0.0}",
                            style = MaterialTheme.typography.displayLarge,
                            color = AlertOrange
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val due = dueDetails?.todayDueBalance ?: 0.0
                                if (due > 0) {
                                    UpiIntentLauncher.initiateUpiPayment(
                                        context = context,
                                        payeeUpiId = "lender@upi", // Lender's UPI registered in database
                                        payeeName = "Lender Account",
                                        amount = due,
                                        transactionNote = "DailyPay Installment"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pay Today's Due (UPI)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }
                }

                // Balance Summary Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Paid Today",
                        value = "₹${dueDetails?.todayPaidAmount ?: 0.0}",
                        icon = Icons.Default.CheckCircle,
                        accentColor = MoneyGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Remaining",
                        value = "₹${dueDetails?.remainingBalance ?: 0.0}",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick Navigation Cards
                Card(
                    onClick = onApplyLoanClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Apply for a Loan", style = MaterialTheme.typography.titleMedium)
                            Text("Select preset ₹5k - ₹50k or enter amount", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                        }
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = BrandPrimary)
                    }
                }

                Card(
                    onClick = onViewLedgerClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Personal Passbook / Ledger", style = MaterialTheme.typography.titleMedium)
                            Text("View history & download PDF statement", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                        }
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = CallBlue)
                    }
                }
            }
        }
    }
}
