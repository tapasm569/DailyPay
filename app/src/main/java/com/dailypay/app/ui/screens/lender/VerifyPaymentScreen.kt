package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.PendingPaymentItem
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.components.AppImageView
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyPaymentScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var pendingList by remember { mutableStateOf<List<PendingPaymentItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            lenderRepo.getPendingVerifications(lenderId)
                .onSuccess {
                    pendingList = it
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Cash (${pendingList.size})") },
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
        } else if (pendingList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("All cash payments verified!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(pendingList, key = { it.repayment.id ?: "" }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    AppImageView(
                                        data = item.borrowerProfilePic,
                                        contentDescription = item.borrowerName,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, BrandPrimary.copy(alpha = 0.4f), CircleShape),
                                        placeholderIcon = Icons.Default.Person,
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(item.borrowerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("+91 ${item.borrowerMobile}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                    }
                                }
                                Text("₹${item.repayment.amountPaid}", style = MaterialTheme.typography.titleLarge, color = AlertOrange, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val id = item.repayment.id ?: return@OutlinedButton
                                        isProcessing = true
                                        scope.launch {
                                            lenderRepo.verifyRepayment(id, accept = false)
                                                .onSuccess {
                                                    isProcessing = false
                                                    loadData()
                                                }
                                                .onFailure {
                                                    isProcessing = false
                                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !isProcessing
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reject")
                                }

                                Button(
                                    onClick = {
                                        val id = item.repayment.id ?: return@Button
                                        isProcessing = true
                                        scope.launch {
                                            lenderRepo.verifyRepayment(id, accept = true)
                                                .onSuccess {
                                                    isProcessing = false
                                                    loadData()
                                                }
                                                .onFailure {
                                                    isProcessing = false
                                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !isProcessing
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cash Received")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
