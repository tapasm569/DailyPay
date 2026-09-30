package com.dailypay.app.ui.screens.borrower

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dailypay.app.data.model.ApprovedLoanDetail
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.BorrowerDashboardSummary
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.model.RepaymentStatus
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.theme.AlertOrange
import com.dailypay.app.ui.theme.AlertOrangeSubtle
import com.dailypay.app.ui.theme.BorderSubtleLight
import com.dailypay.app.ui.theme.BrandPrimary
import com.dailypay.app.ui.theme.CallBlue
import com.dailypay.app.ui.theme.DangerRed
import com.dailypay.app.ui.theme.MoneyGreen
import com.dailypay.app.ui.theme.MoneyGreenSubtle
import com.dailypay.app.ui.theme.TextSecondaryLight
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.UpiIntentLauncher
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerDashboardScreen(
    borrowerId: String,
    onPayEmiClick: () -> Unit = {},
    onApplyLoanClick: () -> Unit,
    onApprovedLoansClick: () -> Unit,
    onViewLedgerClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val borrowerRepo = remember { BorrowerRepository() }

    var borrowerProfile by remember { mutableStateOf<Borrower?>(null) }
    var lenderProfile by remember { mutableStateOf<Lender?>(null) }
    var activeLoans by remember { mutableStateOf<List<ApprovedLoanDetail>>(emptyList()) }
    var summary by remember { mutableStateOf<BorrowerDashboardSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploadingAvatar by remember { mutableStateOf(false) }

    // Payment Dialog States
    var showPayDialog by remember { mutableStateOf(false) }
    var selectedLoan by remember { mutableStateOf<ApprovedLoanDetail?>(null) }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.UPI) }
    var emiAmountInput by remember { mutableStateOf("") }
    var utrInput by remember { mutableStateOf("") }
    var cashNoteInput by remember { mutableStateOf("") }
    var showQrCode by remember { mutableStateOf(false) }
    var isSubmittingPayment by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            borrowerRepo.getBorrowerProfile(borrowerId).onSuccess { profile ->
                borrowerProfile = profile
                val cleanLenderId = profile.lenderId.trim()
                if (cleanLenderId.isNotBlank()) {
                    borrowerRepo.getLenderProfile(cleanLenderId).onSuccess { lender ->
                        lenderProfile = lender
                    }
                }
            }
            borrowerRepo.getCustomerApprovedLoans(borrowerId).onSuccess { loans ->
                activeLoans = loans.filter { it.remainingBalance > 0.0 }
            }
            borrowerRepo.getDashboardSummary(borrowerId)
                .onSuccess {
                    summary = it
                    isLoading = false
                }
                .onFailure { error ->
                    isLoading = false
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(borrowerId) {
        loadData()
    }

    fun openPaymentDialog() {
        if (activeLoans.isEmpty()) {
            Toast.makeText(context, "You have no active loans with pending balance.", Toast.LENGTH_SHORT).show()
            return
        }
        val loanToPay = activeLoans.firstOrNull { it.todayDue > 0.0 } ?: activeLoans.first()
        selectedLoan = loanToPay

        val defaultAmount = if (loanToPay.todayDue > 0.0) loanToPay.todayDue else loanToPay.dailyInstallment
        emiAmountInput = String.format(Locale.US, "%.2f", maxOf(0.0, defaultAmount))
        utrInput = ""
        cashNoteInput = ""
        selectedPaymentMode = PaymentMode.UPI
        showQrCode = false
        showPayDialog = true
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                try {
                    isUploadingAvatar = true
                    val bytes = context.contentResolver.openInputStream(selectedUri)?.use { stream ->
                        stream.readBytes()
                    }
                    if (bytes != null && bytes.isNotEmpty()) {
                        val mobile = borrowerProfile?.mobileNumber ?: "user"
                        borrowerRepo.updateProfilePicture(borrowerId, mobile, bytes)
                            .onSuccess { newUrl ->
                                borrowerProfile = borrowerProfile?.copy(profilePicUrl = newUrl)
                                isUploadingAvatar = false
                                Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .onFailure { err ->
                                isUploadingAvatar = false
                                Toast.makeText(context, "Upload failed: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        isUploadingAvatar = false
                    }
                } catch (e: Exception) {
                    isUploadingAvatar = false
                    Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val formattedTodayDue = remember(summary?.todayDue) {
        String.format(Locale.US, "%.2f", summary?.todayDue ?: 0.0)
    }
    val formattedTodayPaid = remember(summary?.todayPaid) {
        String.format(Locale.US, "%.2f", summary?.todayPaid ?: 0.0)
    }
    val formattedTotalBorrowed = remember(summary?.totalBorrowed) {
        String.format(Locale.US, "%.2f", summary?.totalBorrowed ?: 0.0)
    }
    val formattedTotalPaid = remember(summary?.totalPaid) {
        String.format(Locale.US, "%.2f", summary?.totalPaid ?: 0.0)
    }
    val formattedTotalRemaining = remember(summary?.totalRemaining) {
        String.format(Locale.US, "%.2f", summary?.totalRemaining ?: 0.0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Portal") },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = DangerRed)
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
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Avatar Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            if (!borrowerProfile?.profilePicUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = borrowerProfile?.profilePicUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, BrandPrimary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(68.dp),
                                    shape = CircleShape,
                                    color = BrandPrimary.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (!isUploadingAvatar) imagePickerLauncher.launch("image/*")
                                    },
                                color = BrandPrimary,
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isUploadingAvatar) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Change Photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = borrowerProfile?.name.orEmpty().ifBlank { "Customer" },
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "+91 ${borrowerProfile?.mobileNumber.orEmpty()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight
                            )
                            val location = listOfNotNull(
                                borrowerProfile?.villageCity,
                                borrowerProfile?.dist
                            ).filter { it.isNotBlank() }.joinToString(", ")
                            if (location.isNotBlank()) {
                                Text(
                                    text = "📍 $location",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }
                }

                // Today's Status Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, AlertOrange.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable { openPaymentDialog() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AlertOrangeSubtle)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Today's Due", style = MaterialTheme.typography.labelMedium, color = AlertOrange)
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = AlertOrange, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("₹$formattedTodayDue", style = MaterialTheme.typography.titleLarge, color = AlertOrange)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MoneyGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MoneyGreenSubtle)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Paid Today", style = MaterialTheme.typography.labelMedium, color = MoneyGreen)
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("₹$formattedTodayPaid", style = MaterialTheme.typography.titleLarge, color = MoneyGreen)
                        }
                    }
                }

                // Balance Breakdown
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("My Loan Overview", style = MaterialTheme.typography.titleMedium)
                            Text("${summary?.activeLoansCount ?: 0} Active Loans", style = MaterialTheme.typography.labelSmall, color = BrandPrimary)
                        }
                        HorizontalDivider(color = BorderSubtleLight)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Borrowed", color = TextSecondaryLight)
                            Text("₹$formattedTotalBorrowed", style = MaterialTheme.typography.titleMedium)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid", color = MoneyGreen)
                            Text("₹$formattedTotalPaid", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Remaining", color = DangerRed)
                            Text("₹$formattedTotalRemaining", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                        }
                    }
                }

                Text("Quick Actions", style = MaterialTheme.typography.titleMedium)

                CustomerActionCard(
                    title = "Pay EMI (UPI / Cash)",
                    subtitle = "Pay daily installment via UPI app, QR code, or submit cash request",
                    icon = Icons.Default.Payment,
                    accentColor = MoneyGreen,
                    onClick = { openPaymentDialog() }
                )

                CustomerActionCard(
                    title = "Approved Loans",
                    subtitle = "View loan details, tenure, start date & end date",
                    icon = Icons.Default.CheckCircle,
                    accentColor = BrandPrimary,
                    onClick = onApprovedLoansClick
                )

                CustomerActionCard(
                    title = "Apply For Loan",
                    subtitle = "Submit a new daily repayment loan request",
                    icon = Icons.Default.PostAdd,
                    accentColor = AlertOrange,
                    onClick = onApplyLoanClick
                )

                CustomerActionCard(
                    title = "My Ledger & Receipts",
                    subtitle = "Detailed date-by-date statement and receipts",
                    icon = Icons.Default.ReceiptLong,
                    accentColor = CallBlue,
                    onClick = onViewLedgerClick
                )
            }
        }

        // ================= REPAYMENT DIALOG (UPI + CASH) =================
        if (showPayDialog) {
            val lenderUpiId = lenderProfile?.upiId?.trim().orEmpty()
            val lenderMobile = lenderProfile?.mobileNumber?.trim().orEmpty()
            val lenderDisplayName = lenderProfile?.businessName?.ifBlank { lenderProfile?.name } ?: "DailyPay Lender"
            val parsedAmount = emiAmountInput.toDoubleOrNull() ?: 0.0
            val formattedAmount = String.format(Locale.US, "%.2f", maxOf(0.0, parsedAmount))

            // Build dynamic UPI QR Code Image URL
            val upiPayload = "upi://pay?pa=$lenderUpiId&pn=${Uri.encode(lenderDisplayName)}&am=$formattedAmount&cu=INR"
            val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${Uri.encode(upiPayload)}"

            AlertDialog(
                onDismissRequest = { if (!isSubmittingPayment) showPayDialog = false },
                title = { Text("Repay Daily EMI") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. Payment Method Switcher (UPI vs Cash)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedPaymentMode == PaymentMode.UPI,
                                onClick = { if (!isSubmittingPayment) selectedPaymentMode = PaymentMode.UPI },
                                label = { Text("Pay via UPI") },
                                leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedPaymentMode == PaymentMode.CASH,
                                onClick = { if (!isSubmittingPayment) selectedPaymentMode = PaymentMode.CASH },
                                label = { Text("Pay in Cash") },
                                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Amount Input
                        OutlinedTextField(
                            value = emiAmountInput,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    emiAmountInput = input
                                }
                            },
                            label = { Text("Installment Amount (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = !isSubmittingPayment
                        )

                        // ================= MODE A: UPI FLOW =================
                        if (selectedPaymentMode == PaymentMode.UPI) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Pay using Lender Details:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Option 1: Mobile Number for PhonePe/GPay/Paytm (Bypasses NPCI Risk Error)
                                    if (lenderMobile.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = CallBlue, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Mobile: +91 $lenderMobile", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            }
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(lenderMobile))
                                                    Toast.makeText(context, "Mobile number copied: $lenderMobile", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BrandPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    // Option 2: UPI ID / VPA
                                    if (lenderUpiId.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Payment, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("UPI ID: $lenderUpiId", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            }
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(lenderUpiId))
                                                    Toast.makeText(context, "UPI ID copied: $lenderUpiId", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BrandPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // QR Code / Intent Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showQrCode = !showQrCode },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = lenderUpiId.isNotBlank() && parsedAmount > 0.0
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (showQrCode) "Hide QR" else "Show QR")
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (lenderUpiId.isBlank()) {
                                            Toast.makeText(context, "Lender has not configured a UPI ID.", Toast.LENGTH_SHORT).show()
                                        } else if (parsedAmount <= 0.0) {
                                            Toast.makeText(context, "Enter amount first.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val currentLoanId = selectedLoan?.loanId.orEmpty().take(8)
                                            UpiIntentLauncher.initiateUpiPayment(
                                                context = context,
                                                payeeUpiId = lenderUpiId,
                                                payeeName = lenderDisplayName,
                                                amount = parsedAmount,
                                                transactionNote = "DailyPay EMI Loan #$currentLoanId"
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = lenderUpiId.isNotBlank() && parsedAmount > 0.0
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open UPI")
                                }
                            }

                            // Dynamic QR Code Display
                            if (showQrCode && lenderUpiId.isNotBlank() && parsedAmount > 0.0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AsyncImage(
                                            model = qrCodeUrl,
                                            contentDescription = "Scan to Pay UPI QR",
                                            modifier = Modifier
                                                .size(180.dp)
                                                .border(2.dp, BorderSubtleLight, RoundedCornerShape(12.dp))
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Scan using GPay, PhonePe or Paytm", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                    }
                                }
                            }

                            // NPCI Advisory Hint
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AlertOrangeSubtle
                            ) {
                                Text(
                                    text = "💡 Tip: If your UPI app shows 'NPCI Risk Policy', copy the Lender's Mobile Number above and pay directly inside GPay / PhonePe.",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AlertOrange
                                )
                            }

                            // 12-Digit UTR Input
                            OutlinedTextField(
                                value = utrInput,
                                onValueChange = { input ->
                                    val digitsOnly = input.filter { it.isDigit() }
                                    if (digitsOnly.length <= 12) {
                                        utrInput = digitsOnly
                                    }
                                },
                                label = { Text("12-Digit UPI Ref / UTR *") },
                                supportingText = { Text("${utrInput.length}/12 digits from receipt") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                enabled = !isSubmittingPayment
                            )
                        } else {
                            // ================= MODE B: CASH FLOW =================
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MoneyGreenSubtle
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Cash Handover Verification", fontWeight = FontWeight.Bold, color = MoneyGreen)
                                    Text(
                                        text = "Please physically hand over ₹$formattedAmount in cash to the lender or collection agent. Once the lender confirms receipt, your loan balance will be deducted.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = cashNoteInput,
                                onValueChange = { cashNoteInput = it },
                                label = { Text("Handover Note (Optional)") },
                                placeholder = { Text("e.g. Paid cash at shop / handed to agent") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 2,
                                enabled = !isSubmittingPayment
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (parsedAmount <= 0.0) {
                                Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val cleanUtr = utrInput.trim()
                            if (selectedPaymentMode == PaymentMode.UPI && cleanUtr.length != 12) {
                                Toast.makeText(context, "Please enter the 12-digit UTR from your UPI payment receipt.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSubmittingPayment = true
                            scope.launch {
                                val currentLoan = selectedLoan ?: activeLoans.firstOrNull()
                                val loanId = currentLoan?.loanId.orEmpty()
                                val cleanLenderId = borrowerProfile?.lenderId?.trim().orEmpty()

                                val paymentNotes = if (selectedPaymentMode == PaymentMode.UPI) {
                                    "Online UPI Payment. UTR: $cleanUtr"
                                } else {
                                    cashNoteInput.trim().ifBlank { "Cash handed over directly by borrower" }
                                }

                                val repayment = Repayment(
                                    loanId = loanId,
                                    borrowerId = borrowerId.trim(),
                                    lenderId = cleanLenderId,
                                    paymentDate = DateUtils.getTodaySqlFormat(),
                                    amountPaid = parsedAmount,
                                    paymentMode = selectedPaymentMode,
                                    status = RepaymentStatus.PENDING,
                                    utrReference = if (selectedPaymentMode == PaymentMode.UPI) cleanUtr else null,
                                    notes = paymentNotes
                                )

                                borrowerRepo.submitEmiPayment(repayment)
                                    .onSuccess {
                                        isSubmittingPayment = false
                                        showPayDialog = false
                                        val message = if (selectedPaymentMode == PaymentMode.UPI) {
                                            "UPI payment submitted with UTR! Awaiting lender approval."
                                        } else {
                                            "Cash payment request submitted! Lender will verify upon receiving cash."
                                        }
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        loadData()
                                    }
                                    .onFailure { error ->
                                        isSubmittingPayment = false
                                        Toast.makeText(context, "Failed: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        },
                        enabled = !isSubmittingPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        if (isSubmittingPayment) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submitting...")
                        } else {
                            Text(if (selectedPaymentMode == PaymentMode.UPI) "Submit UPI & UTR" else "Submit Cash Payment")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPayDialog = false },
                        enabled = !isSubmittingPayment
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun CustomerActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            }

            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondaryLight)
        }
    }
}