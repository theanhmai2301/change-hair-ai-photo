package com.exo.hairstyleai.firstopen

import android.content.Context
import java.io.File

/**
 * Per-step first-open state, stored in the dedicated "first_open" SharedPreferences
 * so it can be excluded from Auto Backup. Each step writes its own flag, so reopening
 * the app resumes at the first incomplete step (see SplashActivity).
 */
private const val PREFS_NAME = "first_open"
private const val KEY_LANGUAGE = "language_code"
private const val KEY_SURVEY = "complete_survey"
private const val KEY_ONBOARDING = "complete_onboarding"
private const val MARKER_NAME = "first_open.initialized"

private fun Context.firstOpenPrefs() =
    applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

/** The UI language the user picked, or null if they haven't chosen yet. */
var Context.languageCode: String?
    get() = firstOpenPrefs().getString(KEY_LANGUAGE, null)
    set(value) = firstOpenPrefs().edit().putString(KEY_LANGUAGE, value).apply()

var Context.isSurveyDone: Boolean
    get() = firstOpenPrefs().getBoolean(KEY_SURVEY, false)
    set(value) = firstOpenPrefs().edit().putBoolean(KEY_SURVEY, value).apply()

var Context.isOnboardingDone: Boolean
    get() = firstOpenPrefs().getBoolean(KEY_ONBOARDING, false)
    set(value) = firstOpenPrefs().edit().putBoolean(KEY_ONBOARDING, value).apply()

/**
 * A fresh install (or a backup/device-transfer restore) must run the first-open flow
 * again. The marker lives in [Context.getNoBackupFilesDir], which is never restored by
 * Auto Backup — so its absence reliably means "this install hasn't onboarded yet".
 * Call once from [android.app.Application.onCreate] before any routing.
 */
fun Context.ensureFreshInstallResetsFirstOpen() {
    val marker = File(noBackupFilesDir, MARKER_NAME)
    if (!marker.exists()) {
        firstOpenPrefs().edit().clear().apply()
        runCatching { marker.createNewFile() }
    }
}
