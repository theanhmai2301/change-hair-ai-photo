package com.exo.hairstyleai.firstopen

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.exo.hairstyleai.MainActivity
import com.exo.hairstyleai.firstopen.language.LanguageFO1Activity
import com.exo.hairstyleai.firstopen.onboarding.OnBoardingActivity
import com.exo.hairstyleai.firstopen.survey.SurveyActivity

/**
 * Launcher + orchestrator. Shows the system splash, then routes to the first
 * incomplete first-open step (or straight to the app if everything is done).
 */
class SplashActivity : AppCompatActivity() {

    private var ready = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { !ready }

        // TODO: ADS — preload native/interstitial for the next screen here.
        Handler(Looper.getMainLooper()).postDelayed({
            ready = true
            routeNext()
            finish()
        }, SPLASH_DELAY_MS)
    }

    private fun routeNext() {
        when {
            languageCode == null -> LanguageFO1Activity.start(this)
            !isSurveyDone -> SurveyActivity.start(this)
            !isOnboardingDone -> OnBoardingActivity.start(this)
            else -> MainActivity.start(this)
        }
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1000L
    }
}
