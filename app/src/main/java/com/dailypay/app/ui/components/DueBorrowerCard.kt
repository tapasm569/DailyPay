package com.dailypay.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dailypay.app.R
import com.dailypay.app.data.model.DailyDueItem
import com.dailypay.app.ui.theme.*
import com.dailypay.app.util.CommunicationUtils

@Composable
fun DueBorrowerCard(
    item: DailyDueItem,
    onCollectPaymentClick: (DailyDueItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPaidToday = item.todayPaidAmount >= item.todayDueBalance && item.todayDueBalance > 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtleLight, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Name & Communication Shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.borrowerName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+91 ${item.borrowerMobile}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight
                    )
                }

                // WhatsApp & Call Icon Actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            CommunicationUtils.openWhatsAppChat(
                                context = context,
                                rawMobileNumber = item.borrowerMobile,
                                message = "Hello ${item.borrowerName}, reminder from DailyPay: Today's due installment is ₹${item.todayDueBalance}. Please pay on time."
                            )
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.dp, WhatsAppGreen.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_whatsapp),
                            contentDescription = "WhatsApp",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            CommunicationUtils.openPhoneDialer(context, item.borrowerMobile)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.dp, CallBlue.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_call),
                            contentDescription = "Call",
                            tint = CallBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderSubtleLight)
            Spacer(modifier = Modifier.height(14.dp))

            // Metrics: Today's Due, Paid, and Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Today's Due", style = MaterialTheme.typography.labelMedium, color = AlertOrange)
                    Text(
                        text = "₹${item.todayDueBalance}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AlertOrange
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Paid Today", style = MaterialTheme.typography.labelMedium, color = MoneyGreen)
                    Text(
                        text = "₹${item.todayPaidAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MoneyGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Outstanding", style = MaterialTheme.typography.labelMedium, color = TextSecondaryLight)
                    Text(
                        text = "₹${item.remainingBalance}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            if (!isPaidToday) {
                Button(
                    onClick = { onCollectPaymentClick(item) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Collect Payment")
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MoneyGreenSubtle
                ) {
                    Text(
                        text = "✓ Today's Installment Paid",
                        modifier = Modifier.padding(vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MoneyGreen,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
