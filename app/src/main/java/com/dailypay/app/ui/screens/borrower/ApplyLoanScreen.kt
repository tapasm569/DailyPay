package com.dailypay.app.ui.screens.borrower

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Loan
import com.dailypay.app.data.model.LoanStatus
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLoanScreen(
    borrowerId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val borrowerRepo = remember { BorrowerRepository() }

    val presetAmounts = listOf(5000, 10000, 15000, 20000, 30000, 50000)
    var selectedPrincipal by remember { mutableDoubleStateOf(10000.0) }
    var manualAmountInput by remember { mutableStateOf("10000") }
    var selectedTenureDays by remember { mutableIntStateOf(30) }
    val defaultMonthlyRate = 10.0
    var assignedLenderId by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isProfileLoading by remember { mutableStateOf(true) }

    // Fetch the real lenderId linked to this borrower
    LaunchedEffect(borrowerId) {
        borrowerRepo.getBorrowerProfile(borrowerId)
            .onSuccess { profile ->
                assignedLenderId = profile.lenderId
                isProfileLoading = false
            }
            .onFailure {
                isProfileLoading = false
                Toast.makeText(context, "Could not load lender details", Toast.LENGTH_SHORT).show()
            }
    }

    val months = selectedTenureDays / 30.0
    val totalInterest = selectedPrincipal * (defaultMonthlyRate / 100.0) * months
    val totalPayable = selectedPrincipal + totalInterest
    val dailyInstallment = if (selectedTenureDays > 0) totalPayable / selectedTenureDays else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply for a Loan") },
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
        if (isProfileLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
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
                Text("Choose Amount", style = MaterialTheme.typography.titleMedium)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(110.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetAmounts) { amt ->
                        val isSelected = selectedPrincipal == amt.toDouble()
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPrincipal = amt.toDouble()
                                manualAmountInput = amt.toString()
                            },
                            label = { Text("₹$amt") }
                        )
                    }
                }

                OutlinedTextField(
                    value = manualAmountInput,
                    onValueChange = {
                        manualAmountInput = it.filter { ch -> ch.isDigit() }
                        selectedPrincipal = it.toDoubleOrNull() ?: 0.0
                    },
                    label = { Text("Or Enter Custom Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Select Tenure", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(30, 60, 90).forEach { days ->
                        FilterChip(
                            selected = selectedTenureDays == days,
                            onClick = { selectedTenureDays = days },
                            label = { Text("$days Days") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rate:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                            Text("$defaultMonthlyRate% / Month", style = MaterialTheme.typography.titleMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Interest Amount:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                            Text("₹${String.format("%.2f", totalInterest)}", style = MaterialTheme.typography.titleMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Repayment:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                            Text("₹${String.format("%.2f", totalPayable)}", style = MaterialTheme.typography.titleMedium, color = BrandPrimary)
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Daily Due:", style = MaterialTheme.typography.titleMedium)
                            Text("₹${String.format("%.2f", dailyInstallment)} / day", style = MaterialTheme.typography.titleMedium, color = AlertOrange)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val realLenderId = assignedLenderId
                        if (realLenderId.isNullOrBlank()) {
                            Toast.makeText(context, "Cannot identify lender account. Please relogin.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (selectedPrincipal <= 0.0) {
                            Toast.makeText(context, "Please select or enter an amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSubmitting = true
                        scope.launch {
                            val loanApplication = Loan(
                                lenderId = realLenderId,
                                borrowerId = borrowerId,
                                principalAmount = selectedPrincipal,
                                tenureDays = selectedTenureDays,
                                monthlyInterestRate = defaultMonthlyRate,
                                status = LoanStatus.PENDING
                            )

                            borrowerRepo.applyLoan(loanApplication)
                                .onSuccess {
                                    isSubmitting = false
                                    Toast.makeText(context, "Loan application sent to lender!", Toast.LENGTH_LONG).show()
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
                        Text("Submit Loan Request", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
