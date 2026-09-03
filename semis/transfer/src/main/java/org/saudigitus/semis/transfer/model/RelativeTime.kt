package org.saudigitus.semis.transfer.model

import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * How long ago a request was raised, in the coarsest unit that still says something
 * useful. Shown under the status so that a school can tell at a glance which requests
 * have been waiting.
 */
enum class RelativeTimeUnit {
    JUST_NOW,
    MINUTES,
    HOURS,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS,
}

data class RelativeTime(
    val unit: RelativeTimeUnit,
    val amount: Int,
)

private const val MINUTES_IN_HOUR = 60L
private const val HOURS_IN_DAY = 24L
private const val DAYS_IN_WEEK = 7L
private const val DAYS_IN_MONTH = 30L
private const val DAYS_IN_YEAR = 365L

/**
 * Measures [from] against [now].
 *
 * A request raised in the future is reported as just now rather than as a negative
 * amount: clocks between devices and the server do drift, and a request cannot
 * meaningfully be pending for less than no time.
 */
internal fun relativeTime(from: Date, now: Date): RelativeTime {
    val elapsedMillis = (now.time - from.time).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
    val hours = minutes / MINUTES_IN_HOUR
    val days = hours / HOURS_IN_DAY

    return when {
        minutes < 1 -> RelativeTime(RelativeTimeUnit.JUST_NOW, 0)
        hours < 1 -> RelativeTime(RelativeTimeUnit.MINUTES, minutes.toInt())
        days < 1 -> RelativeTime(RelativeTimeUnit.HOURS, hours.toInt())
        days < DAYS_IN_WEEK -> RelativeTime(RelativeTimeUnit.DAYS, days.toInt())
        days < DAYS_IN_MONTH -> RelativeTime(
            RelativeTimeUnit.WEEKS,
            (days / DAYS_IN_WEEK).toInt(),
        )

        days < DAYS_IN_YEAR -> RelativeTime(
            RelativeTimeUnit.MONTHS,
            (days / DAYS_IN_MONTH).toInt(),
        )

        else -> RelativeTime(RelativeTimeUnit.YEARS, (days / DAYS_IN_YEAR).toInt())
    }
}
