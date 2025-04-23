package com.android.peoplefinder.activity.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class AspectRatioImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (drawable != null) {
            val width = measuredWidth
            val height = (width * drawable.intrinsicHeight / drawable.intrinsicWidth.toFloat()).toInt()
            setMeasuredDimension(width, height)
        }
    }
}