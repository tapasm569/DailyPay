package com.dailypay.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailypay.app.ui.theme.BrandPrimary
import com.dailypay.app.util.LocaleHelper

@Composable
fun LanguageSelectionDialog(
    onDismissRequest: () -> Unit
) {
    val currentCode = remember { LocaleHelper.getCurrentLanguageCode() }
    var selectedCode by remember { mutableStateOf(currentCode) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Choose Language / ভাষা নির্বাচন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocaleHelper.supportedLanguages.forEach { lang ->
                    val isSelected = selectedCode == lang.code
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) BrandPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCode = lang.code
                                LocaleHelper.setAppLanguage(lang.code)
                                onDismissRequest()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = lang.englishName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = BrandPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}
