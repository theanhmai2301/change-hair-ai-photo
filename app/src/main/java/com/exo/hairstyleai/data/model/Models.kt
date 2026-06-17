package com.exo.hairstyleai.data.model

/* ── Raw API DTOs — field names mirror the JSON returned by the backend ── */

/** GET /v1/hairstyles */
data class HairstylesResponse(
    val count: Int = 0,
    val hairstyles: List<RawHairstyle> = emptyList(),
)

data class RawHairstyle(
    val id: String,
    val name: String,
    val before_url: String? = null,
    val preview_url: String? = null,
)

/** POST /v1/hair success body. */
data class HairResult(
    val feature: String? = null,
    val url: String? = null,
    val image_base64: String? = null,
    val mime_type: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val seed: Long = 0,
    val tier: String? = null,
    val cost: Double = 0.0,
    val request_id: String? = null,
)

/* ── Domain model consumed by the UI ── */

enum class HairCategory { COLOR, CUT }

data class HairPreset(
    val id: String,
    val name: String,
    val previewUrl: String?,
    val category: HairCategory,
    val swatch: Swatch?,
)

/** Little ring drawn on color tiles. */
sealed class Swatch {
    data class Solid(val color: Int) : Swatch()
    /** Multicolor pastel ring (pastel_rainbown). */
    object Rainbow : Swatch()
}
