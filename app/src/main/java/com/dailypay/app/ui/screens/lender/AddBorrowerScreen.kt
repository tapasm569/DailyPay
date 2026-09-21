package com.dailypay.app.ui.screens.lender

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBorrowerScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lenderRepo = remember { LenderRepository() }

    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var villageCity by remember { mutableStateOf("") }
    var postOffice by remember { mutableStateOf("") }
    var policeStation by remember { mutableStateOf("") }
    var dist by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // KYC file byte buffers & selection labels
    var aadhaarBytes by remember { mutableStateOf<ByteArray?>(null) }
    var aadhaarFileName by remember { mutableStateOf<String?>(null) }

    var panBytes by remember { mutableStateOf<ByteArray?>(null) }
    var panFileName by remember { mutableStateOf<String?>(null) }

    var avatarBytes by remember { mutableStateOf<ByteArray?>(null) }
    var avatarFileName by remember { mutableStateOf<String?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }

    // Activity result launchers with automatic compression
    val aadhaarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            aadhaarBytes = compressImageUri(context, it)
            aadhaarFileName = if (aadhaarBytes != null) "Aadhaar Card Attached" else "Failed to load image"
        }
    }

    val panLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            panBytes = compressImageUri(context, it)
            panFileName = if (panBytes != null) "PAN Card Attached" else "Failed to load image"
        }
    }

    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            avatarBytes = compressImageUri(context, it)
            avatarFileName = if (avatarBytes != null) "Profile Photo Attached" else "Failed to load image"
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Borrower Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { if (it.length <= 10) mobileNumber = it.filter { ch -> ch.isDigit() } },
                label = { Text("Mobile Number *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                label = { Text("Set Login Password *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "KYC Documents & Photo", style = MaterialTheme.typography.titleMedium)

            // Document Pickers
            OutlinedButton(
                onClick = { aadhaarLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(aadhaarFileName ?: "Upload Aadhaar Card")
            }

            OutlinedButton(
                onClick = { panLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(panFileName ?: "Upload PAN Card")
            }

            OutlinedButton(
                onClick = { avatarLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AccountBox, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(avatarFileName ?: "Upload Profile Picture")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (name.isBlank() || mobileNumber.length != 10 || password.isBlank()) {
                        Toast.makeText(context, "Please fill in all required (*) fields.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    scope.launch {
                        val borrower = Borrower(
                            lenderId = lenderId,
                            name = name.trim(),
                            mobileNumber = mobileNumber.trim(),
                            villageCity = villageCity.trim(),
                            postOffice = postOffice.trim(),
                            policeStation = policeStation.trim(),
                            dist = dist.trim(),
                            passwordHash = password.trim()
                        )

                        lenderRepo.addBorrower(borrower, aadhaarBytes, panBytes, avatarBytes)
                            .onSuccess {
                                isSubmitting = false
                                Toast.makeText(context, "Borrower profile created successfully!", Toast.LENGTH_LONG).show()
                                onNavigateBack()
                            }
                            .onFailure {
                                isSubmitting = false
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Borrower Profile", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * Resizes and compresses image data to prevent network timeouts and payload limits.
 */
private fun compressImageUri(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val maxDimension = 1280
        val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val width: Int
        val height: Int
        if (originalBitmap.width > originalBitmap.height) {
            width = minOf(originalBitmap.width, maxDimension)
            height = (width / ratio).toInt()
        } else {
            height = minOf(originalBitmap.height, maxDimension)
            width = (height * ratio).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.toByteArray()
    } catch (e: Exception) {
        null
    }
}
