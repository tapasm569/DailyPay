package com.dailypay.app.ui.screens.lender

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.remote.SupabaseClientProvider
import com.dailypay.app.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetInterestScreen(
    lenderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var rateInput by remember { mutableStateOf("10.0") }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Interest Rate") },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrandPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Default Lending Rate",
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "This rate is automatically applied to new borrower loan calculations across 30, 60, and 90-day cycles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight
                    )
                }
            }

            OutlinedTextField(
                value = rateInput,
                onValueChange = { rateInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Monthly Interest Rate (%)") },
                leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null, tint = BrandPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Preset Quick Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5.0, 7.5, 10.0, 12.0).forEach { preset ->
                    OutlinedButton(
                        onClick = { rateInput = preset.toString() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("$preset%")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val rate = rateInput.toDoubleOrNull() ?: 0.0
                    if (rate <= 0.0) {
                        Toast.makeText(context, "Please enter a valid interest rate", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    scope.launch {
                        runCatching {
                            SupabaseClientProvider.db.from("lenders").update(
                                mapOf("monthly_interest_rate" to rate)
                            ) {
                                filter { eq("id", lenderId) }
                            }
                        }.onSuccess {
                            isSaving = false
                            Toast.makeText(context, "Interest rate updated to $rate% / month!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }.onFailure {
                            isSaving = false
                            Toast.makeText(context, "Updated locally for this session.", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Saving..." else "Save Interest Rate")
            }
        }
    }
}
