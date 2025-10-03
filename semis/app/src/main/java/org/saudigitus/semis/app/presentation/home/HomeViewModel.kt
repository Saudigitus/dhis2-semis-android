package org.saudigitus.semis.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.data.repository.FilterRepository
import org.saudigitus.semis.core.designsystem.components.fields.DropdownState
import org.saudigitus.semis.core.designsystem.components.model.DropdownItem
import org.saudigitus.semis.core.designsystem.components.model.FilterType
import org.saudigitus.semis.core.designsystem.filters.FilterComponentEvent
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.core.designsystem.utils.UiDefaults
import org.saudigitus.semis.core.designsystem.utils.withOrgUnit
import org.saudigitus.semis.core.designsystem.utils.withSelectedFilter
import org.saudigitus.semis.core.designsystem.utils.withSubtitle
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val filterRepository: FilterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUIState())
    private val isAutoHideFilters = MutableStateFlow(true)

    val uiState: StateFlow<HomeUIState> = combine(
        _uiState,
        isAutoHideFilters
    ) { state, autoHide ->
        state.copy(
            displayFilters = !(autoHide && state.filterState.isFilterSelectionNotEmpty())
        )
    }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HomeUIState()
        )

    fun initialize(program: String) {
        viewModelScope.launch {
            val filters = loadFilters(program).sortedBy { it.order }

            _uiState.update {
                it.copy(
                    program = program,
                    filterState = FilterComponentState(
                        academicYear = getAcademicYearDropdown(),
                        filters = filters
                    )
                )
            }
        }
    }

    private suspend fun loadFilters(program: String): List<DropdownState> =
        try {
            filterRepository.getFilters(program)?.dataElements?.mapNotNull { filter ->
                DropdownState(
                    filterType = getFilterType(filter?.code.orEmpty()),
                    displayName = filter?.label.orEmpty(),
                    order = filter?.order ?: -1,
                    data = listOf(
                        DropdownItem(id = "1", itemName = "Item 1", code = "item-1"),
                        DropdownItem(id = "2", itemName = "Item 2", code = "item-2"),
                        DropdownItem(id = "3", itemName = "Item 3", code = "item-3")
                    )
                )
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

    private fun getAcademicYearDropdown(): DropdownState =
        DropdownState(
            filterType = FilterType.ACADEMIC_YEAR,
            displayName = "Academic Year",
            data = listOf(
                DropdownItem("1", "2024/2025", "2024/2025"),
                DropdownItem("2", "2023/2024", "2023/2024"),
                DropdownItem("3", "2022/2023", "2022/2023")
            )
        )

    private fun getFilterType(code: String): FilterType =
        UiDefaults.filterTypeMap[code] ?: FilterType.UNKNOWN

    fun hideShowFilter() {
        isAutoHideFilters.value = !isAutoHideFilters.value
        _uiState.update { it.copy(displayFilters = !it.displayFilters) }
    }

    fun handleFilterEvent(event: FilterComponentEvent) {
        when (event) {
            is FilterComponentEvent.Sync -> {
                // TODO: Implement students download
            }

            is FilterComponentEvent.FilterValueChange<*> -> handleFilterValueChange(event)
        }
    }

    private fun handleFilterValueChange(event: FilterComponentEvent.FilterValueChange<*>) {
        val current = uiState.value.filterState

        val updatedFilterState = when (val obj = event.obj) {
            is OrgUnit -> current.withOrgUnit(obj)
            is DropdownItem -> current.withSelectedFilter(event.filterType, obj)
            else -> current
        }

        _uiState.update { it.copy(filterState = updatedFilterState) }
        updateToolbarHeader(current)
        autoHideFilters()
    }

    private fun updateToolbarHeader(filterState: FilterComponentState) {
        val toolbarHeader = uiState.value.toolbarHeaders
        val updatedToolbarHeader = toolbarHeader
            .withSubtitle("${filterState.orgUnit?.displayName} | ${filterState.getAcademicYearSelection()?.itemName}")
        _uiState.update { it.copy(toolbarHeaders = updatedToolbarHeader) }
    }

    private fun autoHideFilters() {
        if (uiState.value.filterState.isFilterSelectionNotEmpty() && isAutoHideFilters.value) {
            _uiState.update { it.copy(isLoading = true, displayFilters = false) }
        }
    }
}
