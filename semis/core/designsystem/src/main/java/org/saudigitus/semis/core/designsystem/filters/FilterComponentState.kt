package org.saudigitus.semis.core.designsystem.filters

import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.designsystem.components.fields.DropdownState
import org.saudigitus.semis.core.designsystem.components.model.DropdownItem
import org.saudigitus.semis.core.designsystem.components.model.FilterType

data class FilterComponentState(
    val academicYear: DropdownState? = null,
    val orgUnit: OrgUnit? = null,
    val filters: List<DropdownState> = emptyList(),
    val selectedFilters: Map<FilterType, DropdownItem> = emptyMap(),
) {

    fun getAcademicYearSelection() = selectedFilters
        .getOrElse(FilterType.ACADEMIC_YEAR) { null }

    private fun getFilterTypes(): List<FilterType> {
        return filters.map { it.filterType }
    }

    private fun checkRemaining(): Boolean {
        return selectedFilters.keys.containsAll(getFilterTypes())
    }

    fun isFilterSelectionNotEmpty(): Boolean {
        return orgUnit != null && getAcademicYearSelection() != null && checkRemaining()
    }
}
