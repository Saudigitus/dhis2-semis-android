package org.saudigitus.semis.core.data.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The scenarios below mirror the grade configuration the schools are set up with: the grades are
 * split into a primary, a lower secondary and an upper secondary option group, and each school type
 * is restricted to the groups it teaches.
 */
class OptionRuleEffectsTest {

    private val primary = setOf("grade1", "grade2", "grade3", "grade4", "grade5", "grade6")
    private val lowerSecondary = setOf("grade7", "grade8", "grade9")
    private val allGrades = (primary + lowerSecondary + setOf("grade10", "grade11", "grade12")).toList()

    private fun visibleGrades(
        optionUidsToHide: Set<String> = emptySet(),
        optionUidsToShow: Set<String>? = null,
    ) = allGrades.filter { isOptionVisible(it, optionUidsToHide, optionUidsToShow) }

    @Test
    fun `a school matched by no rule keeps every grade`() {
        val effects = OptionRuleEffects()

        assertTrue(effects.restrictsNothing)
        assertEquals(allGrades, visibleGrades())
    }

    @Test
    fun `a school that teaches upper secondary only loses the grades of the hidden groups`() {
        val effects = OptionRuleEffects(optionGroupsToHide = listOf("primary", "lowerSecondary"))

        assertFalse(effects.restrictsNothing)
        assertEquals(
            listOf("grade10", "grade11", "grade12"),
            visibleGrades(optionUidsToHide = primary + lowerSecondary),
        )
    }

    @Test
    fun `hiding one group leaves the grades of the other groups untouched`() {
        assertEquals(
            listOf(
                "grade1", "grade2", "grade3", "grade4", "grade5", "grade6",
                "grade10", "grade11", "grade12",
            ),
            visibleGrades(optionUidsToHide = lowerSecondary),
        )
    }

    @Test
    fun `a revealed group becomes the only choice even when nothing was hidden`() {
        val effects = OptionRuleEffects(optionGroupsToShow = listOf("primary"))

        assertFalse(effects.restrictsNothing)
        assertEquals(primary.toList(), visibleGrades(optionUidsToShow = primary))
    }

    @Test
    fun `a revealed group overrides the groups another rule hid`() {
        assertEquals(
            primary.toList(),
            visibleGrades(optionUidsToHide = primary + lowerSecondary, optionUidsToShow = primary),
        )
    }

    @Test
    fun `an option hidden on its own is dropped without touching its group`() {
        val effects = OptionRuleEffects(optionsToHide = listOf("grade7"))

        assertFalse(effects.restrictsNothing)
        assertEquals(
            allGrades - "grade7",
            visibleGrades(optionUidsToHide = setOf("grade7")),
        )
    }

    @Test
    fun `an option that belongs to no group survives a group level hide`() {
        val ungrouped = allGrades + "gradeUngrouped"

        assertEquals(
            listOf("grade10", "grade11", "grade12", "gradeUngrouped"),
            ungrouped.filter { isOptionVisible(it, primary + lowerSecondary, null) },
        )
    }
}
