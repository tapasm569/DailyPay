package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils
import kotlinx.coroutines.launch

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterClientScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    onAddNewBorrowerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var borrowers by remember { mutableStateOf<List<Borrower>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedBorrowerForDetails by remember { mutableStateOf<Borrower?>(null) }
    var borrowerToEdit by remember { mutableStateOf<Borrower?>(null) }
    var borrowerToDelete by remember { mutableStateOf<Borrower?>(null) }
    var isProcessingAction by remember { mutableStateOf(false) }

    fun loadBorrowers() {
        scope.launch {
            lenderRepo.getBorrowers(lenderId)
                .onSuccess {
                    borrowers = it
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(lenderId) {
        loadBorrowers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Master Client List (${borrowers.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewBorrowerClick,
                containerColor = BrandPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Borrower")
            }
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
        } else if (borrowers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No registered borrowers found.", color = TextSecondaryLight)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(borrowers, key = { it.id ?: it.mobileNumber }) { borrower ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp))
                            .clickable { selectedBorrowerForDetails = borrower },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = borrower.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Manage",
                                        tint = TextSecondaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "+91 ${borrower.mobileNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryLight
                                )

                                val location = listOfNotNull(borrower.villageCity, borrower.dist)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                                if (location.isNotBlank()) {
                                    Text(
                                        text = location,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = {
                                        CommunicationUtils.openWhatsAppChat(
                                            context = context,
                                            rawMobileNumber = borrower.mobileNumber,
                                            message = "Hello ${borrower.name}, contacting you from DailyPay."
                                        )
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, WhatsAppGreen.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_whatsapp),
                                        contentDescription = "WhatsApp",
                                        tint = WhatsAppGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { CommunicationUtils.openPhoneDialer(context, borrower.mobileNumber) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, CallBlue.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_call),
                                        contentDescription = "Call",
                                        tint = CallBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. View Profile Modal
        selectedBorrowerForDetails?.let { borrower ->
            AlertDialog(
                onDismissRequest = { selectedBorrowerForDetails = null },
                confirmButton = {
                    TextButton(onClick = { selectedBorrowerForDetails = null }) {
                        Text("Close")
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Borrower Profile", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { selectedBorrowerForDetails = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = BrandPrimary.copy(alpha = 0.08f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = borrower.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = BrandPrimary
                                )
                                Text(
                                    text = "Mobile: +91 ${borrower.mobileNumber}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtleLight, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProfileRow("Village / City", borrower.villageCity ?: "-")
                                HorizontalDivider(color = BorderSubtleLight)
                                ProfileRow("Post Office", borrower.postOffice ?: "-")
                                HorizontalDivider(color = BorderSubtleLight)
                                ProfileRow("Police Station", borrower.policeStation ?: "-")
                                HorizontalDivider(color = BorderSubtleLight)
                                ProfileRow("District", borrower.dist ?: "-")
                                HorizontalDivider(color = BorderSubtleLight)
                                ProfileRow("Password", borrower.passwordHash)
                            }
                        }

                        Text("KYC Documents Attached:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(if (!borrower.aadhaarCardUrl.isNullOrBlank()) "Aadhaar ✓" else "No Aadhaar") }
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(if (!borrower.panCardUrl.isNullOrBlank()) "PAN ✓" else "No PAN") }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    borrowerToEdit = borrower
                                    selectedBorrowerForDetails = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Update")
                            }

                            Button(
                                onClick = {
                                    borrowerToDelete = borrower
                                    selectedBorrowerForDetails = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            )
        }

        // 2. Update Profile Modal
        borrowerToEdit?.let { borrower ->
            var editName by remember { mutableStateOf(borrower.name) }
            var editMobile by remember { mutableStateOf(borrower.mobileNumber) }
            var editVillage by remember { mutableStateOf(borrower.villageCity ?: "") }
            var editPostOffice by remember { mutableStateOf(borrower.postOffice ?: "") }
            var editPoliceStation by remember { mutableStateOf(borrower.policeStation ?: "") }
            var editDist by remember { mutableStateOf(borrower.dist ?: "") }
            var editPassword by remember { mutableStateOf(borrower.passwordHash) }

            AlertDialog(
                onDismissRequest = { if (!isProcessingAction) borrowerToEdit = null },
                title = { Text("Update Borrower Profile") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = editMobile,
                            onValueChange = { if (it.length <= 10) editMobile = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Mobile Number *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editVillage,
                                onValueChange = { editVillage = it },
                                label = { Text("Village/City") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = editPostOffice,
                                onValueChange = { editPostOffice = it },
                                label = { Text("Post Office") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editPoliceStation,
                                onValueChange = { editPoliceStation = it },
                                label = { Text("Police Station") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = editDist,
                                onValueChange = { editDist = it },
                                label = { Text("District") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("Password *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editName.isBlank() || editMobile.length != 10 || editPassword.isBlank()) {
                                Toast.makeText(context, "Please fill required fields properly.", Toast.LENGTH_SHORT).show()
                            } else {
                                isProcessingAction = true
                                scope.launch {
                                    val updated = borrower.copy(
                                        name = editName.trim(),
                                        mobileNumber = editMobile.trim(),
                                        villageCity = editVillage.trim(),
                                        postOffice = editPostOffice.trim(),
                                        policeStation = editPoliceStation.trim(),
                                        dist = editDist.trim(),
                                        passwordHash = editPassword.trim()
                                    )

                                    lenderRepo.updateBorrower(updated)
                                        .onSuccess {
                                            isProcessingAction = false
                                            borrowerToEdit = null
                                            Toast.makeText(context, "Borrower profile updated successfully!", Toast.LENGTH_SHORT).show()
                                            loadBorrowers()
                                        }
                                        .onFailure {
                                            isProcessingAction = false
                                            Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        },
                        enabled = !isProcessingAction,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isProcessingAction) "Saving..." else "Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { borrowerToEdit = null },
                        enabled = !isProcessingAction
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // 3. Delete Confirmation Modal
        borrowerToDelete?.let { borrower ->
            AlertDialog(
                onDismissRequest = { if (!isProcessingAction) borrowerToDelete = null },
                title = { Text("Delete Borrower?") },
                text = {
                    Text(
                        text = "Are you sure you want to permanently delete \"${borrower.name}\" (+91 ${borrower.mobileNumber})? Note: Borrowers with active loans or payment history cannot be deleted until those records are cleared.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val bId = borrower.id
                            if (!bId.isNullOrBlank()) {
                                isProcessingAction = true
                                scope.launch {
                                    lenderRepo.deleteBorrower(bId)
                                        .onSuccess {
                                            isProcessingAction = false
                                            borrowerToDelete = null
                                            Toast.makeText(context, "Borrower deleted successfully.", Toast.LENGTH_SHORT).show()
                                            loadBorrowers()
                                        }
                                        .onFailure { error ->
                                            isProcessingAction = false
                                            val errorMsg = if (error.message?.contains("violates foreign key constraint", ignoreCase = true) == true) {
                                                "Cannot delete borrower with existing loans or repayments. Remove their loans first."
                                            } else {
                                                error.message ?: "Deletion failed"
                                            }
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                        }
                                }
                            }
                        },
                        enabled = !isProcessingAction,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text(if (isProcessingAction) "Deleting..." else "Confirm Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { borrowerToDelete = null },
                        enabled = !isProcessingAction
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}