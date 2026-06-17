package com.exo.hairstyleai.firstopen.survey

import android.content.Context
import android.content.Intent

/** Survey step 2 — exactly 1 selected (carried in). Hands off when a 2nd topic is picked. */
class SurveyDupActivity : BaseSurveyActivity() {

    override val advanceAt = 2

    override fun advance(selected: IntArray) {
        Survey3Activity.start(this, selected)
    }

    companion object {
        fun start(context: Context, selected: IntArray) {
            context.startActivity(
                Intent(context, SurveyDupActivity::class.java)
                    .putExtra(BaseSurveyActivity.EXTRA_SELECTED, selected),
            )
        }
    }
}
