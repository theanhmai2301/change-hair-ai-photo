package com.exo.hairstyleai.data

import android.graphics.Color
import com.exo.hairstyleai.data.api.ApiClient
import com.exo.hairstyleai.data.model.HairCategory
import com.exo.hairstyleai.data.model.HairPreset
import com.exo.hairstyleai.data.model.HairResult
import com.exo.hairstyleai.data.model.Swatch
import com.exo.hairstyleai.util.PhotoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Talks to the exo-image-ai backend and adapts its payloads into the domain
 * model. Color presets only re-color (keep the cut); cut presets change the
 * hairstyle (keep the natural color). Face identity is preserved.
 */
object HairRepository {

    private val TEXT = "text/plain".toMediaType()

    /** The catalog has no category field — these 10 ids are "color only". */
    private val COLOR_IDS = setOf(
        "baby_blue", "blush_pink", "cherry_cola", "cherry_red", "chestnut_brown",
        "cool_brown", "ginger_spice", "lavender", "neutral_blondes", "pastel_rainbown",
    )

    /** Representative swatch per color preset. */
    private val COLOR_SWATCH = mapOf(
        "baby_blue" to "#7EC8E3",
        "blush_pink" to "#F4A6C0",
        "cherry_cola" to "#5A1F1F",
        "cherry_red" to "#D62828",
        "chestnut_brown" to "#6B4226",
        "cool_brown" to "#8B5E3C",
        "ginger_spice" to "#C45A28",
        "lavender" to "#B39DDB",
        "neutral_blondes" to "#E6C992",
    )

    /** Load the full catalog and split it into color / cut presets. */
    suspend fun loadPresets(): List<HairPreset> = withContext(Dispatchers.IO) {
        val response = ApiClient.api.getHairstyles()
        response.hairstyles.map { raw ->
            val isColor = COLOR_IDS.contains(raw.id)
            val swatch: Swatch? = when {
                !isColor -> null
                raw.id == "pastel_rainbown" -> Swatch.Rainbow
                else -> COLOR_SWATCH[raw.id]?.let { Swatch.Solid(Color.parseColor(it)) }
            }
            HairPreset(
                id = raw.id,
                name = raw.name,
                previewUrl = raw.preview_url,
                category = if (isColor) HairCategory.COLOR else HairCategory.CUT,
                swatch = swatch,
            )
        }
    }

    /** Apply a preset to the chosen photo. Returns the generated image + meta. */
    suspend fun applyHair(
        source: PhotoSource,
        hair: String,
        quality: String = "medium",
    ): HairResult = withContext(Dispatchers.IO) {
        val hairPart = hair.toRequestBody(TEXT)
        val qualityPart = quality.toRequestBody(TEXT)

        when (source) {
            is PhotoSource.Remote ->
                ApiClient.api.applyHairUrl(
                    imageUrl = source.url.toRequestBody(TEXT),
                    hair = hairPart,
                    quality = qualityPart,
                )

            is PhotoSource.LocalFile -> {
                val body = source.file.asRequestBody("image/*".toMediaType())
                val part = MultipartBody.Part.createFormData("file", "selfie.jpg", body)
                ApiClient.api.applyHairFile(
                    file = part,
                    hair = hairPart,
                    quality = qualityPart,
                )
            }
        }
    }
}
