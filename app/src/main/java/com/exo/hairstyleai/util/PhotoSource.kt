package com.exo.hairstyleai.util

import java.io.File

/**
 * Where the photo to edit comes from.
 *  - [Remote]   : a sample image hosted by the backend → sent as `image_url`.
 *  - [LocalFile]: a gallery pick or camera capture copied into our cache →
 *                 sent as a multipart `file`. Keeping a real [File] (instead of
 *                 a content Uri) sidesteps cross-activity permission grants.
 */
sealed class PhotoSource {
    data class Remote(val url: String) : PhotoSource()
    data class LocalFile(val file: File, val fromCamera: Boolean = false) : PhotoSource()

    /** The object Coil should load for previews. */
    val model: Any
        get() = when (this) {
            is Remote -> url
            is LocalFile -> file
        }
}
