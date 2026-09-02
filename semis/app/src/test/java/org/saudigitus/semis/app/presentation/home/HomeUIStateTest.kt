package org.saudigitus.semis.app.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.fields.DropdownState
import org.saudigitus.semis.core.designsystem.components.model.DropdownItem
import org.saudigitus.semis.core.designsystem.components.model.FilterType
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState

class HomeUIStateTest {

    @Test
    fun `modules stay closed while the class is not fully chosen`() {
        val state = HomeUIState(filterState = filterState(grade = null))

        assertFalse(state.areModulesAvailable)
        assertEquals(HomeNotice.SELECT_FILTERS, state.notice)
    }

    @Test
    fun `modules open once the class is chosen even though it holds no learner`() {
        val state = HomeUIState(filterState = filterState(count = 0))

        assertTrue(state.areModulesAvailable)
        assertEquals(HomeNotice.NO_DATA, state.notice)
    }

    @Test
    fun `a chosen class that holds learners is reported without a notice`() {
        val state = HomeUIState(filterState = filterState(count = 12))

        assertTrue(state.areModulesAvailable)
        assertEquals(HomeNotice.NONE, state.notice)
    }

    @Test
    fun `the school alone does not open the modules`() {
        val state = HomeUIState(
            filterState = filterState(academicYear = null, grade = null),
        )

        assertFalse(state.areModulesAvailable)
        assertEquals(HomeNotice.SELECT_FILTERS, state.notice)
    }

    /**
     * Builds a selection over one configured filter, the grade, so that leaving it out reproduces
     * the case of a user who has answered part of the filters and not the rest.
     */
    private fun filterState(
        academicYear: DropdownItem? = item("2025"),
        grade: DropdownItem? = item("Grade 1"),
        count: Int = 0,
    ): FilterComponentState {
        val selected = buildMap {
            academicYear?.let { put(FilterType.ACADEMIC_YEAR, it) }
            grade?.let { put(FilterType.GRADE, it) }
        }

        return FilterComponentState(
            orgUnit = OrgUnit(uid = "ou", displayName = "Albion LBS"),
            filters = listOf(DropdownState(filterType = FilterType.GRADE)),
            selectedFilters = selected,
            filterDetailsState = FilterDetailsState(count = count),
        )
    }

    private fun item(name: String) = DropdownItem(id = name, itemName = name, code = name)
}
