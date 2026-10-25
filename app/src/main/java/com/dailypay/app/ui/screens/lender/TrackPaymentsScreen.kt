package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.components.AppImageView
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackPaymentsScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var allPayments by remember { mutableStateOf<List<Repayment>>(emptyList()) }
    var borrowersMap by remember { mutableStateOf<Map<String, Borrower>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            lenderRepo.getBorrowers(lenderId).onSuccess { list ->
                borrowersMap = list.associateBy { it.id ?: "" }
            }
            lenderRepo.getTrackPayments(lenderId)
                .onSuccess {
                    allPayments = it
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(lenderId) {
        loadData()
    }

    val dayWiseMap = remember(allPayments) {
        allPayments.groupBy { it.paymentDate ?: "Undated" }
    }

    val lifetimeTotal = allPayments.sumOf { it.amountPaid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Track Cash Payments")
                        Text("Total Cash Collected: ₹$lifetimeTotal", style = MaterialTheme.typography.bodySmall, color = MoneyGreen)
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
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        } else if (allPayments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No verified cash payment records found.", style = MaterialTheme.typography.bodyLarge, color = TextSecondaryLight)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                dayWiseMap.forEach { (date, paymentsList) ->
                    val dayTotal = paymentsList.sumOf { it.amountPaid }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.08f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(date, style = MaterialTheme.typography.titleMedium, color = BrandPrimary)
                                    Text("Cash Collected (${paymentsList.size} entries)", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                }
                                Text("₹$dayTotal", style = MaterialTheme.typography.titleLarge, color = MoneyGreen)
                            }
                        }
                    }

                    itemsIndexed(paymentsList, key = { index, item -> item.id ?: "${item.paymentDate}_$index" }) { _, r ->
                        val borrower = borrowersMap[r.borrowerId]

                        Card(
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtleLight, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    AppImageView(
                                        data = borrower?.profilePicUrl,
                                        contentDescription = borrower?.name,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .border(1.2.dp, BrandPrimary.copy(alpha = 0.4f), CircleShape),
                                        placeholderIcon = Icons.Default.Person,
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(borrower?.name ?: "Customer", style = MaterialTheme.typography.titleMedium)
                                        Text("+91 ${borrower?.mobileNumber ?: ""}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                        if (!r.notes.isNullOrBlank()) {
                                            Text(r.notes, style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${r.amountPaid}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("CASH") }
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
