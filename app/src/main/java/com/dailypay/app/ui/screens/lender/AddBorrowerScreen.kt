@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.data.model.Borrower
import com.dailypay.app.data.repository.LenderRepository
import com.dailypay.app.ui.theme.BrandPrimary
import com.dailypay.app.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun AddBorrowerScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val lenderRepo = remember { LenderRepository(context) }

    val resolvedLenderId = remember(lenderId) {
        if (lenderId.isNotBlank() && lenderId != "{lenderId}") lenderId
        else sessionManager.getUserId() ?: ""
    }

    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var villageCity by remember { mutableStateOf("") }
    var postOffice by remember { mutableStateOf("") }
    var dist by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lender_add_borrower), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Borrower Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = mobile,
                onValueChange = { if (it.length <= 10) mobile = it },
                label = { Text("Mobile Number (10 Digits) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = villageCity,
                onValueChange = { villageCity = it },
                label = { Text("Village / City") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = postOffice,
                onValueChange = { postOffice = it },
                label = { Text("Post Office") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dist,
                onValueChange = { dist = it },
                label = { Text("District") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pinCode,
                onValueChange = { if (it.length <= 6) pinCode = it },
                label = { Text("PIN Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Please enter borrower name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (mobile.trim().length != 10) {
                        Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (resolvedLenderId.isBlank()) {
                        Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    scope.launch {
                        val newBorrower = Borrower(
                            lenderId = resolvedLenderId,
                            name = name.trim(),
                            mobileNumber = mobile.trim(),
                            villageCity = villageCity.trim().ifBlank { null },
                            postOffice = postOffice.trim().ifBlank { null },
                            dist = dist.trim().ifBlank { null },
                            pinCode = pinCode.trim().ifBlank { null }
                        )

                        lenderRepo.addBorrower(
                            borrower = newBorrower,
                            aadhaarBytes = null,
                            panBytes = null,
                            avatarBytes = null
                        ).onSuccess {
                            isSubmitting = false
                            Toast.makeText(context, "Borrower added successfully!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }.onFailure { error ->
                            isSubmitting = false
                            Toast.makeText(context, "Failed: ${error.message}", Toast.LENGTH_LONG).show()
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
                Text(
                    text = if (isSubmitting) "Saving..." else stringResource(R.string.action_save),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
