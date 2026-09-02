package org.saudigitus.semis.core.form.data.repository.impl

import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator

/**
 * Records to delete when the attendance of a date is discarded.
 *
 * With the attendance status flow on, being present is the absence of a record, so only
 * the coded statuses — absent, late and any other configured one — are stored and deleted.
 * Without it every learner carries a record, so the whole date is cleared. Either way the
 * attendance status event is on another program stage and is never part of this.
 */
internal fun removableAttendanceEvents(
    events: List<AttendanceEventWithDecorator>,
    configuredStatusCodes: List<String>,
    absencesOnly: Boolean,
): List<AttendanceEventWithDecorator> = events.filter { event ->
    !absencesOnly || event.event?.value in configuredStatusCodes
}

/**
 * Records that were loaded for the date but are no longer held by the form, so they have
 * to be deleted on save instead of lingering with a status the learner no longer has.
 */
internal fun orphanedAttendanceEvents(
    loaded: List<AttendanceEventWithDecorator>,
    current: List<AttendanceEventWithDecorator>,
): List<AttendanceEventWithDecorator> {
    val keptTeis = current.mapNotNull { it.event?.tei }.toSet()

    return loaded.filter { it.event?.tei !in keptTeis }
}
