package com.devesj.lifetimechart.util

import android.animation.ArgbEvaluator
import android.graphics.Color

object ColorUtil {
    private val colors = listOf(
        0xFFE57373.toInt(), // 빨강
        0xFF81C784.toInt(), // 초록
        0xFF64B5F6.toInt(), // 파랑
        0xFFFFD54F.toInt(), // 노랑
        0xFFBA68C8.toInt()  // 보라
    )

    fun getColorForItemName(name: String): Int {
        val index = Math.abs(name.hashCode()) % colors.size
        return colors[index]
    }

    fun getColorForValue(value: Int, maxValue: Int): Int {
        val startColor = Color.RED // 시작 색상: 빨간색
        val endColor = Color.parseColor("#800080") // 종료 색상: 보라색 (Hex 코드)

        // 값에 따른 비율 계산 (0.0 ~ 1.0)
        val fraction = value.toFloat() / maxValue

        // 색상 보간
        val evaluator = ArgbEvaluator()
        return evaluator.evaluate(fraction, startColor, endColor) as Int
    }
}