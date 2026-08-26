package org.saudigitus.semis.app.presentation.home.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.data.model.StoredFilterSelection
import org.saudigitus.semis.core.designsystem.components.fields.DropdownState
import org.saudigitus.semis.core.designsystem.components.model.DropdownItem
import org.saudigitus.semis.core.designsystem.components.model.FilterType

class FilterSelectionRestoreTest {

    private val albion = OrgUnit(uid = "ou1", displayName = "Albion LBS")
    private val banjul = OrgUnit(uid = "ou2", displayName = "Banjul UBS")

    private val years = listOf(item("2024"), item("2025"))
    private val grades = DropdownState(
        filterType = FilterType.GRADE,
        data = listOf(item("G1"), item("G2")),
    )

    @Test
    fun `the school is restored when it is still one the user can capture into`() {
        val stored = StoredFilterSelection(orgUnitUid = "ou1")

        assertEquals(albion, restoredOrgUnit(stored, listOf(albion, banjul)))
    }

    @Test
    fun `a school that left the user scope is not restored`() {
        val stored = StoredFilterSelection(orgUnitUid = "ou1")

        assertNull(restoredOrgUnit(stored, listOf(banjul)))
    }

    @Test
    fun `values that still exist are restored`() {
        val stored = StoredFilterSelection(
            orgUnitUid = "ou1",
            academicYearCode = "2025",
            filterCodes = mapOf(FilterType.GRADE.name to "G2"),
        )

        val restored = restoredSelectedFilters(stored, true, years, listOf(grades))

        assertEquals("2025", restored[FilterType.ACADEMIC_YEAR]?.code)
        assertEquals("G2", restored[FilterType.GRADE]?.code)
    }

    @Test
    fun `a value that no longer exists in its class is left unchosen`() {
        val stored = StoredFilterSelection(
            orgUnitUid = "ou1",
            academicYearCode = "2025",
            filterCodes = mapOf(FilterType.GRADE.name to "G9"),
        )

        val restored = restoredSelectedFilters(stored, true, years, listOf(grades))

        assertEquals("2025", restored[FilterType.ACADEMIC_YEAR]?.code)
        assertNull(restored[FilterType.GRADE])
    }

    @Test
    fun `an academic year that left the calendar is left to the configured default`() {
        val stored = StoredFilterSelection(orgUnitUid = "ou1", academicYearCode = "2019")

        val restored = restoredSelectedFilters(stored, true, years, listOf(grades))

        assertNull(restored[FilterType.ACADEMIC_YEAR])
    }

    @Test
    fun `losing the school drops what describes the class but keeps the academic year`() {
        val stored = StoredFilterSelection(
            orgUnitUid = "ou1",
            academicYearCode = "2025",
            filterCodes = mapOf(FilterType.GRADE.name to "G2"),
        )

        val restored = restoredSelectedFilters(stored, false, years, listOf(grades))

        assertEquals("2025", restored[FilterType.ACADEMIC_YEAR]?.code)
        assertNull(restored[FilterType.GRADE])
    }

    @Test
    fun `nothing remembered restores nothing`() {
        val stored = StoredFilterSelection()

        assertTrue(stored.isEmpty)
        assertNull(restoredOrgUnit(stored, listOf(albion)))
        assertTrue(restoredSelectedFilters(stored, false, years, listOf(grades)).isEmpty())
    }

    @Test
    fun `only identifiers are kept when the selection is recorded`() {
        val selection = storedFilterSelection(
            orgUnit = albion,
            selectedFilters = mapOf(
                FilterType.ACADEMIC_YEAR to item("2025"),
                FilterType.GRADE to item("G1"),
            ),
        )

        assertEquals("ou1", selection.orgUnitUid)
        assertEquals("2025", selection.academicYearCode)
        assertEquals(mapOf(FilterType.GRADE.name to "G1"), selection.filterCodes)
    }

    @Test
    fun `a value without a code is not recorded, since it could never be found again`() {
        val selection = storedFilterSelection(
            orgUnit = albion,
            selectedFilters = mapOf(
                FilterType.GRADE to DropdownItem(id = "x", itemName = "Grade 1", code = null),
            ),
        )

        assertTrue(selection.filterCodes.isEmpty())
    }

    private fun item(code: String) = DropdownItem(id = code, itemName = code, code = code)
}
