package com.dailypay.app.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.data.model.PasswordResetRequest
import com.dailypay.app.data.model.ResetStatus
import com.dailypay.app.data.repository.AdminRepository
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordRequestsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val adminRepo = remember { AdminRepository() }

    var requestsList by remember { mutableStateOf<List<PasswordResetRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedRequestForReset by remember { mutableStateOf<PasswordResetRequest?>(null) }
    var newPasswordText by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            adminRepo.getPasswordResetRequests()
                .onSuccess {
                    requestsList = it.filter { req -> req.status == ResetStatus.PENDING }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Requests (${requestsList.size})") },
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        } else if (requestsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MoneyGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No pending password reset requests.",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondaryLight
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(requestsList, key = { index, item -> item.id ?: "req_$index" }) { _, req ->
                    val mobile = req.mobileNumber ?: "N/A"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Role: ${req.userRole.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = BrandPrimary
                                    )
                                    Text(
                                        text = "Mobile: +91 $mobile",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Requested: ${req.createdAt?.take(10) ?: "Today"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            CommunicationUtils.openWhatsAppChat(
                                                context = context,
                                                rawMobileNumber = mobile,
                                                message = "Hello, DailyPay Admin regarding your password reset request."
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
                                        onClick = { CommunicationUtils.openPhoneDialer(context, mobile) },
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

                            HorizontalDivider(color = BorderSubtleLight)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        selectedRequestForReset = req
                                        newPasswordText = ""
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                                ) {
                                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reset Password")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val reqId = req.id ?: return@OutlinedButton
                                        scope.launch {
                                            adminRepo.resolvePasswordResetRequest(reqId, ResetStatus.EXPIRED)
                                                .onSuccess {
                                                    Toast.makeText(context, "Request dismissed.", Toast.LENGTH_SHORT).show()
                                                    loadData()
                                                }
                                        }
                                    },
                                    modifier = Modifier.weight(0.7f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= RESET PASSWORD DIALOG =================
        selectedRequestForReset?.let { req ->
            val mobile = req.mobileNumber ?: ""
            val targetId = req.lenderId ?: req.userId ?: ""

            AlertDialog(
                onDismissRequest = { if (!isProcessing) selectedRequestForReset = null },
                title = { Text("Assign New Password") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Enter new login password for +91 $mobile (${req.userRole.name}):")

                        OutlinedTextField(
                            value = newPasswordText,
                            onValueChange = { newPasswordText = it },
                            label = { Text("New Password *") },
                            placeholder = { Text("Minimum 4 characters") },
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
                            val reqId = req.id ?: return@Button
                            if (newPasswordText.trim().length < 4) {
                                Toast.makeText(context, "Password must be at least 4 characters.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isProcessing = true
                            scope.launch {
                                adminRepo.approveAndResetPassword(
                                    requestId = reqId,
                                    targetId = targetId,
                                    mobileNumber = mobile,
                                    userRole = req.userRole,
                                    newPassword = newPasswordText.trim()
                                ).onSuccess {
                                    isProcessing = false
                                    selectedRequestForReset = null
                                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()

                                    // Offer to send credentials via WhatsApp
                                    CommunicationUtils.openWhatsAppChat(
                                        context = context,
                                        rawMobileNumber = mobile,
                                        message = "Hello, your DailyPay password has been reset. Your new login password is: ${newPasswordText.trim()}"
                                    )
                                    loadData()
                                }.onFailure {
                                    isProcessing = false
                                    Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isProcessing) "Updating..." else "Confirm & Send via WhatsApp")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { selectedRequestForReset = null },
                        enabled = !isProcessing
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
