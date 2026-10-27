package org.saudigitus.semis.attendance.ui.model

import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator

internal fun missingLearnerAttendanceCount(
    learnerUids: Collection<String>,
    attendanceEvents: List<AttendanceEventWithDecorator>,
): Int {
    val completedLearnerUids = attendanceEvents.mapNotNull { attendanceEvent ->
        attendanceEvent.event
            ?.takeIf { it.value.isNotBlank() }
            ?.tei
            ?.takeIf { it.isNotBlank() }
    }.toSet()

    return learnerUids.count { it !in completedLearnerUids }
}
