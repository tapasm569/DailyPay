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
    var previewDocUrl by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isUploadingDoc by remember { mutableStateOf(false) }

    var pendingUploadType by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        if (lenderId.isBlank() || lenderId == "unknown") {
            isLoading = false
            Toast.makeText(context, "Invalid lender session identifier.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            lenderRepo.getBorrowers(lenderId)
                .onSuccess {
                    borrowersList = it
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

        // ================= PROFILE DETAILS DIALOG =================
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
                                   