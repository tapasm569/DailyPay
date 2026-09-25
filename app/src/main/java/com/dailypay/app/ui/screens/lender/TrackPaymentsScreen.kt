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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackPaymentsScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository(context) }

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

    // Group verified payments day-wise by date
    val dayWiseMap = remember(allPayments) {
        allPayments.groupBy { it.paymentDate }
    }

    val lifetimeTotal = allPayments.sumOf { it.amountPaid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Track Payments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lifetime Recovered: ₹$lifetimeTotal",
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
        } else if (allPayments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MoneyGreen.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MoneyGreen,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No verified payment records found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondaryLight
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                dayWiseMap.forEach { (dateStr, paymentsList) ->
                    val dayTotal = paymentsList.sumOf { it.amountPaid }
                    val cashTotal = paymentsList.filter { it.paymentMode == PaymentMode.CASH }.sumOf { it.amountPaid }
                    val upiTotal = paymentsList.filter { it.paymentMode == PaymentMode.UPI }.sumOf { it.amountPaid }
                    val displayDate = DateUtils.formatToDisplayDate(dateStr)

                    // Day Summary Card Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = displayDate,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Cash: ₹$cashTotal • UPI: ₹$upiTotal (${paymentsList.size} collections)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight
                                    )
                                }
                                Text(
                                    text = "₹$dayTotal",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MoneyGreen
                                )
                            }
                        }
                    }

                    // Individual Payment Cards under that Date
                    itemsIndexed(paymentsList, key = { index, item -> item.id ?: "${item.paymentDate}_$index" }) { _, r ->
                        val borrower = borrowersMap[r.borrowerId]
                        val isUpi = r.paymentMode == PaymentMode.UPI

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtleLight, RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = borrower?.name ?: "Customer",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "+91 ${borrower?.mobileNumber ?: ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryLight
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${r.amountPaid}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MoneyGreen
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isUpi) BrandPrimary.copy(alpha = 0.12f) else AlertOrange.copy(alpha = 0.12f),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isUpi) "UPI" else "CASH",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUpi) BrandPrimary else AlertOrange
                                            )
                                        }
                                    }
                                }

                                // If UPI with UTR Reference, display copyable chip
                                if (!r.transactionRef.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.background,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleLight)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "UTR: ${r.transactionRef}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = BrandPrimary
                                            )
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(r.transactionRef))
                                                    Toast.makeText(context, "Copied UTR: ${r.transactionRef}", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy UTR",
                                                    modifier = Modifier.size(14.dp),
                                                    tint = BrandPrimary
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
    }
}
