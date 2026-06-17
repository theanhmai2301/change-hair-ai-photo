package com.exo.hairstyleai.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/** Tiny SharedPreferences wrapper for app-level flags. */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("hairstyle_prefs", Context.MODE_PRIVATE)

    /** AppCompatDelegate night-mode constant (system / light / dark). */
    var themeMode: Int
        get() = sp.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = sp.edit().putInt(KEY_THEME, value).apply()

    /** False until the user finishes the first-open flow (language → survey → OB → permission). */
    var onboardingDone: Boolean
        get() = sp.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = sp.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    /** Optional: store the survey answer so we don't ask again. */
    var surveyAnswer: String?
        get() = sp.getString(KEY_SURVEY, null)
        set(value) = sp.edit().putString(KEY_SURVEY, value).apply()

    companion object {
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_SURVEY = "survey_answer"
        private const val KEY_THEME = "theme_mode"
    }
}
