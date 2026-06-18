package com.exo.hairstyleai.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import com.exo.hairstyleai.data.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

/** Resolves the generated image bytes and writes them into the device gallery. */
object ImageSaver {

    /**
     * Get the result bytes with as little network as possible: the backend returns
     * the image inline as base64, so decode that (zero downloads); only fall back to
     * fetching the hosted URL — a single time — when base64 is absent. The caller
     * holds onto the bytes and reuses them for the preview and the save.
     */
    suspend fun fetchBytes(base64: String?, url: String?): ByteArray? = withContext(Dispatchers.IO) {
        if (!base64.isNullOrEmpty()) {
            // Tolerate a "data:image/png;base64,..." prefix.
            val raw = base64.substringAfter("base64,", base64)
            return@withContext try {
                Base64.decode(raw, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        if (!url.isNullOrEmpty()) return@withContext downloadBytes(url)
        null
    }

    /** Write already-in-hand image bytes into the gallery — no download. */
    suspend fun saveBytes(
        context: Context,
        bytes: ByteArray,
        displayName: String,
    ): Boolean = withContext(Dispatchers.IO) {
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
