package com.exo.hairstyleai.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/** A FrameLayout locked to a 4:5 (portrait) aspect ratio — used for the
 *  hair-edit photo preview. Height is derived from the measured width, then
 *  the standard FrameLayout pass lays children out against the exact height. */
class FourFiveFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = width * 5 / 4
        val exactHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, exactHeight)
    }
}
