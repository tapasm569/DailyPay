package com.dailypay.app.ui.screens.lender

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.components.AppImageView
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.ImageCompressor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBorrowerScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository(context.applicationContext) }

    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var villageCity by remember { mutableStateOf("") }
    var postOffice by remember { mutableStateOf("") }
    var policeStation by remember { mutableStateOf("") }
    var dist by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var avatarBytes by remember { mutableStateOf<ByteArray?>(null) }
    var avatarPreviewBase64 by remember { mutableStateOf<String?>(null) }
    var aadhaarBytes by remember { mutableStateOf<ByteArray?>(null) }
    var panBytes by remember { mutableStateOf<ByteArray?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
            avatarBytes = bytes
            avatarPreviewBase64 = ImageCompressor.compressAndEncodeBase64(bytes)
        }
    }

    val aadhaarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            aadhaarBytes = context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
        }
    }

    val panPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            panBytes = context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Borrower") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Photo Selector Box
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
            ) {
                AppImageView(
                    data = avatarPreviewBase64,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, BrandPrimary, CircleShape),
                    placeholderIcon = Icons.Default.Person
                )

                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .clickable { avatarPicker.launch("image/*") },
                    color = BrandPrimary,
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Add Photo", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Borrower Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it.filter { ch -> ch.isDigit() }.take(10) },
                label = { Text("Mobile Number (10 digits) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = villageCity,
                    onValueChange = { villageCity = it },
                    label = { Text("Village / City") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = postOffice,
                    onValueChange = { postOffice = it },
                    label = { Text("Post Office") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = policeStation,
                    onValueChange = { policeStation = it },
                    label = { Text("Police Station") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = dist,
                    onValueChange = { dist = it },
                    label = { Text("District") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Set Login Password (optional)") },
                placeholder = { Text("Defaults to mobile number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("KYC Documents & Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Aadhaar Attachment Button
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { aadhaarPicker.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (aadhaarBytes != null) MoneyGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (aadhaarBytes != null) Icons.Default.CheckCircle else Icons.Default.Badge,
                        contentDescription = null,
                        tint = if (aadhaarBytes != null) MoneyGreen else BrandPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (aadhaarBytes != null) "Aadhaar Attached (${aadhaarBytes!!.size / 1024} KB)" else "Attach Aadhaar Card",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (aadhaarBytes != null) FontWeight.Bold else FontWeight.Normal,
                        color = if (aadhaarBytes != null) MoneyGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // PAN Attachment Button
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { panPicker.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (panBytes != null) MoneyGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (panBytes != null) Icons.Default.CheckCircle else Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = if (panBytes != null) MoneyGreen else BrandPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (panBytes != null) "PAN Attached (${panBytes!!.size / 1024} KB)" else "Attach PAN Card",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (panBytes != null) FontWeight.Bold else FontWeight.Normal,
                        color = if (panBytes != null) MoneyGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Enter borrower name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (mobileNumber.length < 10) {
                        Toast.makeText(context, "Enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    scope.launch {
                        val newBorrower = Borrower(
                            lenderId = lenderId.trim(),
                            name = name.trim(),
                            mobileNumber = mobileNumber.trim(),
                            villageCity = villageCity.trim().ifBlank { null },
                            postOffice = postOffice.trim().ifBlank { null },
                            policeStation = policeStation.trim().ifBlank { null },
                            dist = dist.trim().ifBlank { null },
                            passwordHash = password.trim().ifBlank { mobileNumber.trim() }
                        )

                        lenderRepo.addBorrower(
                            borrower = newBorrower,
                            aadhaarBytes = aadhaarBytes,
                            panBytes = panBytes,
                            avatarBytes = avatarBytes
                        ).onSuccess {
                            isSubmitting = false
                            Toast.makeText(context, "Borrower profile created successfully!", Toast.LENGTH_LONG).show()
                            onNavigateBack()
                        }.onFailure { err ->
                            isSubmitting = false
                            Toast.makeText(context, "Failed: ${err.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save Borrower Profile", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
