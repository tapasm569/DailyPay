package com.dailypay.app.ui.screens.lender

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
                title = { Text("Lender Dashboard") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Priority Verification & Tracking
            Text("Payments & Collections", style = MaterialTheme.typography.titleMedium)

            LenderOptionCard(
                title = "Verify Payment",
                subtitle = "Approve or reject customer submitted UPI & Cash payments",
                icon = Icons.Default.CheckCircle,
                accentColor = MoneyGreen,
                onClick = onVerifyPaymentClick
            )

            LenderOptionCard(
                title = "Track Payment",
                subtitle = "Day-wise history of all verified payments & collections",
                icon = Icons.Default.DateRange,
                accentColor = BrandPrimary,
                onClick = onTrackPaymentsClick
            )

            LenderOptionCard(
                title = "Today's Due",
                subtitle = "All pending borrowers installment list for today",
                icon = Icons.Default.Schedule,
                accentColor = AlertOrange,
                onClick = onTodaysDueClick
            )

            LenderOptionCard(
                title = "Today's Payment",
                subtitle = "Collections recorded and cleared today",
                icon = Icons.Default.Payment,
                accentColor = MoneyGreen,
                onClick = onTodaysPaymentClick
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text("Loan Operations", style = MaterialTheme.typography.titleMedium)

            LenderOptionCard(
                title = "Add Borrower",
                subtitle = "Register new borrower with KYC documentation",
                icon = Icons.Default.PersonAdd,
                accentColor = BrandPrimary,
                onClick = onAddBorrowerClick
            )

            LenderOptionCard(
                title = "Give Loan (Manual)",
                subtitle = "Create direct loan without borrower request",
                icon = Icons.Default.Add,
                accentColor = CallBlue,
                onClick = onGiveLoanManualClick
            )

            LenderOptionCard(
                title = "New Loan Requests",
                subtitle = "Review and approve borrower applied loans",
                icon = Icons.Default.PostAdd,
                accentColor = AlertOrange,
                onClick = onNewLoanRequestClick
            )

            LenderOptionCard(
                title = "Approved Loans",
                subtitle = "Active loans, tenures, start and end dates",
                icon = Icons.Default.Done,
                accentColor = MoneyGreen,
                onClick = onApprovedLoansClick
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text("Ledger & Records", style = MaterialTheme.typography.titleMedium)

            LenderOptionCard(
                title = "Master Client",
                subtitle = "View and edit all registered borrowers profile",
                icon = Icons.Default.Person,
                accentColor = BrandPrimary,
                onClick = onMasterClientClick
            )

            LenderOptionCard(
                title = "Master Ledger",
                subtitle = "Lifetime disbursements, dues, and recoveries",
                icon = Icons.Default.ReceiptLong,
                accentColor = CallBlue,
                onClick = onLenderLedgerClick
            )
        }
    }
}

@Composable
private fun LenderOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtleLight, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = TextSecondaryLight,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
