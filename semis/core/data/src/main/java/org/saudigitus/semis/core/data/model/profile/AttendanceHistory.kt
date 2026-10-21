package org.saudigitus.semis.core.data.model.profile

/**
 * Attendance a learner accumulated over the selected academic year.
 *
 * [statusCounts] holds one entry per configured attendance status, counting zero when the
 * learner has no day of it, so the counters only disappear when the program configures no
 * status at all.
 *
 * [recordedDays] covers the present days too: with the attendance status flow on they leave no
 * record of the learner and are known only from the counters.
 */
data class AttendanceHistory(
    val records: List<AttendanceRecord> = emptyList(),
    val statusCounts: List<AttendanceStatusCount> = emptyList(),
) {
    val recordedDays: Int get() = maxOf(records.size, statusCounts.sumOf { it.count })
}
