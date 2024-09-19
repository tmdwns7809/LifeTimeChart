package com.devesj.lifetimechart.util

import android.graphics.Color

object ColorUtil {
    fun getColorForValue(value: Int, maxValue: Int): Int {
        // 값에 따른 비율 계산 (0.0 ~ 1.0)
        val fraction = value.toFloat() / maxValue

        return Color.HSVToColor(floatArrayOf(fraction * 290, 0.7f, 1.0f))
    }
}