@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.model.PaymentMode
import com.dailypay.app.data.model.Repayment
import com.dailypay.app.data.remote.SupabaseClientProvider
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.AlertOrange
import com.dailypay.app.ui.theme.BorderSubtleLight
import com.dailypay.app.ui.theme.BrandPrimary
import com.dailypay.app.ui.theme.CallBlue
import com.dailypay.app.ui.theme.DangerRed
import com.dailypay.app.ui.theme.MoneyGreen
import com.dailypay.app.ui.theme.TextSecondaryLight
import com.dailypay.app.ui.theme.WhatsAppGreen
import com.dailypay.app.util.CommunicationUtils
import com.dailypay.app.util.DateUtils
import com.dailypay.app.util.SessionManager
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LenderHomeScreen(
    lenderId: String,
    onNavigateToDashboard: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository(context.applicationContext) }
    val sessionManager = remember { SessionManager(context) }

    // Fallback: if argument was empty, grab from SessionManager
    val resolvedLenderId = remember(lenderId) {
        if (lenderId.isNotBlank() && lenderId != "{lenderId}") {
            lenderId.trim()
        } else {
            sessionManager.getUserId()?.trim().orEmpty()
        }
    }

    var lenderProfile by remember { mutableStateOf<Lender?>(null) }
    var duesList by remember { mutableStateOf<List<DailyDueItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showAddressDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }

    var selectedItemForPayment by remember { mutableStateOf<DailyDueItem?>(null) }
    var paymentAmountText by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var isSubmittingPayment by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val currentDateText = remember { DateUtils.getTodayDisplayFormat() }

    fun loadData() {
        if (resolvedLenderId.isBlank() || resolvedLenderId == "{lenderId}") {
            isLoading = false
            onLogoutClick()
            return
        }

        scope.launch {
            lenderRepo.getLenderProfile(resolvedLenderId).onSuccess { lenderProfile = it }
            lenderRepo.getDailyDues(resolvedLenderId)
                .onSuccess {
                    duesList = it
                    isLoading = false
                }
                .onFailure { error ->
                    isLoading = false
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(resolvedLenderId) {
        loadData()
    }

    val filteredList = duesList.filter { item ->
        item.borrowerName.contains(searchQuery, ignoreCase = true) ||
                item.borrowerMobile.contains(searchQuery)
    }

    val pendingDues = filteredList.filter { it.todayDueBalance > 0.0 }
    val paidDues = filteredList.filter { it.todayPaidAmount > 0.0 }

    val businessName = lenderProfile?.businessName?.ifBlank { lenderProfile?.name } ?: "DailyPay Finance"
    val ownerName = lenderProfile?.ownerName?.ifBlank { lenderProfile?.name } ?: "Lender"
    val currentUpiId = lenderProfile?.upiId?.trim().orEmpty()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BrandPrimary
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = businessName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Proprietor: $ownerName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "+91 ${lenderProfile?.mobileNumber ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Receiving UPI status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = if (currentUpiId.isNotBlank()) "UPI: $currentUpiId" else "UPI: Not Configured",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.EditLocation, contentDescription = null, tint = BrandPrimary) },
                    label = { Text("Edit Profile & UPI ID") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showAddressDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = AlertOrange) },
                    label = { Text("Reset Password") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showResetPasswordDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp), color = BorderSubtleLight)

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = DangerRed) },
                    label = { Text("Logout", color = DangerRed, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogoutClick()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = businessName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Proprietor: $ownerName",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Main Menu", tint = BrandPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToDashboard) {
                            Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = BrandPrimary)
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
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search client name or mobile...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = BrandPrimary
                    ) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Pending",
                                        fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Medium,
                                        color = if (pagerState.currentPage == 0) BrandPrimary else TextSecondaryLight
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = if (pagerState.currentPage == 0) AlertOrange else AlertOrange.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${pendingDues.size}",
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (pagerState.currentPage == 0) Color.White else AlertOrange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        )

                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Paid Today",
                                        fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (pagerState.currentPage == 1) BrandPrimary else TextSecondaryLight
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = if (pagerState.currentPage == 1) MoneyGreen else MoneyGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${paidDues.size}",
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (pagerState.currentPage == 1) Color.White else MoneyGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        )
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { pageIndex ->
                        when (pageIndex) {
                            0 -> PendingListTab(
                                pendingList = pendingDues,
                                currentDateStr = currentDateText,
                                onCollectClick = { item ->
                                    selectedItemForPayment = item
                                    paymentAmountText = String.format(Locale.US, "%.2f", item.todayDueBalance)
                                    selectedPaymentMode = PaymentMode.CASH
                                }
                            )
                            1 -> PaidTodayListTab(
                                paidList = paidDues,
                                businessName = businessName,
                                currentDateStr = currentDateText
                            )
                        }
                    }
                }
            }

            // ================= COLLECT PAYMENT DIALOG =================
            selectedItemForPayment?.let { item ->
                val formattedDue = String.format(Locale.US, "%.2f", maxOf(0.0, item.todayDueBalance))

                AlertDialog(
                    onDismissRequest = { if (!isSubmittingPayment) selectedItemForPayment = null },
                    title = { Text("Collect EMI Payment") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Borrower: ${item.borrowerName.ifBlank { "Customer" }}")
                            Text(
                                text = "Today's Due: ₹$formattedDue",
                                fontWeight = FontWeight.Bold,
                                color = AlertOrange
                            )

                            OutlinedTextField(
                                value = paymentAmountText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                        paymentAmountText = input
                                    }
                                },
                                label = { Text("Amount Collected (₹) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isSubmittingPayment
                            )

                            Text("Payment Method:", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedPaymentMode == PaymentMode.CASH,
                                    onClick = { if (!isSubmittingPayment) selectedPaymentMode = PaymentMode.CASH },
                                    label = { Text("Cash") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = selectedPaymentMode == PaymentMode.UPI,
                                    onClick = { if (!isSubmittingPayment) selectedPaymentMode = PaymentMode.UPI },
                                    label = { Text("UPI Received") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amount = paymentAmountText.toDoubleOrNull() ?: 0.0
                                if (amount <= 0.0) {
                                    Toast.makeText(context, "Enter a valid payment amount.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isSubmittingPayment = true
                                scope.launch {
                                    val repayment = Repayment(
                                        loanId = item.loanId.trim(),
                                        borrowerId = item.borrowerId.trim(),
                                        lenderId = resolvedLenderId,
                                        amountPaid = amount,
                                        paymentDate = DateUtils.getTodaySqlFormat(),
                                        paymentMode = selectedPaymentMode,
                                        notes = "Collected on $currentDateText (${selectedPaymentMode.name})"
                                    )
                                    lenderRepo.recordRepayment(repayment)
                                        .onSuccess {
                                            isSubmittingPayment = false
                                            selectedItemForPayment = null
                                            Toast.makeText(context, "Payment recorded successfully!", Toast.LENGTH_SHORT).show()
                                            loadData()
                                        }
                                        .onFailure { error ->
                                            isSubmittingPayment = false
                                            Toast.makeText(context, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
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
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Saving...")
                            } else {
                                Text("Confirm Payment")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedItemForPayment = null }, enabled = !isSubmittingPayment) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // ================= EDIT ADDRESS & UPI ID DIALOG =================
            if (showAddressDialog) {
                var editUpiId by remember { mutableStateOf(lenderProfile?.upiId ?: "") }
                var editVillage by remember { mutableStateOf(lenderProfile?.villageTown ?: "") }
                var editPostOffice by remember { mutableStateOf(lenderProfile?.postOffice ?: "") }
                var editDist by remember { mutableStateOf(lenderProfile?.dist ?: "") }
                var editAddress by remember { mutableStateOf(lenderProfile?.address ?: "") }
                var isSavingAddr by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { if (!isSavingAddr) showAddressDialog = false },
                    title = { Text("Business Profile & Receiving UPI") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editUpiId,
                                onValueChange = { editUpiId = it },
                                label = { Text("Receiving UPI ID / VPA *") },
                                placeholder = { Text("e.g. mobile@upi, business@okhdfcbank") },
                                supportingText = { Text("Borrowers will send online EMI payments to this UPI address") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            OutlinedTextField(
                                value = editVillage,
                                onValueChange = { editVillage = it },
                                label = { Text("Village / Town") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editPostOffice,
                                onValueChange = { editPostOffice = it },
                                label = { Text("Post Office") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editDist,
                                onValueChange = { editDist = it },
                                label = { Text("District") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editAddress,
                                onValueChange = { editAddress = it },
                                label = { Text("Complete Address") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isSavingAddr = true
                                scope.launch {
                                    try {
                                        // 1. Update address fields via lenderRepo
                                        lenderRepo.updateLenderAddress(
                                            lenderId = resolvedLenderId,
                                            villageTown = editVillage.trim().ifBlank { null },
                                            postOffice = editPostOffice.trim().ifBlank { null },
                                            dist = editDist.trim().ifBlank { null },
                                            address = editAddress.trim().ifBlank { null }
                                        ).getOrThrow()

                                        // 2. Persist receiving UPI ID to Supabase
                                        val cleanUpi = editUpiId.trim()
                                        withContext(Dispatchers.IO) {
                                            SupabaseClientProvider.db
                                                .from("lenders")
                                                .update(
                                                    buildJsonObject {
                                                        put("upi_id", cleanUpi.ifBlank { null })
                                                    }
                                                ) {
                                                    filter { eq("id", resolvedLenderId) }
                                                }
                                        }

                                        isSavingAddr = false
                                        showAddressDialog = false
                                        Toast.makeText(context, "Profile and UPI ID updated successfully!", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    } catch (e: Exception) {
                                        isSavingAddr = false
                                        Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isSavingAddr,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                        ) {
                            if (isSavingAddr) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Saving...")
                            } else {
                                Text("Save Profile")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddressDialog = false }, enabled = !isSavingAddr) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // ================= RESET PASSWORD DIALOG =================
            if (showResetPasswordDialog) {
                var newPassword by remember { mutableStateOf("") }
                var confirmPassword by remember { mutableStateOf("") }
                var isSavingPwd by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { if (!isSavingPwd) showResetPasswordDialog = false },
                    title = { Text("Reset Login Password") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New Password (min 6 chars) *") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm New Password *") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newPassword.trim().length < 6) {
                                    Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (newPassword.trim() != confirmPassword.trim()) {
                                    Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isSavingPwd = true
                                scope.launch {
                                    lenderRepo.updateLenderPassword(resolvedLenderId, newPassword.trim())
                                        .onSuccess {
                                            isSavingPwd = false
                                            showResetPasswordDialog = false
                                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                        .onFailure { error ->
                                            isSavingPwd = false
                                            Toast.makeText(context, "Reset failed: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            enabled = !isSavingPwd,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                        ) {
                            Text(if (isSavingPwd) "Saving..." else "Update Password")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetPasswordDialog = false }, enabled = !isSavingPwd) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

// ----------------- TAB 0: PENDING DUES -----------------
@Composable
private fun PendingListTab(
    pendingList: List<DailyDueItem>,
    currentDateStr: String,
    onCollectClick: (DailyDueItem) -> Unit
) {
    val context = LocalContext.current

    if (pendingList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No pending dues for today!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("All scheduled collections are cleared.", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = pendingList,
                key = { item -> "${item.loanId}_${item.borrowerId}" }
            ) { item ->
                val hasOverdue = item.todayDueBalance > item.dailyInstallment
                val formattedTodayDue = remember(item.todayDueBalance) {
                    String.format(Locale.US, "%.2f", maxOf(0.0, item.todayDueBalance))
                }
                val formattedInstallment = remember(item.dailyInstallment) {
                    String.format(Locale.US, "%.2f", item.dailyInstallment)
                }
                val formattedRemaining = remember(item.remainingBalance) {
                    String.format(Locale.US, "%.2f", maxOf(0.0, item.remainingBalance))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = AlertOrange.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = AlertOrange, modifier = Modifier.size(24.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.borrowerName.ifBlank { "Customer" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("+91 ${item.borrowerMobile}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.background
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = TextSecondaryLight
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = currentDateStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondaryLight
                                            )
                                        }
                                    }

                                    if (hasOverdue) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = DangerRed.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "Includes Overdue",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = DangerRed,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = {
                                        try {
                                            CommunicationUtils.openWhatsAppChat(
                                                context = context,
                                                rawMobileNumber = item.borrowerMobile,
                                                message = "Hello ${item.borrowerName}, your daily installment for $currentDateStr of ₹$formattedTodayDue is due today."
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, WhatsAppGreen.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.ic_whatsapp), contentDescription = "WhatsApp", tint = WhatsAppGreen, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = {
                                        try {
                                            CommunicationUtils.openPhoneDialer(context, item.borrowerMobile)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, CallBlue.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.ic_call), contentDescription = "Call", tint = CallBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Today Due", style = MaterialTheme.typography.labelSmall, color = AlertOrange)
                                Text("₹$formattedTodayDue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AlertOrange)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Daily EMI", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                Text("₹$formattedInstallment", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Balance Left", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                Text("₹$formattedRemaining", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { onCollectClick(item) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Collect")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- TAB 1: PAID TODAY WITH DIGITAL RECEIPT -----------------
@Composable
private fun PaidTodayListTab(
    paidList: List<DailyDueItem>,
    businessName: String,
    currentDateStr: String
) {
    val context = LocalContext.current

    if (paidList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = TextSecondaryLight, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No payments cleared today yet.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Approved payments will show here.", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = paidList,
                key = { item -> "${item.loanId}_${item.borrowerId}" }
            ) { item ->
                val formattedPaidToday = remember(item.todayPaidAmount) {
                    String.format(Locale.US, "%.2f", item.todayPaidAmount)
                }
                val formattedTotalPaid = remember(item.totalPaid) {
                    String.format(Locale.US, "%.2f", item.totalPaid)
                }
                val formattedRemaining = remember(item.remainingBalance) {
                    String.format(Locale.US, "%.2f", maxOf(0.0, item.remainingBalance))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = MoneyGreen.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(24.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.borrowerName.ifBlank { "Customer" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("+91 ${item.borrowerMobile}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MoneyGreen.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MoneyGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Paid: $currentDateStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MoneyGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = {
                                        try {
                                            val currentDateTime = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.US).format(Date())
                                            val receiptText = """
                                                🧾 *PAYMENT RECEIPT*
                                                🏪 *${businessName.uppercase(Locale.US)}*
                                                📅 Date: $currentDateTime

                                                👤 Customer: ${item.borrowerName}
                                                📱 Mobile: +91 ${item.borrowerMobile}

                                                ━━━━━━━━━━━━━━━━━━━
                                                💵 *Amount Paid Today: ₹$formattedPaidToday*
                                                ✅ Total Recovered: ₹$formattedTotalPaid
                                                ⚠️ Remaining Balance: ₹$formattedRemaining
                                                ━━━━━━━━━━━━━━━━━━━

                                                Status: VERIFIED & CLEARED ✅
                                                Thank you for your timely repayment!
                                            """.trimIndent()

                                            CommunicationUtils.openWhatsAppChat(
                                                context = context,
                                                rawMobileNumber = item.borrowerMobile,
                                                message = receiptText
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, WhatsAppGreen.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.ic_whatsapp), contentDescription = "Send WhatsApp Receipt", tint = WhatsAppGreen, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = {
                                        try {
                                            CommunicationUtils.openPhoneDialer(context, item.borrowerMobile)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, CallBlue.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.ic_call), contentDescription = "Call", tint = CallBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Paid Today", style = MaterialTheme.typography.labelSmall, color = MoneyGreen)
                                Text("₹$formattedPaidToday", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MoneyGreen)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Recovered", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                Text("₹$formattedTotalPaid", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining Bal", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                Text(
                                    text = "₹$formattedRemaining",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.remainingBalance > 0.0) DangerRed else MoneyGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}