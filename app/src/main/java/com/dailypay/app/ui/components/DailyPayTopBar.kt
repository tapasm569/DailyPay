package com.dailypay.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.dailypay.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPayTopBar(
    businessName: String,
    pendingDue: Double,
    receivedPayment: Double,
    onSearchClick: () -> Unit,
    onAccountClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onResetPasswordClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    // Safe formatting using Locale.US to prevent decimal crashes
    val formattedPendingDue = remember(pendingDue) {
        try {
            String.format(Locale.US, "%.2f", pendingDue)
        } catch (e: Exception) {
            "0.00"
        }
    }

    val formattedReceived = remember(receivedPayment) {
        try {
            String.format(Locale.US, "%.2f", receivedPayment)
        } catch (e: Exception) {
            "0.00"
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Row: Hamburger, Business Name, Search & Account Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = !menuExpanded }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Hamburger Dropdown Menu
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            offset = DpOffset(x = 0.dp, y = 8.dp),
                            properties = PopupProperties(
                                focusable = true,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            ),
                            shape = RoundedCornerShape(12.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            shadowElevation = 8.dp,
                            modifier = Modifier.width(240.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = businessName.ifBlank { "DailyPay" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = BrandPrimary
                                )
                                Text(
                                    text = "Lender Account",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Edit Profile") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onEditProfileClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reset Password") },
                                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onResetPasswordClick()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Logout", color = DangerRed) },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = DangerRed) },
                                onClick = {
                                    menuExpanded = false
                                    onLogoutClick()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = businessName.ifBlank { "DailyPay" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onAccountClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrandPrimary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Dashboard",
                            tint = BrandPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Stats Row: Pending Due & Received Payment Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pending Due Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = AlertOrangeSubtle
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AlertOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Pending Due",
                                style = MaterialTheme.typography.labelMedium,
                                color = AlertOrange
                            )
                            Text(
                                text = "₹$formattedPendingDue",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryLight
                            )
                        }
                    }
                }

                // Received Payment Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MoneyGreenSubtle
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MoneyGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Received Today",
                                style = MaterialTheme.typography.labelMedium,
                                color = MoneyGreen
                            )
                            Text(
                                text = "₹$formattedReceived",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryLight
                            )
                        }
                    }
                }
            }
        }
    }
}
