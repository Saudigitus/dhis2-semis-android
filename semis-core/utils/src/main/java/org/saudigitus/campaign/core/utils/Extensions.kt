package org.saudigitus.campaign.core.utils

import android.content.Context

fun Int.dp(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

fun String.formatTrueOnly(): String = when (this) {
    "1", "true" -> true.toString()
    else -> ""
}