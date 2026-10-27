package org.saudigitus.semis.attendance.ui.model

import org.saudigitus.semis.core.data.model.app_config.StatusOption

internal const val PRESENT_STATUS_KEY = "present"

/**
 * The configured status standing for a recorded presence, which carries no attendance
 * value of its own and is therefore counted as whatever the other statuses leave over.
 */
internal fun StatusOption.isPresent() =
    key.equals(PRESENT_STATUS_KEY, ignoreCase = true) ||
        code.equals(PRESENT_STATUS_KEY, ignoreCase = true)

/**
 * Maps the attendance counts of a day onto the data elements of the attendance status
 * event: the learner total, then one summary per configured status.
 *
 * Statuses are configuration driven, so the counts cannot be flattened into a single
 * absence total. Each option instead declares the data element holding its own summary
 * through [StatusOption.totalSummary]; an option configured without one has nowhere to be
 * stored and is skipped rather than written somewhere else.
 */
internal fun attendanceStatusSummaryValues(
    statusOptions: List<StatusOption>,
    totalRecordsDataElement: String,
    counts: AttendanceSummaryCounts,
): List<Pair<String, String>> {
    val values = linkedMapOf<String, String>()

    values[totalRecordsDataElement] = counts.totalLearners.toString()

    statusOptions.forEach { status ->
        val summaryDataElement = status.totalSummary?.takeIf { it.isNotBlank() }
            ?: return@forEach
        val count = if (status.isPresent()) {
            counts.presentLearners
        } else {
            counts.statusCounts[status.code] ?: 0
        }

        values[summaryDataElement] = count.toString()
    }

    return values.map { it.key to it.value }
}
