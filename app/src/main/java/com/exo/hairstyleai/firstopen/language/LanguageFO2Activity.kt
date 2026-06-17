package com.exo.hairstyleai.firstopen.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.exo.hairstyleai.MainActivity
import com.exo.hairstyleai.databinding.ActivityLanguageFo2Binding
import com.exo.hairstyleai.firstopen.LocaleHelper
import com.exo.hairstyleai.firstopen.languageCode
import com.exo.hairstyleai.firstopen.survey.SurveyActivity

/** Language screen #2 — pre-selected list + Confirm. Reused from Settings too. */
class LanguageFO2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageFo2Binding
    private lateinit var adapter: LanguageAdapter

    private var selectedCode: String? = null
    private var fromMain = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageFo2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        fromMain = intent.getBooleanExtra(EXTRA_FROM_MAIN, false)
        selectedCode = intent.getStringExtra(EXTRA_CODE)
            ?: languageCode
            ?: resources.configuration.locales[0].language

        val items = Languages.ordered(this, selectedCode)
        adapter = LanguageAdapter(items, selectedCode) { lang ->
            selectedCode = lang.code
            adapter.setSelected(lang.code)
        }
        binding.languageList.layoutManager = LinearLayoutManager(this)
        binding.languageList.adapter = adapter

        binding.confirmButton.setOnClickListener { confirm() }
        // TODO: ADS — loadNativeAd(binding.frAds)
    }

    private fun confirm() {
        val code = selectedCode ?: return
        val changed = code != languageCode
        languageCode = code

        if (fromMain) {
            // Relaunch the app so every screen is rebuilt in the new language.
            if (changed) {
                startActivity(
                    Intent(this, MainActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK,
                    ),
                )
            }
            finish()
        } else {
            SurveyActivity.start(this)
            finish()
        }
    }

    companion object {
        private const val EXTRA_CODE = "extra_code"
        private const val EXTRA_FROM_MAIN = "extra_from_main"

        fun start(context: Context, code: String) {
            context.startActivity(
                Intent(context, LanguageFO2Activity::class.java).putExtra(EXTRA_CODE, code),
            )
        }

        /** Opened from the in-app Settings (shows Confirm, relaunches app on change). */
        fun startFromMain(context: Context) {
            context.startActivity(
                Intent(context, LanguageFO2Activity::class.java).putExtra(EXTRA_FROM_MAIN, true),
            )
        }
    }
}
