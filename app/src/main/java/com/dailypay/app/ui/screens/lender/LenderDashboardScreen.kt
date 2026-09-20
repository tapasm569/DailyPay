package com.dailypay.app.ui.screens.lender

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dailypay.app.ui.theme.*

data class LenderDashboardOption(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tintColor: Color,
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
    onTodaysDueClick: () -> Unit,
    onTodaysPaymentClick: () -> Unit,
    onMasterClientClick: () -> Unit,
    onLedgerBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        LenderDashboardOption("Add Borrowers", "Register borrower & upload KYC", Icons.Default.PersonAddAlt1, BrandPrimary, onAddBorrowerClick),
        LenderDashboardOption("New Loan Request", "Review & approve requests", Icons.Default.RequestPage, AlertOrange, onNewLoanRequestClick),
        LenderDashboardOption("Set Interest", "Configure monthly interest rate", Icons.Default.Percent, CallBlue, onSetInterestClick),
        LenderDashboardOption("Give Loan Manually", "Disburse directly to client", Icons.Default.Payments, MoneyGreen, onGiveLoanManualClick),
        LenderDashboardOption("Today's Due", "Daily collection & rollover queue", Icons.Default.CalendarToday, AlertOrange, onTodaysDueClick),
        LenderDashboardOption("Today's Payment", "Collections received today", Icons.Default.CheckCircle, MoneyGreen, onTodaysPaymentClick),
        LenderDashboardOption("Master Client", "All registered borrower profiles", Icons.Default.Groups, BrandPrimary, onMasterClientClick),
        LenderDashboardOption("Ledger Book", "Summary, statements & PDF export", Icons.Default.MenuBook, CallBlue, onLedgerBookClick)
    )

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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(options) { opt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(18.dp))
                        .clickable { opt.onClick() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(opt.tintColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = opt.icon,
                                contentDescription = null,
                                tint = opt.tintColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = opt.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = opt.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
