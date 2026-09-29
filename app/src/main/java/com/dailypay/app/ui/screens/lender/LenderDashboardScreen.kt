package com.dailypay.app.ui.screens.lender

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dailypay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderDashboardScreen(
    @Suppress("UNUSED_PARAMETER") lenderId: String,
    onNavigateBack: () -> Unit,
    onAddBorrowerClick: () -> Unit,
    onGiveLoanManualClick: () -> Unit,
    onNewLoanRequestClick: () -> Unit,
    onApprovedLoansClick: () -> Unit,
    onTodaysDueClick: () -> Unit,
    onTodaysPaymentClick: () -> Unit,
    onVerifyPaymentClick: () -> Unit,
    onTrackPaymentsClick: () -> Unit,
    onMasterClientClick: () -> Unit,
    onLenderLedgerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ================= 1. COLLECTIONS =================
            DashboardSectionHeader(title = "Collections")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardTile(
                        title = "Today's Due",
                        icon = Icons.Default.Schedule,
                        tint = AlertOrange,
                        onClick = onTodaysDueClick,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardTile(
                        title = "Paid Today",
                        icon = Icons.Default.CheckCircle,
                        tint = MoneyGreen,
                        onClick = onTodaysPaymentClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardTile(
                        title = "Verify Payment",
                        icon = Icons.Default.Verified,
                        tint = BrandPrimary,
                        onClick = onVerifyPaymentClick,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardTile(
                        title = "Track Records",
                        icon = Icons.Default.History,
                        tint = CallBlue,
                        onClick = onTrackPaymentsClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ================= 2. LOAN OPERATIONS =================
            DashboardSectionHeader(title = "Loan Operations")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardTile(
                        title = "New Requests",
                        icon = Icons.Default.NotificationsActive,
                        tint = AlertOrange,
                        onClick = onNewLoanRequestClick,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardTile(
                        title = "Disburse Loan",
                        icon = Icons.Default.AddCard,
                        tint = MoneyGreen,
                        onClick = onGiveLoanManualClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardTile(
                        title = "Approved Loans",
                        icon = Icons.Default.AssignmentTurnedIn,
                        tint = BrandPrimary,
                        onClick = onApprovedLoansClick,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardTile(
                        title = "Add Borrower",
                        icon = Icons.Default.PersonAdd,
                        tint = CallBlue,
                        onClick = onAddBorrowerClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ================= 3. LEDGER & RECORDS =================
            DashboardSectionHeader(title = "Ledger & Records")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardTile(
                    title = "Master Client",
                    icon = Icons.Default.People,
                    tint = BrandPrimary,
                    onClick = onMasterClientClick,
                    modifier = Modifier.weight(1f)
                )
                DashboardTile(
                    title = "Lender Ledger",
                    icon = Icons.Default.AccountBalanceWallet,
                    tint = MoneyGreen,
                    onClick = onLenderLedgerClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DashboardSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun DashboardTile(
    title: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(72.dp)
            .border(
                width = 1.dp,
                color = BorderSubtleLight.copy(alpha = 0.7f),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
