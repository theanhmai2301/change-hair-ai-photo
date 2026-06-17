package com.exo.hairstyleai.firstopen.survey

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.ActivitySurveyBinding
import com.exo.hairstyleai.firstopen.LocaleHelper
import com.exo.hairstyleai.firstopen.isSurveyDone
import com.exo.hairstyleai.firstopen.onboarding.OnBoardingActivity

/**
 * The survey is ONE screen for the user, implemented as 3 step-activities that swap in
 * seamlessly (no window animation) as the selection grows — each swap is an ad checkpoint.
 * All steps render the SAME title + topics ([SurveyContent]); they differ only in when they
 * hand off:
 *   - step 1 ([SurveyActivity])     : 0 selected → hands off the moment the 1st topic is picked
 *   - step 2 ([SurveyDupActivity])  : 1 selected → hands off when a 2nd topic is picked
 *   - step 3 ([Survey3Activity])    : 2+ selected, final → Continue leaves the survey
 *
 * The current selection is carried across steps via [EXTRA_SELECTED], so the user never sees
 * it reset — to them it is a single uninterrupted screen.
 */
abstract class BaseSurveyActivity : AppCompatActivity() {

    protected lateinit var binding: ActivitySurveyBinding
    private lateinit var adapter: SurveyAdapter

    /** Selected-count at which this step hands off to the next; null = final step (no hand-off). */
    protected open val advanceAt: Int? = null

    /** Open the next step, carrying [selected] forward. Called once the threshold is crossed. */
    protected open fun advance(selected: IntArray) {}

    /** Leave the survey for good — mark it done and move on to onboarding. */
    protected open fun finishSurvey() {
        isSurveyDone = true
        OnBoardingActivity.start(this)
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.setText(SurveyContent.TITLE)

        val initial = intent.getIntArrayExtra(EXTRA_SELECTED) ?: IntArray(0)
        adapter = SurveyAdapter(SurveyContent.TOPICS, initial) { sel -> onSelectionChanged(sel) }
        binding.surveyGrid.layoutManager = GridLayoutManager(this, 2)
        binding.surveyGrid.adapter = adapter
        updateContinue(initial.size)

        binding.nextButton.setOnClickListener {
            if (adapter.selected().size >= SurveyContent.MIN_TO_CONTINUE) {
                finishSurvey()
            } else {
                Toast.makeText(this, R.string.survey_min_select, Toast.LENGTH_SHORT).show()
            }
        }
        // TODO: ADS — loadNativeAd(binding.frAds); show an interstitial on each step hand-off.
    }

    private fun onSelectionChanged(selected: IntArray) {
        val threshold = advanceAt
        if (threshold != null && selected.size >= threshold) {
            advance(selected)
            finish()
            return
        }
        updateContinue(selected.size)
    }

    private fun updateContinue(count: Int) {
        val ok = count >= SurveyContent.MIN_TO_CONTINUE
        // Stays tappable even when not ready, so an empty tap can show the hint toast;
        // the surface background is the "not ready" look (replaces the disabled selector state).
        binding.nextButton.setBackgroundResource(
            if (ok) R.drawable.bg_btn_primary else R.drawable.bg_btn_surface,
        )
        binding.nextText.setTextColor(
            ContextCompat.getColor(this, if (ok) R.color.white else R.color.text_disabled),
        )
    }

    companion object {
        const val EXTRA_SELECTED = "extra_selected"
    }
}
