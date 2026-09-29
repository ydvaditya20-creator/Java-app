package com.example.zenaral.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

object ImageAttachmentHelper {

    fun createTempPictureUri(context: Context): Uri {
        val cacheDir = File(context.cacheDir, "camera")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val tempFile = File.createTempFile("camera_receipt_", ".jpg", cacheDir)
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, tempFile)
    }

    fun uriToBase64DataUrl(context: Context, uri: Uri, maxDimension: Int = 1024, quality: Int = 80): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return null

            // Scale down if larger than maxDimension to keep JSON payload lightweight & fast
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val targetWidth: Int
                val targetHeight: Int
                if (ratio > 1) {
                    targetWidth = maxDimension
                    targetHeight = (maxDimension / ratio).toInt()
                } else {
                    targetHeight = maxDimension
                    targetWidth = (maxDimension * ratio).toInt()
                }
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Extracts Google Drive File ID from various link formats:
     * - https://drive.google.com/file/d/{FILE_ID}/view?usp=sharing
     * - https://drive.google.com/open?id={FILE_ID}
     * - https://drive.google.com/uc?id={FILE_ID}
     * - https://lh3.googleusercontent.com/d/{FILE_ID}
     */
    fun extractGoogleDriveFileId(url: String): String? {
        val clean = url.trim()
        if (!clean.contains("drive.google.com") && !clean.contains("googleusercontent.com")) {
            return null
        }

        // Pattern 1: /file/d/([a-zA-Z0-9_-]+)
        Regex("""/file/d/([a-zA-Z0-9_-]+)""").find(clean)?.groupValues?.get(1)?.let {
            if (it.isNotBlank()) return it
        }

        // Pattern 2: id=([a-zA-Z0-9_-]+)
        Regex("""[?&]id=([a-zA-Z0-9_-]+)""").find(clean)?.groupValues?.get(1)?.let {
            if (it.isNotBlank()) return it
        }

        // Pattern 3: /d/([a-zA-Z0-9_-]+)
        Regex("""/d/([a-zA-Z0-9_-]+)""").find(clean)?.groupValues?.get(1)?.let {
            if (it.isNotBlank()) return it
        }

        return null
    }

    fun isGoogleDriveLink(url: String): Boolean {
        return extractGoogleDriveFileId(url) != null
    }

    fun isBase64Image(url: String): Boolean {
        val clean = url.trim()
        return clean.startsWith("data:image", ignoreCase = true) ||
               (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true) && clean.length > 80)
    }

    fun base64ToBitmap(url: String): Bitmap? {
        return try {
            val clean = url.trim()
            val base64Data = if (clean.contains(",")) {
                clean.substringAfter(",")
            } else {
                clean
            }
            val decoded = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Returns a direct image URL that Coil AsyncImage can load into an image view.
     * For Google Drive URLs, returns high-resolution thumbnail endpoint.
     */
    fun getDisplayableImageUrl(url: String): String {
        val clean = url.trim()
        val fileId = extractGoogleDriveFileId(clean)
        return if (fileId != null) {
            // Direct thumbnail endpoint supported by Google Drive
            "https://drive.google.com/thumbnail?id=$fileId&sz=w1000"
        } else {
            clean
        }
    }

    /**
     * Returns Google Drive web preview URL suitable for iframe or browser Intent
     */
    fun getDrivePreviewUrl(url: String): String? {
        val fileId = extractGoogleDriveFileId(url) ?: return null
        return "https://drive.google.com/file/d/$fileId/preview"
    }

    /**
     * Returns Google Drive full web view URL
     */
    fun getDriveViewUrl(url: String): String? {
        val fileId = extractGoogleDriveFileId(url) ?: return null
        return "https://drive.google.com/file/d/$fileId/view"
    }
}

