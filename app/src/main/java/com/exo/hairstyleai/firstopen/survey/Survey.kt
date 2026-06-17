package com.exo.hairstyleai.firstopen.survey

import com.exo.hairstyleai.R

/** A survey topic: a translatable [nameRes] + an emoji icon. */
data class SurveyTopic(val nameRes: Int, val emoji: String)

/**
 * The survey is ONE screen for the user, but split across 3 activities (ad-insertion
 * checkpoints). All three show the SAME question and topics — only the current
 * selection differs — so the title/items below are shared by all of them.
 */
object SurveyContent {

    val TITLE = R.string.survey_title

    val TOPICS: List<SurveyTopic> = listOf(
        SurveyTopic(R.string.survey_opt_color, "🎨"),
        SurveyTopic(R.string.survey_opt_cut, "✂️"),
        SurveyTopic(R.string.survey_opt_short, "💇"),
        SurveyTopic(R.string.survey_opt_long, "💁"),
    )

    /** How many topics must stay selected to leave the survey. */
    const val MIN_TO_CONTINUE = 1
}
