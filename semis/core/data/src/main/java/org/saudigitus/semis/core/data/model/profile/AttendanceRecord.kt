package org.saudigitus.semis.core.data.model.profile

import java.util.Date

/**
 * A single day of attendance recorded for a learner.
 */
data class AttendanceRecord(
    val eventUid: String,
    val date: Date?,
    val statusCode: String,
    val statusLabel: String,
    val absenceReason: String?,
)
