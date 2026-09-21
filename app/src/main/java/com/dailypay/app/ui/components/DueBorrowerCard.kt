package com.dailypay.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val arrears = maxOf(0.0, item.todayDueBalance - item.dailyInstallment)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtleLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            CommunicationUtils.openWhatsAppChat(
                                context = context,
                                rawMobileNumber = item.borrowerMobile,
                                message = "Hello ${item.borrowerName}, your total pending installment for today is ₹${item.todayDueBalance} (Base EMI: ₹${item.dailyInstallment}${if (arrears > 0) " + Previous Due: ₹$arrears" else ""}). Please clear it on DailyPay."
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
                        onClick = { CommunicationUtils.openPhoneDialer(context, item.borrowerMobile) },
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

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSubtleLight)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Due Today", style = MaterialTheme.typography.labelMedium, color = AlertOrange)
                    Text(
                        text = "₹${item.todayDueBalance}",
                        style = MaterialTheme.typography.titleLarge,
                        color = AlertOrange
                    )
                    if (arrears > 0.0) {
                        Text(
                            text = "(EMI ₹${item.dailyInstallment} + Due ₹$arrears)",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Paid Today", style = MaterialTheme.typography.labelMedium, color = MoneyGreen)
                    Text("₹${item.todayPaidAmount}", style = MaterialTheme.typography.titleMedium, color = MoneyGreen)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining", style = MaterialTheme.typography.labelMedium, color = TextSecondaryLight)
                    Text("₹${item.remainingBalance}", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onCollectPaymentClick(item) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Collect Payment")
            }
        }
    }
}
