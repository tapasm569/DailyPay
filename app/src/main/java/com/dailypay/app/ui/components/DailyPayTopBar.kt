package com.dailypay.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.dailypay.app.ui.theme.AlertOrange
import com.dailypay.app.ui.theme.AlertOrangeSubtle
import com.dailypay.app.ui.theme.BrandPrimary
import com.dailypay.app.ui.theme.DangerRed
import com.dailypay.app.ui.theme.MoneyGreen
import com.dailypay.app.ui.theme.MoneyGreenSubtle
import com.dailypay.app.ui.theme.TextPrimaryLight
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

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            offset = DpOffset(x = 0.dp, y = 8.dp),
                            properties = PopupProperties(
                                focusable = true,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            ),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
