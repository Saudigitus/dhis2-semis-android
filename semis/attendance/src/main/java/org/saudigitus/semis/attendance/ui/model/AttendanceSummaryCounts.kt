package org.saudigitus.semis.attendance.ui.model

internal data class AttendanceSummaryCounts(
    val totalLearners: Int,
    val totalAbsences: Int,
    val statusCounts: Map<String, Int>,
    val presentLearners: Int,
)

internal fun attendanceSummaryCounts(
    totalLearners: Int,
    configuredStatusCodes: List<String>,
    attendanceValues: List<String>,
): AttendanceSummaryCounts {
    val statusCounts = configuredStatusCodes
        .distinct()
        .associateWith { status -> attendanceValues.count { it == status } }
    val totalAbsences = statusCounts.values.sum()

    return AttendanceSummaryCounts(
        totalLearners = totalLearners,
        totalAbsences = totalAbsences,
        statusCounts = statusCounts,
        presentLearners = (totalLearners - totalAbsences).coerceAtLeast(0),
    )
}
