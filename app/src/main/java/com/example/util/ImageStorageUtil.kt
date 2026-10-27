package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageUtil {

    fun createTempPictureUri(context: Context): Pair<Uri, File> {
        val storageDir = File(context.cacheDir, "camera_cache").apply { mkdirs() }
        val tempFile = File.createTempFile("camera_${System.currentTimeMillis()}_", ".jpg", storageDir)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, tempFile)
        return Pair(uri, tempFile)
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, folder: String = "catalog_images"): String {
        val directory = File(context.filesDir, folder).apply { mkdirs() }
        val fileName = "choc_${UUID.randomUUID()}.jpg"
        val destFile = File(directory, fileName)

        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return destFile.absolutePath
    }

    fun saveUriToInternalStorage(context: Context, sourceUri: Uri, folder: String = "catalog_images"): String? {
        return try {
            val directory = File(context.filesDir, folder).apply { mkdirs() }
            val fileName = "choc_${UUID.randomUUID()}.jpg"
            val destFile = File(directory, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            inputStream?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun loadBitmapFromFilePath(path: String): Bitmap? {
        return try {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
