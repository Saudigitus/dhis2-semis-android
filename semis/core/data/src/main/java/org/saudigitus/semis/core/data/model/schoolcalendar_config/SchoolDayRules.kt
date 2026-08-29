package org.saudigitus.semis.core.data.model.schoolcalendar_config

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * The calendar of a given academic year, or of the one the configuration names as its default.
 *
 * Which year is asked for matters: a user working in an earlier year has to be judged by that
 * year's teaching days, holidays and span, not by this year's. Falling back to the default only
 * when nothing is selected keeps the screens that do not track a selection working as before.
 */
fun SchoolCalendarConfig?.calendarOfYear(academicYearCode: String?): SchoolCalendar? {
    val config = this ?: return null
    val wanted = academicYearCode?.takeIf { it.isNotBlank() }
        ?: config.defaults?.academicYear

    return config.schoolCalendar
        ?.filterNotNull()
        ?.firstOrNull { it.academicYear?.code == wanted }
}

/**
 * Whether a day may carry attendance under a school's calendar.
 *
 * A day qualifies when the school teaches on it, it is not a holiday, it falls inside the
 * academic year and it has already happened. Each of those is only applied where the calendar
 * says something about it: a calendar that lists no holidays is a school with none to exclude,
 * and one without a start or an end is a year that does not bound the choice. Configuration that
 * is absent must not be read as configuration that forbids.
 *
 * The one rule that holds regardless is the future: attendance cannot be taken for a day that has
 * not happened, whatever a calendar says or fails to say.
 */
fun isSchoolDay(
    date: LocalDate,
    today: LocalDate,
    calendar: SchoolCalendar?,
): Boolean {
    if (date.isAfter(today)) return false
    if (calendar == null) return true

    if (!calendar.teachesOn(date.dayOfWeek)) return false
    if (calendar.isHoliday(date)) return false

    return date.withinAcademicYear(calendar.academicYear)
}

/**
 * Whether the school teaches on this day of the week.
 *
 * A calendar with no week configured at all does not restrict the week, since saying nothing is
 * not the same as saying no. Once the week is configured it is taken as the whole answer, so a
 * day it does not mark as teaching is a day the school does not teach.
 */
private fun SchoolCalendar.teachesOn(day: DayOfWeek): Boolean {
    val week = weekDays ?: return true

    return when (day) {
        DayOfWeek.MONDAY -> week.monday
        DayOfWeek.TUESDAY -> week.tuesday
        DayOfWeek.WEDNESDAY -> week.wednesday
        DayOfWeek.THURSDAY -> week.thursday
        DayOfWeek.FRIDAY -> week.friday
        DayOfWeek.SATURDAY -> week.saturday
        DayOfWeek.SUNDAY -> week.sunday
    } == true
}

/** Whether the date is one of the holidays the calendar lists. */
private fun SchoolCalendar.isHoliday(date: LocalDate): Boolean {
    val wanted = date.toString()

    return holidays.orEmpty()
        .filterNotNull()
        .any { it.date == wanted }
}

/** Whether the date falls inside the year, where the year says where it starts and ends. */
private fun LocalDate.withinAcademicYear(academicYear: AcademicYear?): Boolean {
    val start = academicYear?.startDate?.toLocalDateOrNull()
    val end = academicYear?.endDate?.toLocalDateOrNull()

    return (start == null || !isBefore(start)) && (end == null || !isAfter(end))
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching {
    LocalDate.parse(takeIf { it.isNotBlank() })
}.getOrNull()
