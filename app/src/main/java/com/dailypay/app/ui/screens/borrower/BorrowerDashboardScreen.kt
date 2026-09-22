package com.dailypay.app.ui.screens.borrower

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.model.BorrowerDashboardSummary
import com.dailypay.app.data.repository.BorrowerRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerDashboardScreen(
    borrowerId: String,
    onPayEmiClick: () -> Unit,
    onApplyLoanClick: () -> Unit,
    onApprovedLoansClick: () -> Unit,
    onViewLedgerClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val borrowerRepo = remember { BorrowerRepository() }

    var borrowerProfile by remember { mutableStateOf<Borrower?>(null) }
    var summary by remember { mutableStateOf<BorrowerDashboardSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploadingAvatar by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            borrowerRepo.getBorrowerProfile(borrowerId).onSuccess { borrowerProfile = it }
            borrowerRepo.getDashboardSummary(borrowerId)
                .onSuccess {
                    summary = it
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(borrowerId) {
        loadData()
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
                                text = borrowerProfile?.name ?: "Customer",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "+91 ${borrowerProfile?.mobileNumber ?: ""}",
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
                            .border(1.dp, AlertOrange.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
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
                            Text("₹${summary?.todayDue ?: 0.0}", style = MaterialTheme.typography.titleLarge, color = AlertOrange)
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
                            Text("₹${summary?.todayPaid ?: 0.0}", style = MaterialTheme.typography.titleLarge, color = MoneyGreen)
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
                            Text("₹${summary?.totalBorrowed ?: 0.0}", style = MaterialTheme.typography.titleMedium)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid", color = MoneyGreen)
                            Text("₹${summary?.totalPaid ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Remaining", color = DangerRed)
                            Text("₹${summary?.totalRemaining ?: 0.0}", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                        }
                    }
                }

                Text("Quick Actions", style = MaterialTheme.typography.titleMedium)

                CustomerActionCard(
                    title = "Pay EMI",
                    subtitle = "Pay today's installment due via UPI or Cash",
                    icon = Icons.Default.Payment,
                    accentColor = MoneyGreen,
                    onClick = onPayEmiClick
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
