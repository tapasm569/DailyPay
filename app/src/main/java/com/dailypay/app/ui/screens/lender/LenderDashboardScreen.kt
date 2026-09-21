package com.dailypay.app.ui.screens.lender

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailypay.app.ui.theme.*

private data class DashboardMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderDashboardScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    onAddBorrowerClick: () -> Unit,
    onNewLoanRequestClick: () -> Unit,
    onSetInterestClick: () -> Unit,
    onGiveLoanManualClick: () -> Unit,
    onApprovedLoansClick: () -> Unit,
    onTodaysDueClick: () -> Unit,
    onTodaysPaymentClick: () -> Unit,
    onMasterClientClick: () -> Unit,
    onLedgerBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItems = listOf(
        DashboardMenuItem(
            title = "Approved Loans",
            subtitle = "Track active loans & dates",
            icon = Icons.Default.CheckCircle,
            iconTint = MoneyGreen,
            onClick = onApprovedLoansClick
        ),
        DashboardMenuItem(
            title = "Add Borrower",
            subtitle = "Register new client & KYC",
            icon = Icons.Default.PersonAdd,
            iconTint = BrandPrimary,
            onClick = onAddBorrowerClick
        ),
        DashboardMenuItem(
            title = "Loan Requests",
            subtitle = "Review & disburse loans",
            icon = Icons.Default.PendingActions,
            iconTint = AlertOrange,
            onClick = onNewLoanRequestClick
        ),
        DashboardMenuItem(
            title = "Give Loan",
            subtitle = "Disburse manually",
            icon = Icons.Default.Payments,
            iconTint = BrandAccent,
            onClick = onGiveLoanManualClick
        ),
        DashboardMenuItem(
            title = "Today's Due",
            subtitle = "Collection queue",
            icon = Icons.Default.Schedule,
            iconTint = DangerRed,
            onClick = onTodaysDueClick
        ),
        DashboardMenuItem(
            title = "Today's Payment",
            subtitle = "Received installment log",
            icon = Icons.Default.AccountBalanceWallet,
            iconTint = MoneyGreen,
            onClick = onTodaysPaymentClick
        ),
        DashboardMenuItem(
            title = "Master Clients",
            subtitle = "Manage all borrower profiles",
            icon = Icons.Default.People,
            iconTint = CallBlue,
            onClick = onMasterClientClick
        ),
        DashboardMenuItem(
            title = "Set Interest",
            subtitle = "Configure loan rates",
            icon = Icons.Default.Percent,
            iconTint = BrandPrimaryDark,
            onClick = onSetInterestClick
        ),
        DashboardMenuItem(
            title = "Ledger Book",
            subtitle = "Financial statements & PDF",
            icon = Icons.Default.ReceiptLong,
            iconTint = BrandPrimary,
            onClick = onLedgerBookClick
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lender Operations") },
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp))
                        .clickable { item.onClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = item.iconTint.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.iconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            color = TextSecondaryLight,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}