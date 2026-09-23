package com.dailypay.app.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.repository.AdminRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLenderScreen(
    onNavigateBack: () -> Unit,
    onAddNewLenderClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val adminRepo = remember { AdminRepository() }

    var lenders by remember { mutableStateOf<List<Lender>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedLenderForEdit by remember { mutableStateOf<Lender?>(null) }
    var selectedLenderForDelete by remember { mutableStateOf<Lender?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    fun loadLenders() {
        scope.launch {
            adminRepo.getAllLenders()
                .onSuccess {
                    lenders = it
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(Unit) {
        loadLenders()
    }

    val filteredList = lenders.filter { lender ->
        lender.name.contains(searchQuery, ignoreCase = true) ||
                (lender.businessName ?: "").contains(searchQuery, ignoreCase = true) ||
                (lender.ownerName ?: "").contains(searchQuery, ignoreCase = true) ||
                lender.mobileNumber.contains(searchQuery) ||
                (lender.upiId ?: "").contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Lenders (${lenders.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddNewLenderClick) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add New Lender", tint = BrandPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewLenderClick,
                containerColor = BrandPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Lender")
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
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name, business, mobile, UPI...") },
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
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No lenders found.", color = TextSecondaryLight)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredList, key = { it.id ?: it.mobileNumber }) { lender ->
                            val isRunning = lender.status.uppercase() == "RUN"
                            val displayName = (lender.businessName ?: "").ifBlank { lender.name }
                            val displayOwner = (lender.ownerName ?: "").ifBlank { lender.name }

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
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = CircleShape,
                                            color = if (isRunning) MoneyGreen.copy(alpha = 0.12f) else DangerRed.copy(alpha = 0.12f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Storefront,
                                                    contentDescription = null,
                                                    tint = if (isRunning) MoneyGreen else DangerRed,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = displayName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Prop: $displayOwner • +91 ${lender.mobileNumber}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isRunning) MoneyGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (isRunning) "RUN" else "STOP",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isRunning) MoneyGreen else DangerRed
                                            )
                                        }
                                    }

                                    if (!lender.upiId.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.AccountBalance,
                                                contentDescription = null,
                                                tint = TextSecondaryLight,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "UPI: ${lender.upiId}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight
                                            )
                                        }
                                    }

                                    val locationParts = listOfNotNull(
                                        lender.villageTown?.ifBlank { null },
                                        lender.dist?.ifBlank { null }
                                    )
                                    if (locationParts.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = TextSecondaryLight,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = locationParts.joinToString(", "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { selectedLenderForEdit = lender },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Edit")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedButton(
                                            onClick = { selectedLenderForDelete = lender },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = DangerRed)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= EDIT LENDER DIALOG =================
        selectedLenderForEdit?.let { lender ->
            var editName by remember { mutableStateOf(lender.name) }
            var editOwnerName by remember { mutableStateOf(lender.ownerName ?: "") }
            var editBusinessName by remember { mutableStateOf(lender.businessName ?: "") }
            var editMobile by remember { mutableStateOf(lender.mobileNumber) }
            var editUpiId by remember { mutableStateOf(lender.upiId ?: "") }
            var editVillage by remember { mutableStateOf(lender.villageTown ?: "") }
            var editPostOffice by remember { mutableStateOf(lender.postOffice ?: "") }
            var editDist by remember { mutableStateOf(lender.dist ?: "") }
            var editAddress by remember { mutableStateOf(lender.address ?: "") }
            var editStatus by remember { mutableStateOf(lender.status.uppercase()) }
            var editPassword by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { if (!isSubmitting) selectedLenderForEdit = null },
                title = { Text("Edit Lender Details") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editBusinessName,
                            onValueChange = { editBusinessName = it },
                            label = { Text("Business / Store Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editOwnerName,
                            onValueChange = { editOwnerName = it },
                            label = { Text("Proprietor / Owner Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editMobile,
                            onValueChange = { editMobile = it.filter { ch -> ch.isDigit() }.take(10) },
                            label = { Text("Mobile Number (10 digits) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editUpiId,
                            onValueChange = { editUpiId = it },
                            label = { Text("UPI ID (e.g. name@upi)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editVillage,
                            onValueChange = { editVillage = it },
                            label = { Text("Village / Town") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editPostOffice,
                            onValueChange = { editPostOffice = it },
                            label = { Text("Post Office") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editDist,
                            onValueChange = { editDist = it },
                            label = { Text("District") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text("Complete Address") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Account Status:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = editStatus == "RUN",
                                onClick = { editStatus = "RUN" },
                                label = { Text("RUN (Active)") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = editStatus == "STOP",
                                onClick = { editStatus = "STOP" },
                                label = { Text("STOP (Suspended)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("New Password (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editName.isBlank()) {
                                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (editMobile.length < 10) {
                                Toast.makeText(context, "Enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val updatedLender = lender.copy(
                                name = editName.trim(),
                                ownerName = editOwnerName.trim().ifBlank { null },
                                businessName = editBusinessName.trim().ifBlank { null },
                                mobileNumber = editMobile.trim(),
                                upiId = editUpiId.trim().ifBlank { null },
                                villageTown = editVillage.trim().ifBlank { null },
                                postOffice = editPostOffice.trim().ifBlank { null },
                                dist = editDist.trim().ifBlank { null },
                                address = editAddress.trim().ifBlank { null },
                                status = editStatus,
                                passwordHash = if (editPassword.isNotBlank()) editPassword.trim() else lender.passwordHash
                            )

                            isSubmitting = true
                            scope.launch {
                                adminRepo.updateLender(updatedLender)
                                    .onSuccess {
                                        isSubmitting = false
                                        selectedLenderForEdit = null
                                        Toast.makeText(context, "Lender updated successfully!", Toast.LENGTH_SHORT).show()
                                        loadLenders()
                                    }
                                    .onFailure {
                                        isSubmitting = false
                                        Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isSubmitting) "Saving..." else "Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedLenderForEdit = null }, enabled = !isSubmitting) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ================= DELETE CONFIRMATION DIALOG =================
        selectedLenderForDelete?.let { lender ->
            AlertDialog(
                onDismissRequest = { if (!isSubmitting) selectedLenderForDelete = null },
                title = { Text("Delete Lender") },
                text = {
                    val displayName = (lender.businessName ?: "").ifBlank { lender.name }
                    Text("Are you sure you want to delete '$displayName'? All associated lender records will be affected.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val lenderId = lender.id
                            if (lenderId.isNullOrBlank()) {
                                selectedLenderForDelete = null
                                return@Button
                            }

                            isSubmitting = true
                            scope.launch {
                                adminRepo.deleteLender(lenderId)
                                    .onSuccess {
                                        isSubmitting = false
                                        selectedLenderForDelete = null
                                        Toast.makeText(context, "Lender deleted successfully", Toast.LENGTH_SHORT).show()
                                        loadLenders()
                                    }
                                    .onFailure {
                                        isSubmitting = false
                                        Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text(if (isSubmitting) "Deleting..." else "Confirm Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedLenderForDelete = null }, enabled = !isSubmitting) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
                           
