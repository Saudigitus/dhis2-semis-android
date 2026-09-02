package org.saudigitus.semis.core.data.model.profile

/**
 * How many days a learner holds of one configured attendance status.
 *
 * Every configured status is reported, whether or not the learner has a day of it, so the
 * dashboard counters stay the same set from one learner to the next. [color] is the value
 * the status is configured with and is left for the caller to resolve.
 */
data class AttendanceStatusCount(
    val key: String,
    val code: String,
    val label: String,
    val color: String?,
    val count: Int,
)
