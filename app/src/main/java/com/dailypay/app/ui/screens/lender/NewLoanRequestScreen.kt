package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.components.AppImageView
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLoanRequestScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var pendingLoans by remember { mutableStateOf<List<Loan>>(emptyList()) }
    var borrowersMap by remember { mutableStateOf<Map<String, Borrower>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedLoanForApproval by remember { mutableStateOf<Loan?>(null) }
    var selectedLoanForRejection by remember { mutableStateOf<Loan?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            lenderRepo.getBorrowers(lenderId).onSuccess { borrowers ->
                borrowersMap = borrowers.associateBy { it.id ?: "" }
            }
            lenderRepo.getPendingLoanRequests(lenderId)
                .onSuccess {
                    pendingLoans = it
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
                title = { Text("Pending Loan Requests (${pendingLoans.size})") },
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
        } else if (pendingLoans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = MoneyGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No pending loan requests!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("New customer requests will show here.", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
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
                items(pendingLoans, key = { it.id ?: "" }) { loan ->
                    val borrower = borrowersMap[loan.borrowerId]

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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AppImageView(
                                        data = borrower?.profilePicUrl,
                                        contentDescription = borrower?.name,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, BrandPrimary.copy(alpha = 0.4f), CircleShape),
                                        placeholderIcon = Icons.Default.Person,
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = borrower?.name ?: "Customer",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "+91 ${borrower?.mobileNumber ?: ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryLight
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AlertOrange.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "REQUESTED",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AlertOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderSubtleLight)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Requested Principal", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text("₹${loan.principalAmount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandPrimary)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Requested Tenure", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    Text("${loan.tenureDays} Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Est. Daily EMI", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    val estEmi = if (loan.tenureDays > 0) round((loan.principalAmount / loan.tenureDays) * 100.0) / 100.0 else 0.0
                                    Text("₹$estEmi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MoneyGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedLoanForRejection = loan },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reject")
                                }

                                Button(
                                    onClick = { selectedLoanForApproval = loan },
                                    modifier = Modifier.weight(1.3f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verify & Approve")
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= APPROVE & DISBURSE CASH MODAL =================
        selectedLoanForApproval?.let { loan ->
            var interestRateText by remember { mutableStateOf("10.0") }
            var cashNoteText by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { if (!isProcessing) selectedLoanForApproval = null },
                title = { Text("Approve & Disburse Cash") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Principal to Handover: ₹${loan.principalAmount}", fontWeight = FontWeight.Bold)
                        Text("Tenure: ${loan.tenureDays} Days")

                        OutlinedTextField(
                            value = interestRateText,
                            onValueChange = { interestRateText = it },
                            label = { Text("Monthly Interest Rate (%) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MoneyGreen.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Disbursement Mode: CASH (Starts Next Day)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MoneyGreen)
                            }
                        }

                        OutlinedTextField(
                            value = cashNoteText,
                            onValueChange = { cashNoteText = it },
                            label = { Text("Cash Handover Note (Optional)") },
                            placeholder = { Text("e.g. Disbursed cash at office") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val rate = interestRateText.toDoubleOrNull() ?: 0.0

                            isProcessing = true
                            scope.launch {
                                lenderRepo.approveAndDisburseLoan(
                                    loanId = loan.id ?: "",
                                    interestRate = rate,
                                    disbursementMode = PaymentMode.CASH,
                                    disbursementRef = cashNoteText.trim().ifBlank { "CASH_DISBURSED" }
                                ).onSuccess {
                                    isProcessing = false
                                    selectedLoanForApproval = null
                                    Toast.makeText(context, "Loan approved & cash disbursed! Daily EMI starts tomorrow.", Toast.LENGTH_SHORT).show()
                                    loadData()
                                }.onFailure {
                                    isProcessing = false
                                    Toast.makeText(context, "Approval failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                    ) {
                        Text(if (isProcessing) "Disbursing..." else "Confirm Cash Disbursement")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedLoanForApproval = null }, enabled = !isProcessing) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ================= REJECT LOAN CONFIRMATION =================
        selectedLoanForRejection?.let { loan ->
            AlertDialog(
                onDismissRequest = { if (!isProcessing) selectedLoanForRejection = null },
                title = { Text("Reject Loan Request?") },
                text = {
                    Text("Are you sure you want to reject this ₹${loan.principalAmount} loan request? The customer will see the rejection status.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = loan.id ?: return@Button
                            isProcessing = true
                            scope.launch {
                                lenderRepo.rejectLoan(id)
                                    .onSuccess {
                                        isProcessing = false
                                        selectedLoanForRejection = null
                                        Toast.makeText(context, "Loan request rejected.", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure {
                                        isProcessing = false
                                        Toast.makeText(context, "Reject failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text(if (isProcessing) "Rejecting..." else "Confirm Reject")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedLoanForRejection = null }, enabled = !isProcessing) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
