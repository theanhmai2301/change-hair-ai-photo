package com.exo.hairstyleai.firstopen.survey

import android.content.Context
import android.content.Intent

/**
 * Survey step 3 (final) — 2+ selected (carried in). No more hand-offs; the base
 * Continue handler marks the survey done and moves on to onboarding.
 */
class Survey3Activity : BaseSurveyActivity() {

    companion object {
        fun start(context: Context, selected: IntArray) {
            context.startActivity(
                Intent(context, Survey3Activity::class.java)
                    .putExtra(BaseSurveyActivity.EXTRA_SELECTED, selected),
            )
        }
    }
}
