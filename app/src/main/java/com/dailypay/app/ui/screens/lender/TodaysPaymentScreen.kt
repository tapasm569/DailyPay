package com.dailypay.app.ui.screens.lender

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.BorderSubtleLight
import com.dailypay.app.ui.theme.BrandPrimary
import com.dailypay.app.ui.theme.DangerRed
import com.dailypay.app.ui.theme.MoneyGreen
import com.dailypay.app.ui.theme.MoneyGreenSubtle
import com.dailypay.app.ui.theme.TextSecondaryLight
import com.dailypay.app.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysPaymentScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository(context.applicationContext) }

    var paidItems by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentDateText = remember { DateUtils.getTodayDisplayFormat() }
    val cleanLenderId = remember(lenderId) { lenderId.trim() }

    LaunchedEffect(cleanLenderId) {
        if (cleanLenderId.isBlank() || cleanLenderId == "{lenderId}") {
            isLoading = false
            return@LaunchedEffect
        }
        scope.launch {
            lenderRepo.getDailyDues(cleanLenderId)
                .onSuccess { allDues ->
                    paidItems = allDues.filter { it.todayPaidAmount > 0.0 }
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                }
        }
    }

    val totalCollected = remember(paidItems) {
        paidItems.sumOf { it.todayPaidAmount }
    }
    val formattedTotalCollected = remember(totalCollected) {
        String.format(Locale.US, "%.2f", totalCollected)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today's Received Payments")
                        Text(
                            text = "$currentDateText • ${paidItems.size} Collections",
                            style = MaterialTheme.typography.bodySmall,
                            color = MoneyGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Total Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MoneyGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MoneyGreenSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Collected Today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight
                            )
                            Text(
                                text = "₹$formattedTotalCollected",
                                style = MaterialTheme.typography.displayMedium,
                                color = MoneyGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = TextSecondaryLight
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentDateText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MoneyGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                if (paidItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No collections recorded yet today ($currentDateText).",
                            color = TextSecondaryLight
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(
                            items = paidItems,
                            key = { index, item -> "${item.loanId}_${item.borrowerId}_$index" }
                        ) { _, item ->
                            val formattedTodayPaid = remember(item.todayPaidAmount) {
                                String.format(Locale.US, "%.2f", item.todayPaidAmount)
                            }
                            val formattedInstallment = remember(item.dailyInstallment) {
                                String.format(Locale.US, "%.2f", item.dailyInstallment)
                            }
                            val formattedTotalPaid = remember(item.totalPaid) {
                                String.format(Locale.US, "%.2f", item.totalPaid)
                            }
                            val formattedRemaining = remember(item.remainingBalance) {
                                String.format(Locale.US, "%.2f", maxOf(0.0, item.remainingBalance))
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtleLight, RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.borrowerName.ifBlank { "Borrower" },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "+91 ${item.borrowerMobile}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight
                                            )

                                            // Current Date Badge on each Card
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MoneyGreen.copy(alpha = 0.12f),
                                                modifier = Modifier.padding(top = 6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Event,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = MoneyGreen
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Paid on: $currentDateText",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MoneyGreen,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "₹$formattedTodayPaid",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MoneyGreen
                                            )
                                            Text(
                                                text = "Daily: ₹$formattedInstallment",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Total Paid: ₹$formattedTotalPaid",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryLight
                                        )
                                        Text(
                                            text = "Remaining: ₹$formattedRemaining",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (item.remainingBalance > 0.0) DangerRed else MoneyGreen
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
}
