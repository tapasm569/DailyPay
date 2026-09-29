@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dailypay.app.ui.screens.lender

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.SessionManager

private data class DashboardItem(
    val titleRes: Int,
    val icon: ImageVector,
    val iconColor: Color,
    val onClick: () -> Unit
)

@Composable
fun LenderDashboardScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddBorrower: () -> Unit,
    onNavigateToMasterClient: () -> Unit,
    onNavigateToGiveLoan: () -> Unit,
    onNavigateToNewLoanRequest: () -> Unit,
    onNavigateToApprovedLoans: () -> Unit,
    onNavigateToTrackPayments: () -> Unit,
    onNavigateToTodaysDue: () -> Unit,
    onNavigateToTodaysPayment: () -> Unit,
    onNavigateToVerifyPayment: () -> Unit,
    onNavigateToLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val resolvedLenderId = remember(lenderId) {
        if (lenderId.isNotBlank() && lenderId != "{lenderId}") lenderId
        else sessionManager.getUserId() ?: ""
    }

    val dashboardItems = listOf(
        DashboardItem(R.string.lender_add_borrower, Icons.Default.PersonAdd, BrandPrimary, onNavigateToAddBorrower),
        DashboardItem(R.string.lender_master_client, Icons.Default.People, CallBlue, onNavigateToMasterClient),
        DashboardItem(R.string.lender_give_loan_manually, Icons.Default.MonetizationOn, AlertOrange, onNavigateToGiveLoan),
        DashboardItem(R.string.lender_new_loan_request, Icons.Default.PostAdd, BrandPrimary, onNavigateToNewLoanRequest),
        DashboardItem(R.string.lender_ledger_book, Icons.Default.MenuBook, MoneyGreen, onNavigateToLedger),
        DashboardItem(R.string.borrower_approved_loan, Icons.Default.CheckCircle, MoneyGreen, onNavigateToApprovedLoans),
        DashboardItem(R.string.admin_payment_history, Icons.Default.History, CallBlue, onNavigateToTrackPayments),
        DashboardItem(R.string.todays_due, Icons.Default.Schedule, AlertOrange, onNavigateToTodaysDue),
        DashboardItem(R.string.todays_payment, Icons.Default.Payment, MoneyGreen, onNavigateToTodaysPayment),
        DashboardItem(R.string.btn_verify, Icons.Default.FactCheck, BrandPrimary, onNavigateToVerifyPayment)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lender Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(dashboardItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable { item.onClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = CircleShape,
                            color = item.iconColor.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.iconColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(item.titleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
