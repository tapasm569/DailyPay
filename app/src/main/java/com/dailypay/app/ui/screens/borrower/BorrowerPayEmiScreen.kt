package com.dailypay.app.ui.screens.borrower

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.UpiIntentLauncher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerPayEmiScreen(
    borrowerId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val borrowerRepo = remember { BorrowerRepository() }

    var activeLoans by remember { mutableStateOf<List<ApprovedLoanDetail>>(emptyList()) }
    var selectedLoan by remember { mutableStateOf<ApprovedLoanDetail?>(null) }
    var lenderUpiId by remember { mutableStateOf<String?>(null) }
    var lenderName by remember { mutableStateOf("Lender") }
    var isLoading by remember { mutableStateOf(true) }

    var amountText by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.UPI) }
    var utrReferenceText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val currentDateText = remember { DateUtils.getTodayDisplayFormat() }

    // Launcher to capture UPI app return response (UTR, Transaction ID, Status)
    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val upiResult = UpiIntentLauncher.parseUpiResponse(result.data)
        if (!upiResult.approvalRefNo.isNullOrBlank()) {
            utrReferenceText = upiResult.approvalRefNo
            Toast.makeText(context, "UTR Captured: ${upiResult.approvalRefNo}", Toast.LENGTH_SHORT).show()
        } else if (!upiResult.txnId.isNullOrBlank()) {
            utrReferenceText = upiResult.txnId
        }

        if (upiResult.status == "SUCCESS") {
            Toast.makeText(context, "Payment completed in UPI app! Now tap 'Submit for Lender Verification'.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(borrowerId) {
        borrowerRepo.getCustomerApprovedLoans(borrowerId)
            .onSuccess { loans ->
                activeLoans = loans.filter { it.remainingBalance > 0.0 }
                val current = activeLoans.firstOrNull()
                selectedLoan = current
                if (current != null) {
                    amountText = if (current.todayDue > 0.0) current.todayDue.toString() else current.dailyInstallment.toString()
                    val profile = borrowerRepo.getBorrowerProfile(borrowerId).getOrNull()
                    val lenderId = profile?.lenderId
                    if (!lenderId.isNullOrBlank()) {
                        borrowerRepo.getLenderProfile(lenderId).onSuccess { lender ->
                            lenderUpiId = lender.upiId
                            lenderName = lender.businessName?.ifBlank { lender.name } ?: lender.name
                        }
                    }
                }
                isLoading = false
            }
            .onFailure {
                isLoading = false
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay EMI") },
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
        } else if (activeLoans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active loan dues found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondaryLight
                )
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Due Card with Date Badge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AlertOrangeSubtle),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Today's Installment Due", style = MaterialTheme.typography.labelSmall, color = AlertOrange)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AlertOrange.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = AlertOrange
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentDateText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AlertOrange,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Text(
                            text = "₹${selectedLoan?.todayDue ?: 0.0}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = AlertOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daily Rate: ₹${selectedLoan?.dailyInstallment}/day • Remaining: ₹${selectedLoan?.remainingBalance}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                }

                // Amount to pay
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Payment Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Payment Method", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedPaymentMode == PaymentMode.UPI,
                        onClick = { selectedPaymentMode = PaymentMode.UPI },
                        label = { Text("UPI (Online Intent)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedPaymentMode == PaymentMode.CASH,
                        onClick = { selectedPaymentMode = PaymentMode.CASH },
                        label = { Text("Cash (Handover)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedPaymentMode == PaymentMode.UPI) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtleLight, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Pay via Any UPI App", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                            if (lenderUpiId.isNullOrBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = DangerRed.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Lender has not set a UPI ID yet. Please check with your lender or pay cash.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DangerRed
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Payee: $lenderName ($lenderUpiId)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                            }

                            Button(
                                onClick = {
                                    val amount = amountText.toDoubleOrNull() ?: 0.0
                                    if (amount <= 0.0) {
                                        Toast.makeText(context, "Enter a valid amount first", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val targetUpi = lenderUpiId?.trim()
                                    if (targetUpi.isNullOrBlank()) {
                                        Toast.makeText(context, "Lender UPI ID is not configured.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val chooserIntent = UpiIntentLauncher.createUpiIntent(
                                        payeeUpiId = targetUpi,
                                        payeeName = lenderName,
                                        amount = amount,
                                        transactionRef = "DP${System.currentTimeMillis()}",
                                        transactionNote = "Repayment-${selectedLoan?.borrowerName}"
                                    )
                                    try {
                                        upiLauncher.launch(chooserIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No UPI app (PhonePe, GPay, Paytm) found on device.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pay via UPI App")
                            }

                            OutlinedTextField(
                                value = utrReferenceText,
                                onValueChange = { utrReferenceText = it.filter { ch -> ch.isLetterOrDigit() }.take(20) },
                                label = { Text("12-Digit UPI Ref / UTR Number *") },
                                placeholder = { Text("Auto-filled or copy from receipt") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Text(
                            text = "Hand over cash to your lender in person. After submitting here, your lender will verify the payment to clear today's due.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        val loan = selectedLoan

                        if (amount <= 0.0 || loan == null) {
                            Toast.makeText(context, "Enter a valid payment amount.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (selectedPaymentMode == PaymentMode.UPI && utrReferenceText.trim().isBlank()) {
                            Toast.makeText(context, "Please enter or auto-capture the UPI UTR number.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSubmitting = true
                        scope.launch {
                            val profile = borrowerRepo.getBorrowerProfile(borrowerId).getOrNull()
                            val targetLenderId = profile?.lenderId ?: ""

                            val repayment = Repayment(
                                loanId = loan.loanId,
                                borrowerId = borrowerId,
                                lenderId = targetLenderId,
                                paymentDate = DateUtils.getTodaySqlFormat(),
                                amountPaid = amount,
                                paymentMode = selectedPaymentMode,
                                transactionRef = if (selectedPaymentMode == PaymentMode.UPI) utrReferenceText.trim() else null,
                                status = "PENDING",
                                notes = if (selectedPaymentMode == PaymentMode.UPI) "UPI payment submitted by borrower (UTR: ${utrReferenceText.trim()})" else "Cash payment submitted by borrower"
                            )

                            borrowerRepo.submitEmiPayment(repayment)
                                .onSuccess {
                                    isSubmitting = false
                                    Toast.makeText(context, "Payment submitted! Awaiting lender approval.", Toast.LENGTH_LONG).show()
                                    onNavigateBack()
                                }
                                .onFailure {
                                    isSubmitting = false
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                ) {
                    Text(
                        text = if (isSubmitting) "Submitting..." else "Submit for Lender Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
