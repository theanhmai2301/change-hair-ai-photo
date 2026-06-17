package com.exo.hairstyleai.firstopen.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.ActivityOnboardingBinding
import com.exo.hairstyleai.firstopen.LocaleHelper
import com.exo.hairstyleai.firstopen.PermissionActivity
import com.exo.hairstyleai.firstopen.isOnboardingDone

/** Onboarding — 3 swipeable pages, then the notification step. */
class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val dots = mutableListOf<View>()

    private val pages = listOf(
        OnboardingPagerAdapter.Page(R.drawable.ob_shot1, R.string.ob1_title, R.string.ob1_desc),
        OnboardingPagerAdapter.Page(R.drawable.ob_shot2, R.string.ob2_title, R.string.ob2_desc),
        OnboardingPagerAdapter.Page(R.drawable.ob_shot3, R.string.ob3_title, R.string.ob3_desc),
    )

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.onboardingPager.adapter = OnboardingPagerAdapter(pages)
        initDots(pages.size)

        binding.onboardingPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateDots(position)
                    binding.nextText.setText(
                        if (position == pages.lastIndex) R.string.ob_start else R.string.common_continue,
                    )
                }
            },
        )
        updateDots(0)

        binding.nextButton.setOnClickListener {
            val current = binding.onboardingPager.currentItem
            if (current == pages.lastIndex) finishOnboarding()
            else binding.onboardingPager.currentItem = current + 1
        }
        // TODO: ADS — loadNativeAd(binding.frAds)
    }

    private fun finishOnboarding() {
        isOnboardingDone = true
        PermissionActivity.start(this)
        finish()
    }

    private fun initDots(count: Int) {
        binding.dots.removeAllViews()
        dots.clear()
        repeat(count) {
            val v = View(this)
            val lp = LinearLayout.LayoutParams(dp(7), dp(7))
            lp.marginStart = dp(4)
            lp.marginEnd = dp(4)
            v.layoutParams = lp
            v.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(4).toFloat()
            }
            binding.dots.addView(v)
            dots.add(v)
        }
    }

    private fun updateDots(active: Int) {
        dots.forEachIndexed { i, v ->
            val lp = v.layoutParams as LinearLayout.LayoutParams
            lp.width = dp(if (i == active) 22 else 7)
            v.layoutParams = lp
            (v.background as GradientDrawable).setColor(
                ContextCompat.getColor(this, if (i == active) R.color.accent else R.color.text_disabled),
            )
        }
    }

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics,
    ).toInt()

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, OnBoardingActivity::class.java))
        }
    }
}
