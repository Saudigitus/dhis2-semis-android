package org.saudigitus.semis.transfer

import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.transfer.model.RelativeTimeUnit
import org.saudigitus.semis.transfer.model.relativeTime
import java.util.Date
import java.util.concurrent.TimeUnit

class RelativeTimeTest {

    @Test
    fun `anything under a minute reads as just now`() {
        assertEquals(RelativeTimeUnit.JUST_NOW, elapsed(TimeUnit.SECONDS, 59).unit)
    }

    @Test
    fun `the coarsest unit that still says something is used`() {
        assertEquals(RelativeTimeUnit.MINUTES, elapsed(TimeUnit.MINUTES, 45).unit)
        assertEquals(RelativeTimeUnit.HOURS, elapsed(TimeUnit.HOURS, 5).unit)
        assertEquals(RelativeTimeUnit.DAYS, elapsed(TimeUnit.DAYS, 3).unit)
        assertEquals(RelativeTimeUnit.WEEKS, elapsed(TimeUnit.DAYS, 10).unit)
        assertEquals(RelativeTimeUnit.MONTHS, elapsed(TimeUnit.DAYS, 60).unit)
        assertEquals(RelativeTimeUnit.YEARS, elapsed(TimeUnit.DAYS, 400).unit)
    }

    @Test
    fun `the amount counts whole units only`() {
        assertEquals(2, elapsed(TimeUnit.DAYS, 20).amount)
        assertEquals(3, elapsed(TimeUnit.DAYS, 95).amount)
    }

    @Test
    fun `a request stamped in the future reads as just now instead of a negative age`() {
        val now = Date(0)
        val future = Date(TimeUnit.HOURS.toMillis(6))

        assertEquals(RelativeTimeUnit.JUST_NOW, relativeTime(from = future, now = now).unit)
    }

    private fun elapsed(unit: TimeUnit, amount: Long) = relativeTime(
        from = Date(0),
        now = Date(unit.toMillis(amount)),
    )
}
