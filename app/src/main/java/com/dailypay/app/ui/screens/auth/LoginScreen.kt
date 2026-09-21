package com.dailypay.app.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.repository.AuthRepository
import com.dailypay.app.data.repository.LoginResult
import com.dailypay.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onAdminLoginSuccess: () -> Unit,
    onLenderLoginSuccess: (lenderId: String) -> Unit,
    onBorrowerLoginSuccess: (borrowerId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }

    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(BrandPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CurrencyRupee,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "DailyPay",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Micro-Lending & Daily Collection Engine",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight
            )

            Spacer(modifier = Modifier.height(36.dp))

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { if (it.length <= 10) mobileNumber = it.filter { ch -> ch.isDigit() } },
                label = { Text("Mobile Number") },
                placeholder = { Text("Enter 10-digit registered number") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = BrandPrimary)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = BorderSubtleLight
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BrandPrimary)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = BorderSubtleLight
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (mobileNumber.length != 10) {
                        Toast.makeText(context, "Enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password.isBlank()) {
                        Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        authRepo.unifiedLogin(mobileNumber, password)
                            .onSuccess { result ->
                                isLoading = false
                                when (result) {
                                    is LoginResult.Admin -> onAdminLoginSuccess()
                                    is LoginResult.LenderUser -> onLenderLoginSuccess(result.lenderId)
                                    is LoginResult.BorrowerUser -> onBorrowerLoginSuccess(result.borrowerId)
                                }
                            }
                            .onFailure {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    it.message ?: "Invalid login details",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
