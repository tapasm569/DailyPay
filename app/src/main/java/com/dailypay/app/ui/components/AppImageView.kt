package com.dailypay.app.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dailypay.app.ui.theme.TextSecondaryLight

@Composable
fun AppImageView(
    data: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    placeholderIcon: ImageVector = Icons.Default.Person,
    contentScale: ContentScale = ContentScale.Crop
) {
    val decodedBitmap = remember(data) {
        if (!data.isNullOrBlank() && (data.startsWith("data:image") || data.length > 200)) {
            try {
                val cleanBase64 = if (data.contains(",")) data.substringAfter(",") else data
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    when {
        decodedBitmap != null -> {
            Image(
                bitmap = decodedBitmap,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        !data.isNullOrBlank() && (data.startsWith("http://") || data.startsWith("https://")) -> {
            AsyncImage(
                model = data,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        else -> {
            Box(
                modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = placeholderIcon,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(0.55f),
                    tint = TextSecondaryLight
                )
            }
        }
    }
}

@Composable
fun KycDocumentViewerDialog(
    title: String,
    imageData: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 380.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageData.isNullOrBlank()) {
                    Text("No document image available.", color = TextSecondaryLight)
                } else {
                    AppImageView(
                        data = imageData,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        placeholderIcon = Icons.Default.Description,
                        contentScale = ContentScale.Fit
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
