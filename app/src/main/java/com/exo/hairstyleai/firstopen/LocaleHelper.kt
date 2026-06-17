package com.exo.hairstyleai.firstopen

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies the chosen UI language by wrapping the activity's base context — so a screen
 * is born in the right language WITHOUT recreating (no white/black flash during the
 * first-open flow). Every activity in the app overrides:
 *
 *     override fun attachBaseContext(newBase: Context) {
 *         super.attachBaseContext(LocaleHelper.wrap(newBase))
 *     }
 *
 * Use [Locale] (legacy ctor) so code "in" (Indonesian) keeps matching values-in/.
 */
object LocaleHelper {

    fun wrap(base: Context): Context {
        val code = base.languageCode ?: return base
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
