package com.exo.hairstyleai.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.exo.hairstyleai.data.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

/** Downloads the generated image and writes it into the device gallery. */
object ImageSaver {

    suspend fun saveToGallery(
        context: Context,
        imageUrl: String,
        displayName: String,
    ): Boolean = withContext(Dispatchers.IO) {
        val bytes = downloadBytes(imageUrl) ?: return@withContext false
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Hairify",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val itemUri = resolver.insert(collection, values) ?: return@withContext false
        try {
            resolver.openOutputStream(itemUri)?.use { it.write(bytes) }
                ?: return@withContext false
        } catch (e: Exception) {
            resolver.delete(itemUri, null, null)
            return@withContext false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(itemUri, values, null, null)
        }
        true
    }

    private fun downloadBytes(url: String): ByteArray? = try {
        val request = Request.Builder().url(url).build()
        ApiClient.okHttp.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.bytes() else null
        }
    } catch (e: Exception) {
        null
    }
}
