package com.dailypay.app.ui.screens.lender

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dailypay.app.R
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.components.AppImageView
import com.dailypay.app.ui.components.KycDocumentViewerDialog
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
    val lenderRepo = remember { LenderRepository(context.applicationContext) }

    var borrowersList by remember { mutableStateOf<List<Borrower>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedBorrowerForDetail by remember { mutableStateOf<Borrower?>(null) }
    var selectedBorrowerForEdit by remember { mutableStateOf<Borrower?>(null) }
    var selectedBorrowerForDelete by remember { mutableStateOf<Borrower?>(null) }

    var viewingDocTitle by remember { mutableStateOf<String?>(null) }
    var viewingDocData by remember { mutableStateOf<String?>(null) }

    var isUploadingDoc by remember { mutableStateOf(false) }
    var pendingUploadType by remember { mutableStateOf<String?>(null) }

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

    val docImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val borrower = selectedBorrowerForDetail ?: return@rememberLauncherForActivityResult
            val borrowerId = borrower.id ?: return@rememberLauncherForActivityResult
            val docType = pendingUploadType ?: return@rememberLauncherForActivityResult

            isUploadingDoc = true
            scope.launch {
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null && bytes.isNotEmpty()) {
                        lenderRepo.updateBorrowerKycDoc(borrowerId, borrower.mobileNumber, docType, bytes)
                            .onSuccess { newBase64 ->
                                isUploadingDoc = false
                                Toast.makeText(context, "${docType.replaceFirstChar { it.uppercase() }} updated successfully!", Toast.LENGTH_SHORT).show()
                                selectedBorrowerForDetail = when (docType) {
                                    "aadhaar" -> borrower.copy(aadhaarCardUrl = newBase64)
                                    "pan" -> borrower.copy(panCardUrl = newBase64)
                                    else -> borrower.copy(profilePicUrl = newBase64)
                                }
                                loadData()
                            }
                            .onFailure { err ->
                                isUploadingDoc = false
                                Toast.makeText(context, "Upload failed: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        isUploadingDoc = false
                    }
                } catch (e: Exception) {
                    isUploadingDoc = false
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                            text = if (searchQuery.isBlank()) "No clients registered yet." else "No clients match your search.",
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
                                    .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp))
                                    .clickable { selectedBorrowerForDetail = borrower },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppImageView(
                                        data = borrower.profilePicUrl,
                                        contentDescription = borrower.name,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, BrandPrimary, CircleShape),
                                        placeholderIcon = Icons.Default.Person,
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = borrower.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
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

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick = {
                                                CommunicationUtils.openWhatsAppChat(
                                                    context = context,
                                                    rawMobileNumber = borrower.mobileNumber,
                                                    message = "Hello ${borrower.name},"
                                                )
                                            },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, WhatsAppGreen.copy(alpha = 0.3f), CircleShape)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                                contentDescription = "WhatsApp",
                                                tint = WhatsAppGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { CommunicationUtils.openPhoneDialer(context, borrower.mobileNumber) },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, CallBlue.copy(alpha = 0.3f), CircleShape)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_call),
                                                contentDescription = "Call",
                                                tint = CallBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= BORROWER PROFILE DETAILS DIALOG =================
        selectedBorrowerForDetail?.let { borrower ->
            Dialog(
                onDismissRequest = { selectedBorrowerForDetail = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.88f)
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Customer Profile Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { selectedBorrowerForDetail = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        HorizontalDivider(color = BorderSubtleLight)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))

                            // Profile Avatar & Update Button
                            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                AppImageView(
                                    data = borrower.profilePicUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(CircleShape)
                                        .border(2.5.dp, BrandPrimary, CircleShape),
                                    placeholderIcon = Icons.Default.Person,
                                    contentScale = ContentScale.Crop
                                )

                                Surface(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (!isUploadingDoc) {
                                                pendingUploadType = "avatar"
                                                docImagePickerLauncher.launch("image/*")
                                            }
                                        },
                                    color = BrandPrimary,
                                    shape = CircleShape
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isUploadingDoc && pendingUploadType == "avatar") {
                                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                        } else {
                                            Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtleLight, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    DetailTableRow("Full Name", borrower.name)
                                    DetailTableRow("Mobile Number", "+91 ${borrower.mobileNumber}")
                                    DetailTableRow("Village / City", borrower.villageCity ?: "Not Specified")
                                    DetailTableRow("Post Office", borrower.postOffice ?: "Not Specified")
                                    DetailTableRow("Police Station", borrower.policeStation ?: "Not Specified")
                                    DetailTableRow("District", borrower.dist ?: "Not Specified")
                                }
                            }

                            Text("KYC Documents", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                            // Aadhaar Card Row
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtleLight, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Badge, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(26.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Aadhaar Card", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = if (borrower.aadhaarCardUrl.isNullOrBlank()) "Not Uploaded" else "Document Uploaded",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (borrower.aadhaarCardUrl.isNullOrBlank()) DangerRed else MoneyGreen
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilledTonalButton(
                                            onClick = {
                                                if (!borrower.aadhaarCardUrl.isNullOrBlank()) {
                                                    viewingDocTitle = "Aadhaar Card - ${borrower.name}"
                                                    viewingDocData = borrower.aadhaarCardUrl
                                                } else {
                                                    Toast.makeText(context, "No Aadhaar document uploaded yet", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (!isUploadingDoc) {
                                                    pendingUploadType = "aadhaar"
                                                    docImagePickerLauncher.launch("image/*")
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Update")
                                        }
                                    }
                                }
                            }

                            // PAN Card Row
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtleLight, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(26.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("PAN Card", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = if (borrower.panCardUrl.isNullOrBlank()) "Not Uploaded" else "Document Uploaded",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (borrower.panCardUrl.isNullOrBlank()) DangerRed else MoneyGreen
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilledTonalButton(
                                            onClick = {
                                                if (!borrower.panCardUrl.isNullOrBlank()) {
                                                    viewingDocTitle = "PAN Card - ${borrower.name}"
                                                    viewingDocData = borrower.panCardUrl
                                                } else {
                                                    Toast.makeText(context, "No PAN document uploaded yet", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (!isUploadingDoc) {
                                                    pendingUploadType = "pan"
                                                    docImagePickerLauncher.launch("image/*")
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Update")
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedBorrowerForEdit = borrower },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit Profile")
                                }

                                Button(
                                    onClick = { selectedBorrowerForDelete = borrower },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Delete Profile")
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= KYC VIEWER DIALOG =================
        if (!viewingDocTitle.isNullOrBlank() && !viewingDocData.isNullOrBlank()) {
            KycDocumentViewerDialog(
                title = viewingDocTitle ?: "KYC Document",
                imageData = viewingDocData,
                onDismiss = {
                    viewingDocTitle = null
                    viewingDocData = null
                }
            )
        }

        // ================= EDIT PROFILE DIALOG =================
        selectedBorrowerForEdit?.let { borrower ->
            var editName by remember { mutableStateOf(borrower.name) }
            var editMobile by remember { mutableStateOf(borrower.mobileNumber) }
            var editVillage by remember { mutableStateOf(borrower.villageCity ?: "") }
            var editPostOffice by remember { mutableStateOf(borrower.postOffice ?: "") }
            var editPoliceStation by remember { mutableStateOf(borrower.policeStation ?: "") }
            var editDist by remember { mutableStateOf(borrower.dist ?: "") }
            var editPassword by remember { mutableStateOf("") }
            var isSaving by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { if (!isSaving) selectedBorrowerForEdit = null },
                title = { Text("Edit Client Details") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Client Name *") },
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
                            value = editVillage,
                            onValueChange = { editVillage = it },
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
                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("Update Login Password (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editName.isBlank() || editMobile.length < 10) {
                                Toast.makeText(context, "Fill in valid name and 10-digit mobile.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val updated = borrower.copy(
                                name = editName.trim(),
                                mobileNumber = editMobile.trim(),
                                villageCity = editVillage.trim().ifBlank { null },
                                postOffice = editPostOffice.trim().ifBlank { null },
                                policeStation = editPoliceStation.trim().ifBlank { null },
                                dist = editDist.trim().ifBlank { null },
                                passwordHash = if (editPassword.isNotBlank()) editPassword.trim() else borrower.passwordHash
                            )

                            isSaving = true
                            scope.launch {
                                lenderRepo.updateBorrower(updated)
                                    .onSuccess {
                                        isSaving = false
                                        selectedBorrowerForEdit = null
                                        selectedBorrowerForDetail = updated
                                        Toast.makeText(context, "Client details saved!", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure {
                                        isSaving = false
                                        Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !isSaving
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

        // ================= DELETE CONFIRMATION DIALOG =================
        selectedBorrowerForDelete?.let { borrower ->
            var isDeleting by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { if (!isDeleting) selectedBorrowerForDelete = null },
                title = { Text("Delete Borrower") },
                text = { Text("Are you sure you want to delete '${borrower.name}'? Their active loans and records will be impacted.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = borrower.id ?: return@Button
                            isDeleting = true
                            scope.launch {
                                lenderRepo.deleteBorrower(id)
                                    .onSuccess {
                                        isDeleting = false
                                        selectedBorrowerForDelete = null
                                        selectedBorrowerForDetail = null
                                        Toast.makeText(context, "Borrower deleted.", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                    .onFailure {
                                        isDeleting = false
                                        Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        enabled = !isDeleting
                    ) {
                        Text(if (isDeleting) "Deleting..." else "Confirm Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedBorrowerForDelete = null }, enabled = !isDeleting) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailTableRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
