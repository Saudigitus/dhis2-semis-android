package org.saudigitus.semis.core.data.model.profile

import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.data.model.app_config.StatusOption

class AttendanceStatusCountsTest {

    private fun option(code: String, key: String, color: String? = null) = StatusOption(
        code = code,
        color = color,
        configKey = null,
        icon = null,
        key = key,
        totalSummary = null,
    )

    /** Configuration used while the attendance status flow is on: present is left out. */
    private val statusFlowOptions = listOf(
        option("ABSENT", "absent"),
        option("LATE", "late"),
    )

    /** Configuration used while the flow is off: every status is configured. */
    private val plainOptions = listOf(
        option("PRESENT", "present"),
        option("ABSENT", "absent"),
        option("LATE", "late"),
    )

    private val labels = mapOf(
        "PRESENT" to "Present",
        "ABSENT" to "Absent",
        "LATE" to "Late",
    )

    @Test
    fun `with the status flow on the days no configured status claims are present`() {
        val counts = attendanceStatusCounts(
            statusOptions = statusFlowOptions,
            optionLabels = labels,
            recordedStatusCodes = listOf("PRESENT", "PRESENT", "PRESENT", "ABSENT", "LATE"),
            derivePresent = true,
        )

        assertEquals(listOf("Present", "Absent", "Late"), counts.map { it.label })
        assertEquals(listOf(3, 1, 1), counts.map { it.count })
    }

    @Test
    fun `the derived present counter leads the configured statuses`() {
        val counts = attendanceStatusCounts(
            statusOptions = statusFlowOptions,
            optionLabels = labels,
            recordedStatusCodes = listOf("ABSENT"),
            derivePresent = true,
        )

        assertEquals("Present", counts.first().label)
        assertEquals(PRESENT_STATUS_KEY, counts.first().key)
    }

    @Test
    fun `an unconfigured status code counts towards present`() {
        val counts = attendanceStatusCounts(
            statusOptions = statusFlowOptions,
            optionLabels = labels,
            recordedStatusCodes = listOf("PRESENT", "SOMETHING_ELSE", "ABSENT"),
            derivePresent = true,
        ).associateBy { it.label }

        assertEquals(2, counts.getValue("Present").count)
        assertEquals(1, counts.getValue("Absent").count)
    }

    @Test
    fun `present reports zero when every recorded day carries a configured status`() {
        val counts = attendanceStatusCounts(
            statusOptions = statusFlowOptions,
            optionLabels = labels,
            recordedStatusCodes = listOf("ABSENT", "LATE", "LATE"),
            derivePresent = true,
        )

        assertEquals(listOf(0, 1, 2), counts.map { it.count })
    }

    @Test
    fun `a learner without attendance still reports every status at zero`() {
        val counts = attendanceStatusCounts(
            statusOptions = statusFlowOptions,
            optionLabels = labels,
            recordedStatusCodes = emptyList(),
            derivePresent = true,
        )

        assertEquals(listOf("Present", "Absent", "Late"), counts.map { it.label })
        assertEquals(listOf(0, 0, 0), counts.map { it.count })
    }

    @Test
    fun `without the status flow present is counted from its own configured option`() {
        val counts = attendanceStatusCounts(
            statusOptions = plainOptions,
            optionLabels = labels,
            recordedStatusCodes = listOf("PRESENT", "PRESENT", "ABSENT"),
            derivePresent = false,
        )

        assertEquals(listOf("Present", "Absent", "Late"), counts.map { it.label })
        assertEquals(listOf(2, 1, 0), counts.map { it.count })
    }

    @Test
    fun `nothing is counted when the program configures no status`() {
        assertEquals(
            emptyList<AttendanceStatusCount>(),
            attendanceStatusCounts(
                statusOptions = emptyList(),
                optionLabels = labels,
                recordedStatusCodes = listOf("ABSENT"),
                derivePresent = true,
            ),
        )
    }

    @Test
    fun `the present counter falls back to a label when the option set has no spare option`() {
        val counts = attendanceStatusCounts(
            statusOptions = statusFlowOptions,
            optionLabels = mapOf("ABSENT" to "Absent", "LATE" to "Late"),
            recordedStatusCodes = listOf("ABSENT"),
            derivePresent = true,
        )

        assertEquals("Present", counts.first().label)
    }

    @Test
    fun `a configured status keeps the color it was configured with`() {
        val counts = attendanceStatusCounts(
            statusOptions = listOf(option("ABSENT", "absent", color = "#E57373")),
            optionLabels = labels,
            recordedStatusCodes = emptyList(),
            derivePresent = false,
        )

        assertEquals("#E57373", counts.single().color)
    }
}
