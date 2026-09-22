package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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

    // Dialog States
    var selectedBorrowerForDetail by remember { mutableStateOf<Borrower?>(null) }
    var selectedBorrowerForEdit by remember { mutableStateOf<Borrower?>(null) }
    var borrowerToDelete by remember { mutableStateOf<Borrower?>(null) }
    var previewDocUrl by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(Title, Url)
    var isUploadingDoc by remember { mutableStateOf(false) }

    // Pending upload target: "avatar", "aadhaar", or "pan"
    var pendingUploadType by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        scope.launch {
            lenderRepo.getBorrowers(lenderId)
                .onSuccess {
                    borrowersList = it
                    // Also refresh currently open details dialog if active
                    selectedBorrowerForDetail?.let { current ->
                        selectedBorrowerForDetail = it.find { b -> b.id == current.id }
                    }
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
    ) { uri ->
        uri?.let { selectedUri ->
            val borrower = selectedBorrowerForDetail ?: return@let
            val docType = pendingUploadType ?: return@let
            val borrowerId = borrower.id ?: return@let

            scope.launch {
                try {
                    isUploadingDoc = true
                    val bytes = context.contentResolver.openInputStream(selectedUri)?.use { it.readBytes() }
                    if (bytes != null && bytes.isNotEmpty()) {
                        lenderRepo.updateBorrowerKycDoc(borrowerId, borrower.mobileNumber, docType, bytes)
                            .onSuccess { newUrl ->
                                isUploadingDoc = false
                                Toast.makeText(context, "${docType.replaceFirstChar { it.uppercase() }} updated successfully!", Toast.LENGTH_SHORT).show()
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
                            // Clean Card View: Clicking opens the full detail profile dialog
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

        // ================= 1. STYLISH BORROWER PROFILE DETAILS DIALOG =================
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
                        // Header with Close
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
                                if (!borrower.profilePicUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = borrower.profilePicUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(88.dp)
                                            .clip(CircleShape)
                                            .border(2.5.dp, BrandPrimary, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Surface(
                                        modifier = Modifier.size(88.dp),
                                        shape = CircleShape,
                                        color = BrandPrimary.copy(alpha = 0.12f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(46.dp)
                                            )
                                        }
                                    }
                                }

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

                            // Details Table
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

                            // KYC Documents Section
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
                                                color = if (borrower.aadhaarCardUrl.isNullOrBlank()) AlertOrange else MoneyGreen
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (!borrower.aadhaarCardUrl.isNullOrBlank()) {
                                            FilledTonalButton(
                                                onClick = { previewDocUrl = Pair("${borrower.name}'s Aadhaar Card", borrower.aadhaarCardUrl) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("View")
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (!isUploadingDoc) {
                                                    pendingUploadType = "aadhaar"
                                                    docImagePickerLauncher.launch("image/*")
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (borrower.aadhaarCardUrl.isNullOrBlank()) "Upload" else "Update")
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
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = CallBlue, modifier = Modifier.size(26.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("PAN Card", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = if (borrower.panCardUrl.isNullOrBlank()) "Not Uploaded" else "Document Uploaded",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (borrower.panCardUrl.isNullOrBlank()) AlertOrange else MoneyGreen
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (!borrower.panCardUrl.isNullOrBlank()) {
                                            FilledTonalButton(
                                                onClick = { previewDocUrl = Pair("${borrower.name}'s PAN Card", borrower.panCardUrl) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("View")
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (!isUploadingDoc) {
                                                    pendingUploadType = "pan"
                                                    docImagePickerLauncher.launch("image/*")
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (borrower.panCardUrl.isNullOrBlank()) "Upload" else "Update")
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderSubtleLight)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Actions: Edit and Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedBorrowerForEdit = borrower
                                    selectedBorrowerForDetail = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile")
                            }

                            Button(
                                onClick = {
                                    borrowerToDelete = borrower
                                    selectedBorrowerForDetail = null
                                },
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

        // ================= 2. FULLSCREEN KYC DOCUMENT PREVIEW DIALOG =================
        previewDocUrl?.let { (title, url) ->
            Dialog(
                onDismissRequest = { previewDocUrl = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .fillMaxHeight(0.90f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { previewDocUrl = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        HorizontalDivider(color = BorderSubtleLight)
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }

        // ================= 3. EDIT PROFILE DIALOG =================
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
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
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

        // ================= 4. DELETE PROFILE DIALOG =================
        borrowerToDelete?.let { borrower ->
            var isDeleting by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { if (!isDeleting) borrowerToDelete = null },
                title = { Text("Delete Client Account?") },
                text = {
                    Text("Are you sure you want to delete ${borrower.name}? This will remove all their records.")
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
                                        Toast.makeText(context, "Client deleted successfully.", Toast.LENGTH_SHORT).show()
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

@Composable
private fun DetailTableRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.5f), modifier = Modifier.padding(top = 4.dp))
    }
}