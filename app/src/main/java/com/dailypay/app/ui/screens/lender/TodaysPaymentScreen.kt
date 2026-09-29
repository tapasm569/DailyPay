package com.dailypay.app.ui.screens.lender

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
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
    val lenderRepo = remember { LenderRepository(context) }

    var paidItems by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentDateText = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(lenderId) {
        scope.launch {
            lenderRepo.getDailyDues(lenderId)
                .onSuccess { allDues ->
                    paidItems = allDues.filter { it.todayPaidAmount > 0 }
                    isLoading = false
                }
                .onFailure { isLoading = false }
        }
    }

    val totalCollected = paidItems.sumOf { it.todayPaidAmount }

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
                            Text("Total Collected Today", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                            Text("₹${String.format(Locale.getDefault(), "%.2f", totalCollected)}", style = MaterialTheme.typography.displayMedium, color = MoneyGreen)
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
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(32.dp))
                    }
                }

                if (paidItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No collections recorded yet today ($currentDateText).", color = TextSecondaryLight)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(paidItems, key = { it.loanId }) { item ->
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
                                                text = item.borrowerName,
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
                                                text = "₹${item.todayPaidAmount}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MoneyGreen
                                            )
                                            Text(
                                                text = "Daily: ₹${item.dailyInstallment}",
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
                                            text = "Total Paid: ₹${item.totalPaid}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryLight
                                        )
                                        Text(
                                            text = "Remaining: ₹${maxOf(0.0, item.remainingBalance)}",
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
