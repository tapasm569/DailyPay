package com.dailypay.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dailypay.app.ui.theme.*

data class AdminMenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit
)

@Composable
fun AdminDashboardScreen(
    onCreateLenderClick: () -> Unit,
    onManageLenderClick: () -> Unit,
    onSubscriptionClick: () -> Unit,
    onReminderClick: () -> Unit,
    onResetPasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItems = listOf(
        AdminMenuItem(
            title = "Create Lender",
            description = "Register new lender profile",
            icon = Icons.Default.PersonAdd,
            accentColor = BrandPrimary,
            onClick = onCreateLenderClick
        ),
        AdminMenuItem(
            title = "Manage Lender",
            description = "View and update profiles",
            icon = Icons.Default.ManageAccounts,
            accentColor = CallBlue,
            onClick = onManageLenderClick
        ),
        AdminMenuItem(
            title = "Subscriptions",
            description = "Track 30-day active trials",
            icon = Icons.Default.CardMembership,
            accentColor = MoneyGreen,
            onClick = onSubscriptionClick
        ),
        AdminMenuItem(
            title = "Reminders",
            description = "Subscriptions ending in 10 days",
            icon = Icons.Default.NotificationImportant,
            accentColor = AlertOrange,
            onClick = onReminderClick
        ),
        AdminMenuItem(
            title = "Password Requests",
            description = "Approve & send reset OTPs",
            icon = Icons.Default.LockReset,
            accentColor = DangerRed,
            onClick = onResetPasswordClick
        )
    )

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Admin Panel",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Super Administrator Dashboard",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }

                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DangerRedSubtle)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = DangerRed
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtleLight, RoundedCornerShape(18.dp))
                        .clickable { item.onClick() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(item.accentColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextSecondaryLight
                        )
                    }
                }
            }
        }
    }
}
