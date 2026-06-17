package com.exo.hairstyleai.firstopen.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.exo.hairstyleai.databinding.ActivityLanguageFo1Binding
import com.exo.hairstyleai.firstopen.LocaleHelper

/** Language screen #1 — tap a language to go to the confirm screen. */
class LanguageFO1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageFo1Binding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageFo1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = Languages.ordered(this, null)
        binding.languageList.layoutManager = LinearLayoutManager(this)
        binding.languageList.adapter = LanguageAdapter(items, null) { lang ->
            LanguageFO2Activity.start(this, lang.code)
            finish()
        }
        // TODO: ADS — loadNativeAd(binding.frAds)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LanguageFO1Activity::class.java))
        }
    }
}
