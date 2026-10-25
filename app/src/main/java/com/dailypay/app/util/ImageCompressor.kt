package com.dailypay.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageCompressor {
    fun compressAndEncodeBase64(imageBytes: ByteArray?, maxDim: Int = 800, quality: Int = 75): String? {
        if (imageBytes == null || imageBytes.isEmpty()) return null
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

            var inSampleSize = 1
            if (options.outHeight > maxDim || options.outWidth > maxDim) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, decodeOptions) ?: return null

            val outStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
            val compressed = outStream.toByteArray()
            "data:image/jpeg;base64," + Base64.encodeToString(compressed, Base64.NO_WRAP)
        } catch (_: Exception) {
            "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        }
    }
}
