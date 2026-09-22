package com.dailypay.app.ui.screens.lender

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dailypay.app.R
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils
import kotlinx.coroutines.launch

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

    var borrowersList by remember { mutableStateOf<List<Borrower>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var selectedBorrowerForEdit by remember { mutableStateOf<Borrower?>(null) }
    var borrowerToDelete by remember { mutableStateOf<Borrower?>(null) }

    fun loadData() {
        scope.launch {
            lenderRepo.getBorrowers(lenderId)
                .onSuccess {
                    borrowersList = it
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(lenderId) {
        loadData()
    }

    val filteredBorrowers = borrowersList.filter { borrower ->
        borrower.name.contains(searchQuery, ignoreCase = true) ||
                borrower.mobileNumber.contains(searchQuery) ||
                (borrower.villageCity ?: "").contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Master Client (${filteredBorrowers.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddNewBorrowerClick) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Borrower", tint = BrandPrimary)
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search client by name, mobile, village...") },
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

                if (filteredBorrowers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No clients found." else "No clients match your search.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondaryLight
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(filteredBorrowers, key = { index, item -> item.id ?: "borrower_$index" }) { _, borrower ->
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
                                        // Profile picture thumbnail from borrowers table
                                        if (!borrower.profilePicUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = borrower.profilePicUrl,
                                                contentDescription = borrower.name,
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(CircleShape)
                                                    .border(1.5.dp, BrandPrimary, CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Surface(
                                                modifier = Modifier.size(54.dp),
                                                shape = CircleShape,
                                                color = BrandPrimary.copy(alpha = 0.12f)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = BrandPrimary,
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = borrower.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "+91 ${borrower.mobileNumber}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = BrandPrimary
                                            )
                                            val address = listOfNotNull(borrower.villageCity, borrower.dist)
                                                .filter { it.isNotBlank() }
                                                .joinToString(", ")
                                            if (address.isNotBlank()) {
                                                Text(
                                                    text = "📍 $address",
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
                                                        message = "Hello ${borrower.name},"
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
                                                onClick = { CommunicationUtils.openPhoneDialer(context, borrower.mobileNumber) },
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

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderSubtleLight)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { selectedBorrowerForEdit = borrower },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Profile")
                                        }

                                        IconButton(
                                            onClick = { borrowerToDelete = borrower }
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

          // Edit Dialog
        selectedBorrowerForEdit?.let { borrower ->
            var editName by remember { mutableStateOf(borrower.name) }
            var editMobile by remember { mutableStateOf(borrower.mobileNumber) }
            var editVillageCity by remember { mutableStateOf(borrower.villageCity ?: "") }
            var editPostOffice by remember { mutableStateOf(borrower.postOffice ?: "") }
            var editPoliceStation by remember { mutableStateOf(borrower.policeStation ?: "") }
            var editDist by remember { mutableStateOf(borrower.dist ?: "") }
            var isSaving by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { if (!isSaving) selectedBorrowerForEdit = null },
                title = { Text("Edit Client Details") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name *") },
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
                            value = editVillageCity,
                            onValueChange = { editVillageCity = it },
                            label = { Text("Village / City") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editPostOffice,
                            onValueChange = { editPostOffice = it },
                            label = { Text("Post Office") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editPoliceStation,
                            onValueChange = { editPoliceStation = it },
                            label = { Text("Police Station") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editDist,
                            onValueChange = { editDist = it },
                            label = { Text("District") },
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
                                val updated = borrower.copy(
                                    name = editName.trim(),
                                    mobileNumber = editMobile.trim(),
                                    villageCity = editVillageCity.trim().ifBlank { null },
                                    postOffice = editPostOffice.trim().ifBlank { null },
                                    policeStation = editPoliceStation.trim().ifBlank { null },
                                    dist = editDist.trim().ifBlank { null }
                                )
                                lenderRepo.updateBorrower(updated)
                                    .onSuccess {
                                        isSaving = false
                                        selectedBorrowerForEdit = null
                                        Toast.makeText(context, "Client updated successfully!", Toast.LENGTH_SHORT).show()
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
                    TextButton(onClick = { selectedBorrowerForEdit = null }, enabled = !isSaving) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Dialog
        borrowerToDelete?.let { borrower ->
            var isDeleting by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { if (!isDeleting) borrowerToDelete = null },
                title = { Text("Delete Client Account?") },
                text = {
                    Text("Are you sure you want to delete ${borrower.name}? This will remove all their loan history.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = borrower.id ?: return@Button
                            isDeleting = true
                            scope.launch {
                                lenderRepo.deleteBorrower(id)
                                    .onSuccess {
                                        isDeleting = false
                                        borrowerToDelete = null
                                        Toast.makeText(context, "Client deleted.", Toast.LENGTH_SHORT).show()
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
                    TextButton(onClick = { borrowerToDelete = null }, enabled = !isDeleting) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}