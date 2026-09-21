package com.dailypay.app.ui.screens.lender

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
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

    // Upload byte buffers & file info labels
    var aadhaarBytes by remember { mutableStateOf<ByteArray?>(null) }
    var aadhaarInfo by remember { mutableStateOf<String?>(null) }

    var panBytes by remember { mutableStateOf<ByteArray?>(null) }
    var panInfo by remember { mutableStateOf<String?>(null) }

    var avatarBytes by remember { mutableStateOf<ByteArray?>(null) }
    var avatarInfo by remember { mutableStateOf<String?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }

    // 1. Aadhaar Card Launcher
    val aadhaarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val result = processAndCompressImage(context, uri)
            if (result != null) {
                aadhaarBytes = result.first
                aadhaarInfo = "Aadhaar Attached (${result.second})"
                Toast.makeText(context, "Aadhaar card loaded successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to read Aadhaar image. Try another photo.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 2. PAN Card Launcher
    val panLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val result = processAndCompressImage(context, uri)
            if (result != null) {
                panBytes = result.first
                panInfo = "PAN Attached (${result.second})"
                Toast.makeText(context, "PAN card loaded successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to read PAN image. Try another photo.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 3. Profile Avatar Launcher
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val result = processAndCompressImage(context, uri)
            if (result != null) {
                avatarBytes = result.first
                avatarInfo = "Photo Attached (${result.second})"
                Toast.makeText(context, "Profile photo loaded successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to read profile photo. Try another photo.", Toast.LENGTH_LONG).show()
            }
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

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "KYC Documents & Photo", style = MaterialTheme.typography.titleMedium)

            // --- 1. Aadhaar Card Attachment Box ---
            DocumentAttachmentCard(
                label = aadhaarInfo ?: "Upload Aadhaar Card",
                isAttached = aadhaarBytes != null,
                onSelectClick = {
                    try {
                        aadhaarLauncher.launch("image/*")
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open gallery: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                onClearClick = {
                    aadhaarBytes = null
                    aadhaarInfo = null
                }
            )

            // --- 2. PAN Card Attachment Box ---
            DocumentAttachmentCard(
                label = panInfo ?: "Upload PAN Card",
                isAttached = panBytes != null,
                onSelectClick = {
                    try {
                        panLauncher.launch("image/*")
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open gallery: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                onClearClick = {
                    panBytes = null
                    panInfo = null
                }
            )

            // --- 3. Profile Photo Attachment Box ---
            DocumentAttachmentCard(
                label = avatarInfo ?: "Upload Profile Picture",
                isAttached = avatarBytes != null,
                onSelectClick = {
                    try {
                        avatarLauncher.launch("image/*")
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open gallery: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                onClearClick = {
                    avatarBytes = null
                    avatarInfo = null
                }
            )

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

@Composable
private fun DocumentAttachmentCard(
    label: String,
    isAttached: Boolean,
    onSelectClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isAttached) MoneyGreen else BorderSubtleLight,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isAttached) MoneyGreenSubtle else MaterialTheme.colorScheme.surface,
        onClick = onSelectClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isAttached) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = if (isAttached) MoneyGreen else BrandPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isAttached) MoneyGreen else MaterialTheme.colorScheme.onSurface
                )
            }

            if (isAttached) {
                IconButton(onClick = onClearClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove attachment",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Text(
                    text = "Choose",
                    style = MaterialTheme.typography.labelMedium,
                    color = BrandPrimary
                )
            }
        }
    }
}

/**
 * Safely reads raw bytes from any Android ContentResolver URI before decoding.
 * Uses inSampleSize to protect against OutOfMemory on 50MP/108MP camera photos,
 * then resizes and compresses to ~150-300 KB JPEG.
 */
private fun processAndCompressImage(context: Context, uri: Uri): Pair<ByteArray, String>? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val rawBytes = inputStream.use { it.readBytes() }
        if (rawBytes.isEmpty()) return null

        // 1. Inspect dimensions without loading into memory
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)

        val originalWidth = options.outWidth
        val originalHeight = options.outHeight
        if (originalWidth <= 0 || originalHeight <= 0) return null

        // 2. Compute sample size to avoid OutOfMemoryError on large camera shots
        val maxDimension = 1280
        var sampleSize = 1
        while (originalWidth / (sampleSize * 2) >= maxDimension && originalHeight / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }

        // 3. Decode scaled sample
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val sampledBitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions) ?: return null

        // 4. Exact aspect ratio scaling
        val ratio = sampledBitmap.width.toFloat() / sampledBitmap.height.toFloat()
        val finalWidth: Int
        val finalHeight: Int
        if (sampledBitmap.width > sampledBitmap.height) {
            finalWidth = minOf(sampledBitmap.width, maxDimension)
            finalHeight = maxOf(1, (finalWidth / ratio).toInt())
        } else {
            finalHeight = minOf(sampledBitmap.height, maxDimension)
            finalWidth = maxOf(1, (finalHeight * ratio).toInt())
        }

        val resizedBitmap = Bitmap.createScaledBitmap(sampledBitmap, finalWidth, finalHeight, true)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val compressedBytes = outputStream.toByteArray()

        val sizeKb = compressedBytes.size / 1024
        Pair(compressedBytes, "${sizeKb} KB")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
