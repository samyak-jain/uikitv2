package io.agora.uikit.ui

import android.content.Context
import android.util.TypedValue

fun DPToPx(context: Context, DP: Int): Int {
    val res = context.resources
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP.toFloat(), res.displayMetrics).toInt()
}