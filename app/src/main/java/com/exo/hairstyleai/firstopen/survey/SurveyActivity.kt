package com.exo.hairstyleai.firstopen.survey

import android.content.Context
import android.content.Intent

/** Survey step 1 — nothing selected yet. Hands off as soon as the 1st topic is picked. */
class SurveyActivity : BaseSurveyActivity() {

    override val advanceAt = 1

    override fun advance(selected: IntArray) {
        SurveyDupActivity.start(this, selected)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SurveyActivity::class.java))
        }
    }
}
