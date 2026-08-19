package org.saudigitus.semis.core.data.model.profile

import org.saudigitus.semis.core.data.model.app_config.StatusOption

/** Key the derived present counter carries, so it resolves the configured present color. */
const val PRESENT_STATUS_KEY = "present"

/**
 * Counts the attendance of a learner against the configured statuses.
 *
 * Each configured status is counted from the records, reporting zero when the learner has
 * no day of it. With the attendance status flow on the configuration drops the present
 * option — only absence, late and the like are configured — so present is what the records
 * leave over: the days whose status is none of the configured ones.
 *
 * Nothing is counted when the program configures no status at all, since there would be
 * no statuses to report against.
 */
fun attendanceStatusCounts(
    statusOptions: List<StatusOption>,
    optionLabels: Map<String, String>,
    recordedStatusCodes: List<String>,
    derivePresent: Boolean,
    fallbackPresentLabel: String = "Present",
): List<AttendanceStatusCount> {
    val counts = recordedStatusCodes.groupingBy { it }.eachCount()
    val configured = statusOptions.mapNotNull { option ->
        val code = option.code?.takeIf { it.isNotBlank() } ?: return@mapNotNull null

        AttendanceStatusCount(
            key = option.key.orEmpty(),
            code = code,
            label = optionLabels[code]
                ?: option.key?.takeIf { it.isNotBlank() }
                ?: code,
            color = option.color,
            count = counts[code] ?: 0,
        )
    }

    if (configured.isEmpty() || !derivePresent) return configured

    val configuredCodes = configured.mapTo(mutableSetOf()) { it.code }
    // Named after the present option of the option set, matched on its own code or name.
    // Picking whichever option the configuration happens to leave out is not enough: an
    // option set commonly carries more of them, and the first one is rarely present.
    val presentOption = optionLabels.entries.firstOrNull { (code, label) ->
        code !in configuredCodes &&
            (
                code.equals(PRESENT_STATUS_KEY, ignoreCase = true) ||
                    label.equals(PRESENT_STATUS_KEY, ignoreCase = true)
                )
    }

    val present = AttendanceStatusCount(
        key = PRESENT_STATUS_KEY,
        code = presentOption?.key ?: PRESENT_STATUS_KEY,
        label = presentOption?.value ?: fallbackPresentLabel,
        color = null,
        count = recordedStatusCodes.count { it !in configuredCodes },
    )

    return listOf(present) + configured
}
