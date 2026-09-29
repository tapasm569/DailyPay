package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.LoanStatus
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.UpiIntentLauncher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiveLoanManualScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var borrowers by remember { mutableStateOf<List<Borrower>>(emptyList()) }
    var selectedBorrower by remember { mutableStateOf<Borrower?>(null) }
    var borrowerDropdownExpanded by remember { mutableStateOf(false) }

    var principalInput by remember { mutableStateOf("") }
    var selectedTenureDays by remember { mutableIntStateOf(30) }
    var monthlyRateInput by remember { mutableStateOf("10.0") }
    var selectedDisbursementMode by remember { mutableStateOf(PaymentMode.CASH) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isLoadingBorrowers by remember { mutableStateOf(true) }

    LaunchedEffect(lenderId) {
        scope.launch {
            lenderRepo.getBorrowers(lenderId)
                .onSuccess {
                    borrowers = it
                    if (it.isNotEmpty()) selectedBorrower = it.first()
                    isLoadingBorrowers = false
                }
                .onFailure { isLoadingBorrowers = false }
        }
    }

    // Dynamic Financial Calculations
    val principal = principalInput.toDoubleOrNull() ?: 0.0
    val monthlyRate = monthlyRateInput.toDoubleOrNull() ?: 10.0
    val monthsFactor = selectedTenureDays / 30.0
    val totalInterest = (principal * (monthlyRate / 100.0) * monthsFactor)
    val totalPayable = principal + totalInterest
    val dailyDue = if (selectedTenureDays > 0) totalPayable / selectedTenureDays else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Give Loan Manually") },
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
        if (isLoadingBorrowers) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Borrower Selection Dropdown
                Text(text = "Select Borrower", style = MaterialTheme.typography.titleMedium)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { borrowerDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedBorrower?.name?.let { "$it (${selectedBorrower?.mobileNumber})" }
                                    ?: "Select a registered borrower",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = borrowerDropdownExpanded,
                        onDismissRequest = { borrowerDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        borrowers.forEach { borrower ->
                            DropdownMenuItem(
                                text = { Text("${borrower.name} (${borrower.mobileNumber})") },
                                onClick = {
                                    selectedBorrower = borrower
                                    borrowerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Loan Principal Amount
                OutlinedTextField(
                    value = principalInput,
                    onValueChange = { principalInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Principal Capital Amount (₹)") },
                    placeholder = { Text("e.g. 10000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Tenure Selection (30, 60, 90 Days)
                Text(text = "Repayment Tenure", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(30, 60, 90).forEach { days ->
                        val isSelected = selectedTenureDays == days
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTenureDays = days },
                            label = { Text("$days Days") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Monthly Interest Rate Input
                OutlinedTextField(
                    value = monthlyRateInput,
                    onValueChange = { monthlyRateInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Monthly Interest Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Live Calculation Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Interest:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                            Text("₹${String.format("%.2f", totalInterest)}", style = MaterialTheme.typography.titleMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Repayment Amount:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                            Text("₹${String.format("%.2f", totalPayable)}", style = MaterialTheme.typography.titleMedium, color = BrandPrimary)
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Daily Installment Due:", style = MaterialTheme.typography.titleMedium)
                            Text("₹${String.format("%.2f", dailyDue)} / day", style = MaterialTheme.typography.titleMedium, color = AlertOrange)
                        }
                    }
                }

                // Disbursement Mode
                Text(text = "Disbursement Channel", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = selectedDisbursementMode == PaymentMode.CASH,
                        onClick = { selectedDisbursementMode = PaymentMode.CASH },
                        label = { Text("Cash Settlement") }
                    )
                    FilterChip(
                        selected = selectedDisbursementMode == PaymentMode.UPI,
                        onClick = { selectedDisbursementMode = PaymentMode.UPI },
                        label = { Text("Direct UPI App") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Submit Button
                Button(
                    onClick = {
                        val borrower = selectedBorrower
                        if (borrower == null || borrower.id == null) {
                            Toast.makeText(context, "Select a valid borrower", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (principal <= 0.0) {
                            Toast.makeText(context, "Enter a valid principal amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (selectedDisbursementMode == PaymentMode.UPI) {
                            UpiIntentLauncher.initiateUpiPayment(
                                context = context,
                                payeeUpiId = "borrower@upi",
                                payeeName = borrower.name,
                                amount = principal,
                                transactionNote = "Loan-Disbursement-${borrower.name}"
                            )
                        }

                        isSubmitting = true
                        scope.launch {
                            val newLoan = Loan(
                                lenderId = lenderId,
                                borrowerId = borrower.id,
                                principalAmount = principal,
                                tenureDays = selectedTenureDays,
                                monthlyInterestRate = monthlyRate,
                                disbursementMode = selectedDisbursementMode,
                                status = LoanStatus.ACTIVE,
                                startDate = DateUtils.getTodaySqlFormat()
                            )

                            lenderRepo.giveLoanManually(newLoan)
                                .onSuccess {
                                    isSubmitting = false
                                    Toast.makeText(context, "Loan successfully created and active!", Toast.LENGTH_LONG).show()
                                    onNavigateBack()
                                }
                                .onFailure {
                                    isSubmitting = false
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Disburse & Activate Loan", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
