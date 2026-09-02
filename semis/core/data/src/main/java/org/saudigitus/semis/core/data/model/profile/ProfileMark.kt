package org.saudigitus.semis.core.data.model.profile

import java.util.Date

/**
 * A single mark recorded for a learner in a subject.
 */
data class ProfileMark(
    val eventUid: String,
    val date: Date?,
    val label: String,
    val value: Double?,
    val displayValue: String,
)
