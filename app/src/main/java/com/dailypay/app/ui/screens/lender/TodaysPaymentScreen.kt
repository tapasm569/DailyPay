package com.dailypay.app.ui.screens.lender

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysPaymentScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var paidItems by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
                title = { Text("Today's Received Payments") },
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
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
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
                            Text("₹${String.format("%.2f", totalCollected)}", style = MaterialTheme.typography.displayMedium, color = MoneyGreen)
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(32.dp))
                    }
                }

                if (paidItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No collections recorded yet today.", color = TextSecondaryLight)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(paidItems) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtleLight, RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.borrowerName, style = MaterialTheme.typography.titleMedium)
                                        Text("+91 ${item.borrowerMobile}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "₹${item.todayPaidAmount}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MoneyGreen
                                        )
                                        Text("Daily: ₹${item.dailyInstallment}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
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
