package org.saudigitus.semis.core.data.model.schoolcalendar_config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SchoolDayRulesTest {

    private val today = LocalDate.parse("2025-07-31")

    private val monToWed = WeekDays(
        monday = true,
        tuesday = true,
        wednesday = true,
        thursday = false,
        friday = false,
        saturday = false,
        sunday = false,
    )

    private val calendar = SchoolCalendar(
        id = "cal-2025",
        academicYear = AcademicYear(
            code = "2025",
            label = "2025",
            description = "2025",
            startDate = "2025-01-01",
            endDate = "2025-12-31",
        ),
        classPeriods = emptyList(),
        holidays = listOf(Holiday(date = "2025-05-01", event = "Workers Day", type = "public")),
        weekDays = monToWed,
    )

    @Test
    fun `a day the school teaches on is allowed`() {
        // 2025-07-09 is a Wednesday
        assertTrue(isSchoolDay(LocalDate.parse("2025-07-09"), today, calendar))
    }

    @Test
    fun `a day the school does not teach on is refused`() {
        // 2025-07-10 is a Thursday, which this school does not teach on
        assertFalse(isSchoolDay(LocalDate.parse("2025-07-10"), today, calendar))
    }

    @Test
    fun `a weekend the school does teach on is allowed`() {
        val teachesSaturday = calendar.copy(weekDays = monToWed.copy(saturday = true))

        // 2025-07-12 is a Saturday
        assertTrue(isSchoolDay(LocalDate.parse("2025-07-12"), today, teachesSaturday))
    }

    @Test
    fun `a holiday is refused even though the school teaches that weekday`() {
        // 2025-05-01 is a Thursday, so make the school teach it to isolate the holiday
        val teachesThursday = calendar.copy(weekDays = monToWed.copy(thursday = true))

        assertFalse(isSchoolDay(LocalDate.parse("2025-05-01"), today, teachesThursday))
    }

    @Test
    fun `a calendar listing no holidays excludes nothing`() {
        val noHolidays = calendar.copy(holidays = null)

        assertTrue(isSchoolDay(LocalDate.parse("2025-07-09"), today, noHolidays))
    }

    @Test
    fun `a calendar with no week configured does not restrict the week`() {
        val noWeek = calendar.copy(weekDays = null)

        assertTrue(isSchoolDay(LocalDate.parse("2025-07-10"), today, noWeek))
    }

    @Test
    fun `a date outside the academic year is refused`() {
        assertFalse(isSchoolDay(LocalDate.parse("2024-12-31"), today, calendar))
    }

    @Test
    fun `a calendar without a start or an end does not bound the choice`() {
        val unbounded = calendar.copy(
            academicYear = calendar.academicYear?.copy(startDate = null, endDate = null),
        )

        assertTrue(isSchoolDay(LocalDate.parse("2019-01-07"), today, unbounded))
    }

    @Test
    fun `a day that has not happened is refused whatever the calendar says`() {
        val everyDay = calendar.copy(
            weekDays = null,
            holidays = null,
            academicYear = calendar.academicYear?.copy(startDate = null, endDate = null),
        )

        assertFalse(isSchoolDay(today.plusDays(1), today, everyDay))
        assertFalse(isSchoolDay(today.plusDays(1), today, null))
    }

    @Test
    fun `today itself is allowed`() {
        // 2025-07-31 is a Thursday, so make the school teach it
        val teachesThursday = calendar.copy(weekDays = monToWed.copy(thursday = true))

        assertTrue(isSchoolDay(today, today, teachesThursday))
    }

    @Test
    fun `no calendar at all leaves only the rule about the future`() {
        assertTrue(isSchoolDay(LocalDate.parse("2025-07-10"), today, null))
    }

    @Test
    fun `the calendar of the selected year is the one that answers`() {
        val config = SchoolCalendarConfig(
            academicYear = "de",
            defaults = Defaults(academicYear = "2025"),
            schoolCalendar = listOf(calendar, calendar.copy(id = "cal-2024", academicYear = AcademicYear(
                code = "2024",
                label = "2024",
                description = "2024",
                startDate = "2024-01-01",
                endDate = "2024-12-31",
            ))),
        )

        assertEquals("cal-2024", config.calendarOfYear("2024")?.id)
        assertEquals("cal-2025", config.calendarOfYear("2025")?.id)
    }

    @Test
    fun `the configured default answers when no year is selected`() {
        val config = SchoolCalendarConfig(
            academicYear = "de",
            defaults = Defaults(academicYear = "2025"),
            schoolCalendar = listOf(calendar),
        )

        assertEquals("cal-2025", config.calendarOfYear(null)?.id)
        assertEquals("cal-2025", config.calendarOfYear("")?.id)
    }

    @Test
    fun `a year with no calendar of its own resolves to nothing`() {
        val config = SchoolCalendarConfig(
            academicYear = "de",
            defaults = Defaults(academicYear = "2025"),
            schoolCalendar = listOf(calendar),
        )

        assertNull(config.calendarOfYear("2019"))
    }
}
