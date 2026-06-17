package com.exo.hairstyleai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.exo.hairstyleai.firstopen.LocaleHelper

/**
 * The main app host. The first-open flow now lives in separate activities
 * (see [com.exo.hairstyleai.firstopen]); the screen flow inside the app (picker →
 * edit → settings) stays in res/navigation/nav_graph.xml with the picker as start.
 */
class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(
                Intent(context, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK,
                ),
            )
        }
    }
}
