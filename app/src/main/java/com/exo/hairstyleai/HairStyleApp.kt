package com.exo.hairstyleai

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.exo.hairstyleai.firstopen.ensureFreshInstallResetsFirstOpen
import com.exo.hairstyleai.util.Prefs

class HairStyleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Reset first-open state on a genuinely fresh install (defeats Auto Backup).
        ensureFreshInstallResetsFirstOpen()
        // Apply the saved light/dark/system theme before any activity is created.
        AppCompatDelegate.setDefaultNightMode(Prefs(this).themeMode)
    }
}
