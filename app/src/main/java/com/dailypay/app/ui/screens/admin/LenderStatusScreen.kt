package com.dailypay.app.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailypay.app.data.model.Lender
import com.dailypay.app.data.remote.SupabaseClientProvider
import com.dailypay.app.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderStatusScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = SupabaseClientProvider.db

    var lenders by remember { mutableStateOf<List<Lender>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var updatingLenderId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    fun loadLenders() {
        scope.launch {
            try {
                lenders = db.from("lenders")
                    .select()
                    .decodeList<Lender>()
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        loadLenders()
    }

    fun updateStatus(lender: Lender, newStatus: String) {
        val id = lender.id ?: return
        updatingLenderId = id
        scope.launch {
            try {
                db.from("lenders").update(
                    buildJsonObject { put("status", newStatus) }
                ) {
                    filter { eq("id", id) }
                }

                lenders = lenders.map {
                    if (it.id == id) it.copy(status = newStatus) else it
                }
                updatingLenderId = null
                Toast.makeText(context, "${lender.businessName ?: lender.name} set to $newStatus", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                updatingLenderId = null
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filteredList = lenders.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                (it.businessName ?: "").contains(searchQuery, ignoreCase = true) ||
                it.mobileNumber.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lender Status Control") },
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
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by lender, business, or mobile...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No lenders found.", color = TextSecondaryLight)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredList, key = { it.id ?: it.mobileNumber }) { lender ->
                            val isRunning = lender.status.uppercase() == "RUN"
                            val isUpdating = updatingLenderId == lender.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = CircleShape,
                                            color = if (isRunning) MoneyGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (isRunning) Icons.Default.PlayArrow else Icons.Default.Stop,
                                                    contentDescription = null,
                                                    tint = if (isRunning) MoneyGreen else DangerRed,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = lender.businessName?.ifBlank { lender.name } ?: lender.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Prop: ${lender.ownerName ?: lender.name} • +91 ${lender.mobileNumber}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryLight
                                            )
                                            if (!lender.address.isNullOrBlank() || !lender.dist.isNullOrBlank()) {
                                                Text(
                                                    text = listOfNotNull(lender.villageTown, lender.dist).joinToString(", "),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondaryLight
                                                )
                                            }
                                        }

                                        // Status Pill Badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isRunning) MoneyGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (isRunning) "RUNNING" else "STOPPED",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isRunning) MoneyGreen else DangerRed
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = BorderSubtleLight.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Run & Stop Controls
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { updateStatus(lender, "RUN") },
                                            enabled = !isRunning && !isUpdating,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MoneyGreen,
                                                disabledContainerColor = MoneyGreen.copy(alpha = 0.35f)
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = if (isRunning) "Active (Run)" else "Run", fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { updateStatus(lender, "STOP") },
                                            enabled = isRunning && !isUpdating,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = DangerRed,
                                                disabledContainerColor = DangerRed.copy(alpha = 0.35f)
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = if (!isRunning) "Stopped" else "Stop", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}