package com.exo.hairstyleai.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Helpers for staging local photos in the app cache before upload. */
object FileUtils {

    private fun captureDir(context: Context): File =
        File(context.cacheDir, "captures").apply { mkdirs() }

    /** A fresh empty file (+ its FileProvider Uri) for the camera to write into. */
    fun newCaptureTarget(context: Context): Pair<File, Uri> {
        val file = File(captureDir(context), "cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return file to uri
    }

    /** Copy a picked gallery image into the cache and return the backing file. */
    fun copyToCache(context: Context, uri: Uri): File {
        val file = File(captureDir(context), "upload_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Không đọc được ảnh đã chọn." }
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }
}
