package com.dailypay.app.ui.screens.borrower

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Borrower
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

    var borrowerProfile by remember { mutableStateOf<Borrower?>(null) }
    var isLoadingProfile by remember { mutableStateOf(true) }

    var principalAmountText by remember { mutableStateOf("") }
    var tenureDaysText by remember { mutableStateOf("30") }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(borrowerId) {
        borrowerRepo.getBorrowerProfile(borrowerId)
            .onSuccess {
                borrowerProfile = it
                isLoadingProfile = false
            }
            .onFailure {
                isLoadingProfile = false
                Toast.makeText(context, "Failed to load profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply For New Loan") },
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
        if (isLoadingProfile) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Applicant: ${borrowerProfile?.name ?: "Customer"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandPrimary
                        )
                        Text(
                            text = "Registered Mobile: +91 ${borrowerProfile?.mobileNumber ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                }

                OutlinedTextField(
                    value = principalAmountText,
                    onValueChange = { principalAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Loan Amount Required (₹) *") },
                    placeholder = { Text("e.g. 5000, 10000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tenureDaysText,
                    onValueChange = { tenureDaysText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tenure (In Days) *") },
                    placeholder = { Text("Default: 30 days") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Quick Tenure Selectors
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("30", "60", "90", "100").forEach { days ->
                        FilterChip(
                            selected = tenureDaysText == days,
                            onClick = { tenureDaysText = days },
                            label = { Text("$days Days") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val principal = principalAmountText.toDoubleOrNull() ?: 0.0
                        val tenure = tenureDaysText.toIntOrNull() ?: 30
                        val targetLenderId = borrowerProfile?.lenderId

                        if (principal <= 0.0) {
                            Toast.makeText(context, "Please enter a valid loan amount.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (targetLenderId.isNullOrBlank()) {
                            Toast.makeText(context, "Account is not linked to any lender.", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        isSubmitting = true
                        scope.launch {
                            val loan = Loan(
                                lenderId = targetLenderId,
                                borrowerId = borrowerId,
                                principalAmount = principal,
                                tenureDays = tenure,
                                totalPayable = principal,
                                dailyInstallment = (principal / tenure),
                                status = LoanStatus.PENDING
                            )

                            borrowerRepo.applyLoan(loan)
                                .onSuccess {
                                    isSubmitting = false
                                    Toast.makeText(context, "Loan application submitted! Waiting for approval.", Toast.LENGTH_LONG).show()
                                    onNavigateBack()
                                }
                                .onFailure {
                                    isSubmitting = false
                                    Toast.makeText(context, "Failed to apply: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text(if (isSubmitting) "Submitting Application..." else "Submit Loan Application")
                }
            }
        }
    }
}
