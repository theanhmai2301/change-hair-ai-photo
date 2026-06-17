package com.exo.hairstyleai.firstopen.language

import android.content.Context

/** One selectable UI language. [flag] is an emoji flag; [code] matches values-<code>/. */
data class Language(val flag: String, val name: String, val code: String)

object Languages {

    val ALL: List<Language> = listOf(
        Language("🇻🇳", "Tiếng Việt", "vi"),
        Language("🇺🇸", "English", "en"),
        Language("🇪🇸", "Español", "es"),
        Language("🇵🇹", "Português", "pt"),
        Language("🇫🇷", "Français", "fr"),
        Language("🇩🇪", "Deutsch", "de"),
        Language("🇮🇳", "हिन्दी", "hi"),
        Language("🇮🇩", "Bahasa Indonesia", "in"),
        Language("🇯🇵", "日本語", "ja"),
        Language("🇰🇷", "한국어", "ko"),
        Language("🇨🇳", "中文", "zh"),
    )

    /** List with [preferred] (or the device language) moved to the top. */
    fun ordered(context: Context, preferred: String?): List<Language> {
        val target = preferred ?: context.resources.configuration.locales[0].language
        val top = ALL.firstOrNull { it.code == target }
        return if (top == null) ALL else listOf(top) + ALL.filterNot { it.code == top.code }
    }
}
