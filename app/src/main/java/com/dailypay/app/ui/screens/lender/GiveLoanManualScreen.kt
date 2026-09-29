package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.CreateLoanRequest
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    var borrowersList by remember { mutableStateOf<List<Borrower>>(emptyList()) }
    var selectedBorrower by remember { mutableStateOf<Borrower?>(null) }
    var showBorrowerDialog by remember { mutableStateOf(false) }
    var borrowerSearchQuery by remember { mutableStateOf("") }
    var isLoadingBorrowers by remember { mutableStateOf(true) }

    var principalInput by remember { mutableStateOf("") }
    var interestRateInput by remember { mutableStateOf("10") }
    var tenureDaysInput by remember { mutableStateOf("100") }

    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(lenderId) {
        val cleanLenderId = lenderId.trim()
        if (cleanLenderId.isNotBlank() && cleanLenderId != "unknown") {
            lenderRepo.getBorrowers(cleanLenderId)
                .onSuccess {
                    borrowersList = it
                    isLoadingBorrowers = false
                }
                .onFailure {
                    isLoadingBorrowers = false
                    Toast.makeText(context, "Failed to load clients: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            isLoadingBorrowers = false
            Toast.makeText(context, "Invalid lender session.", Toast.LENGTH_SHORT).show()
        }
    }

    val principal = principalInput.toDoubleOrNull() ?: 0.0
    val interestRate = interestRateInput.toDoubleOrNull() ?: 0.0
    val tenureDays = tenureDaysInput.toIntOrNull() ?: 0

    val interestAmount = (principal * interestRate) / 100.0
    val totalRepayable = principal + interestAmount
    val dailyEmi = if (tenureDays > 0) (totalRepayable / tenureDays) else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disburse Loan (Manual)") },
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Select Borrower *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (selectedBorrower != null) BrandPrimary else BorderSubtleLight,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        borrowerSearchQuery = ""
                        showBorrowerDialog = true
                    },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedBorrower?.name ?: "Choose client from master list",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedBorrower != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedBorrower != null) MaterialTheme.colorScheme.onSurface else TextSecondaryLight
                        )
                        if (selectedBorrower != null) {
                            Text(
                                text = "+91 ${selectedBorrower?.mobileNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandPrimary
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select", tint = BrandPrimary)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = principalInput,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                        principalInput = input
                    }
                },
                label = { Text("Principal Amount (₹) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = interestRateInput,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                            interestRateInput = input
                        }
                    },
                    label = { Text("Interest Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = tenureDaysInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 4) tenureDaysInput = digits
                    },
                    label = { Text("Tenure (Days) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtleLight, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Repayment Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.5f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Interest Total:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                        Text("₹ ${String.format(Locale.US, "%.2f", interestAmount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Collectable:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                        Text("₹ ${String.format(Locale.US, "%.2f", totalRepayable)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MoneyGreen)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Daily Due Installment:", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                        Text("₹ ${String.format(Locale.US, "%.2f", dailyEmi)} / day", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = BrandPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val cleanLenderId = lenderId.trim()
                    val borrower = selectedBorrower
                    val borrowerId = borrower?.id?.trim()

                    if (cleanLenderId.isBlank() || cleanLenderId == "unknown") {
                        Toast.makeText(context, "Invalid lender session. Please re-login.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (borrower == null || borrowerId.isNullOrBlank()) {
                        Toast.makeText(context, "Please select a borrower.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (principal <= 0.0) {
                        Toast.makeText(context, "Please enter a valid principal amount.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (tenureDays <= 0) {
                        Toast.makeText(context, "Please enter a tenure greater than 0 days.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    scope.launch {
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val loanRequest = CreateLoanRequest(
                            lenderId = cleanLenderId,
                            borrowerId = borrowerId,
                            principalAmount = principal,
                            interestRate = interestRate,
                            totalAmount = totalRepayable,
                            tenureDays = tenureDays,
                            dailyInstallment = dailyEmi,
                            disbursedDate = currentDate
                        )

                        lenderRepo.createLoan(loanRequest)
                            .onSuccess {
                                isSubmitting = false
                                Toast.makeText(context, "Loan disbursed successfully!", Toast.LENGTH_LONG).show()
                                onNavigateBack()
                            }
                            .onFailure {
                                isSubmitting = false
                                Toast.makeText(context, "Disbursement failed: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirm & Disburse Loan", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        if (showBorrowerDialog) {
            val filteredBorrowers = borrowersList.filter {
                it.name.contains(borrowerSearchQuery, ignoreCase = true) ||
                        it.mobileNumber.contains(borrowerSearchQuery)
            }

            Dialog(onDismissRequest = { showBorrowerDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Select Borrower", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showBorrowerDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        OutlinedTextField(
                            value = borrowerSearchQuery,
                            onValueChange = { borrowerSearchQuery = it },
                            placeholder = { Text("Search by name or mobile...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        HorizontalDivider(color = BorderSubtleLight)

                        if (filteredBorrowers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isLoadingBorrowers) "Loading borrowers..." else "No clients found.",
                                    color = TextSecondaryLight
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredBorrowers) { borrower ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedBorrower = borrower
                                                showBorrowerDialog = false
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selectedBorrower?.id == borrower.id) BrandPrimary.copy(alpha = 0.08f) else Color.Transparent
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = BrandPrimary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(borrower.name, fontWeight = FontWeight.SemiBold)
                                                Text("+91 ${borrower.mobileNumber}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
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
