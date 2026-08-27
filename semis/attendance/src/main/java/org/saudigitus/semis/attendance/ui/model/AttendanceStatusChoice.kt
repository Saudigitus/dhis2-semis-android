package org.saudigitus.semis.attendance.ui.model

import org.hisp.dhis.android.core.event.EventStatus

/**
 * One candidate for the attendance summary of a day, reduced to what deciding between them needs.
 */
internal data class AttendanceStatusCandidate<T>(
    val event: T,
    val status: EventStatus?,
    val lastUpdated: Long,
    val contextValues: List<Pair<String, String>>,
)

/**
 * Picks the summary of a day out of the events that could be it.
 *
 * A day can hold more than one summary, because more than one client records attendance and
 * nothing stops each creating its own. Taking whichever the query happens to return first means the
 * same day can be answered two ways from one moment to the next, and the totals can be written onto
 * whichever twin came up. The choice is therefore fixed here: the one already completed, and among
 * equals the one most recently updated.
 *
 * This does not remove a duplicate, merge one, or stop another appearing. It makes the answer the
 * same every time it is asked, which is all the app can do on its own.
 *
 * A candidate has to carry every value in [contextValues] to be considered at all. Where there is
 * nothing to match on, nothing is chosen: two classes of the same school on the same day would then
 * be indistinguishable, and picking one of them would be a guess written into the data.
 */
internal fun <T> chooseAttendanceStatus(
    candidates: List<AttendanceStatusCandidate<T>>,
    contextValues: List<Pair<String, String>>,
): AttendanceStatusCandidate<T>? {
    if (contextValues.isEmpty()) return null

    return candidates
        .filter { candidate -> candidate.carries(contextValues) }
        .maxWithOrNull(
            compareBy<AttendanceStatusCandidate<T>> { it.status == EventStatus.COMPLETED }
                .thenBy { it.lastUpdated },
        )
}

private fun <T> AttendanceStatusCandidate<T>.carries(
    contextValues: List<Pair<String, String>>,
): Boolean = contextValues.all { it in this.contextValues }
