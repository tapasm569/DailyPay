package com.dailypay.app.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.repository.AdminRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLenderScreen(
    onNavigateBack: () -> Unit,
    onAddNewLenderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val adminRepo = remember { AdminRepository() }

    var lendersList by remember { mutableStateOf<List<Lender>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog States
    var selectedLenderForEdit by remember { mutableStateOf<Lender?>(null) }
    var selectedLenderForPasswordReset by remember { mutableStateOf<Lender?>(null) }
    var lenderToDelete by remember { mutableStateOf<Lender?>(null) }

    fun loadData() {
        scope.launch {
            adminRepo.getAllLenders()
                .onSuccess {
                    lendersList = it
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val filteredLenders = lendersList.filter { lender ->
        lender.businessName.contains(searchQuery, ignoreCase = true) ||
                lender.ownerName.contains(searchQuery, ignoreCase = true) ||
                lender.name.contains(searchQuery, ignoreCase = true) ||
                lender.mobileNumber.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Lenders (${filteredLenders.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddNewLenderClick) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Lender", tint = BrandPrimary)
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, business or mobile...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (filteredLenders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No lenders registered yet." else "No matching lenders found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondaryLight
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(filteredLenders, key = { index, item -> item.id ?: "lender_$index" }) { _, lender ->
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
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = lender.businessName.ifBlank { lender.name },
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Owner: ${lender.ownerName.ifBlank { lender.name }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight
                                            )
                                            Text(
                                                text = "+91 ${lender.mobileNumber}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = BrandPrimary
                                            )
                                        }

                                        // Actions: WhatsApp & Call
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(
                                                onClick = {
                                                    CommunicationUtils.openWhatsAppChat(
                                                        context = context,
                                                        rawMobileNumber = lender.mobileNumber,
                                                        message = "Hello ${lender.name}, DailyPay Admin notification:"
                                                    )
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, WhatsAppGreen.copy(alpha = 0.3f), CircleShape)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                                    contentDescription = "WhatsApp",
                                                    tint = WhatsAppGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { CommunicationUtils.openPhoneDialer(context, lender.mobileNumber) },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, CallBlue.copy(alpha = 0.3f), CircleShape)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_call),
                                                    contentDescription = "Call",
                                                    tint = CallBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Location & Details
                                    val location = listOfNotNull(lender.villageTown, lender.postOffice, lender.dist)
                                        .filter { it.isNotBlank() }
                                        .joinToString(", ")
                                    if (location.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "📍 $location",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryLight
                                        )
                                    }

                                    if (!lender.upiId.isNullOrBlank()) {
                                        Text(
                                            text = "UPI: ${lender.upiId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MoneyGreen
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderSubtleLight)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Management Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { selectedLenderForEdit = lender },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit")
                                        }

                                        OutlinedButton(
                                            onClick = { selectedLenderForPasswordReset = lender },
                                            modifier = Modifier.weight(1.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Password")
                                        }

                                        IconButton(
                                            onClick = { lenderToDelete = lender }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
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
            var editOwnerName by remember { mutableStateOf(lender.ownerName) }
            var editBusinessName by remember { mutableStateOf(lender.businessName) }
            var editMobile by remember { mutableStateOf(lender.mobileNumber) }
            var editVillageTown by remember { mutableStateOf(lender.villageTown ?: "") }
            var editPostOffice by remember { mutableStateOf(lender.postOffice ?: "") }
            var editDist by remember { mutableStateOf(lender.dist ?: "") }
            var editUpiId by remember { mutableStateOf(lender.upiId ?: "") }
            var isSaving by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { if (!isSaving) selectedLenderForEdit = null },
                title = { Text("Edit Lender Details") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Lender Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editBusinessName,
                            onValueChange = { editBusinessName = it },
                            label = { Text("Business Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editOwnerName,
                            onValueChange = { editOwnerName = it },
                            label = { Text("Owner Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editMobile,
                            onValueChange = { editMobile = it.filter { ch -> ch.isDigit() }.take(10) },
                            label = { Text("Mobile Number (10 Digits) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editVillageTown,
                            onValueChange = { editVillageTown = it },
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
                            value = editUpiId,
                            onValueChange = { editUpiId = it },
                            label = { Text("UPI ID (for payments)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editName.isBlank() || editMobile.length != 10) {
                                Toast.makeText(context, "Enter a valid name and 10-digit mobile number.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSaving = true
                            scope.launch {
                                val updated = lender.copy(
                                    name = editName.trim(),
                                    ownerName = editOwnerName.trim(),
                                    businessName = editBusinessName.trim(),
                                    mobileNumber = editMobile.trim(),
                                    villageTown = editVillageTown.trim().ifBlank { null },
                                    postOffice = editPostOffice.trim().ifBlank { null },
                                    dist = editDist.trim().ifBlank { null },
                                    upiId = editUpiId.trim().ifBlank { null }
                                )
                                adminRepo.updateLender(updated)
                                    .onSuccess {
                                        isSaving = false
                                        selectedLenderForEdit = null
                                        Toast.makeText(context, "Lender updated successfully!", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure {
                                        isSaving = false
                                        Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isSaving) "Saving..." else "Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedLenderForEdit = null }, enabled = !isSaving) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ================= RESET PASSWORD DIALOG =================
        selectedLenderForPasswordReset?.let { lender ->
            var newPassword by remember { mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(false) }
            var isUpdating by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { if (!isUpdating) selectedLenderForPasswordReset = null },
                title = { Text("Reset Password") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Set a new password for ${lender.name} (+91 ${lender.mobileNumber})")
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password *") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPassword.length < 4) {
                                Toast.makeText(context, "Password must be at least 4 characters.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isUpdating = true
                            scope.launch {
                                val updated = lender.copy(passwordHash = newPassword.trim())
                                adminRepo.updateLender(updated)
                                    .onSuccess {
                                        isUpdating = false
                                        selectedLenderForPasswordReset = null
                                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure {
                                        isUpdating = false
                                        Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isUpdating,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isUpdating) "Updating..." else "Reset Password")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedLenderForPasswordReset = null }, enabled = !isUpdating) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ================= DELETE CONFIRMATION DIALOG =================
        lenderToDelete?.let { lender ->
            var isDeleting by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { if (!isDeleting) lenderToDelete = null },
                title = { Text("Delete Lender Account?") },
                text = {
                    Text("Are you sure you want to delete ${lender.businessName.ifBlank { lender.name }}? This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = lender.id ?: return@Button
                            isDeleting = true
                            scope.launch {
                                adminRepo.deleteLender(id)
                                    .onSuccess {
                                        isDeleting = false
                                        lenderToDelete = null
                                        Toast.makeText(context, "Lender deleted successfully.", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure {
                                        isDeleting = false
                                        Toast.makeText(context, "Delete failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isDeleting,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text(if (isDeleting) "Deleting..." else "Confirm Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { lenderToDelete = null }, enabled = !isDeleting) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}