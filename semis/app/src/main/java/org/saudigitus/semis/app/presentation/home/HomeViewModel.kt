package org.saudigitus.semis.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.saudigitus.semis.core.data.model.Module
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.data.model.app_config.Registration
import org.saudigitus.semis.core.data.model.schoolcalendar_config.AcademicYear
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.AppModulesRepository
import org.saudigitus.semis.core.data.repository.FilterRepository
import org.saudigitus.semis.core.data.repository.TeiDownloaderRepository
import org.saudigitus.semis.core.data.repository.TeiRepository
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.fields.DropdownState
import org.saudigitus.semis.core.designsystem.components.model.DropdownItem
import org.saudigitus.semis.core.designsystem.components.model.FilterType
import org.saudigitus.semis.core.designsystem.filters.FilterComponentEvent
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.core.designsystem.utils.UiDefaults
import org.saudigitus.semis.core.designsystem.utils.withFilterDetails
import org.saudigitus.semis.core.designsystem.utils.withOUAndFilters
import org.saudigitus.semis.core.designsystem.utils.withSelectedFilter
import org.saudigitus.semis.core.designsystem.utils.withSubtitle
import org.saudigitus.semis.core.utils.onFailure
import org.saudigitus.semis.core.utils.onSuccess
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val filterRepository: FilterRepository,
    private val appConfigRepository: AppConfigRepository,
    private val appModulesRepository: AppModulesRepository,
    private val resourceManager: ResourceManager,
    private val teiDownloaderRepository: TeiDownloaderRepository,
    private val teiRepository: TeiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUIState())
    private val isAutoHideFilters = MutableStateFlow(true)

    private val registration = MutableStateFlow<Registration?>(null)
    private val academicYear = MutableStateFlow<AcademicYear?>(null)
    private val academicYearDL = MutableStateFlow<String>("")

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
            val modules = loadModules(program)
            setRegistration(program)
            setAcademicYear()

            _uiState.update {
                it.copy(
                    program = program,
                    filterState = FilterComponentState(
                        academicYear = getAcademicYearDropdown().first,
                        filters = filters,
                        selectedFilters = if (getAcademicYearDropdown().second != null) {
                            mapOf(
                                FilterType.ACADEMIC_YEAR to getAcademicYearDropdown().second!!
                            )
                        } else emptyMap()
                    ),
                    modules = modules
                )
            }
        }
    }

    private suspend fun setRegistration(program: String) {
        val config = appConfigRepository.getAppConfig(program)
        registration.value = config?.registration
    }

    private suspend fun setAcademicYear() {
        val schoolCalendar = appConfigRepository.getSchoolCalendar()
        val default = schoolCalendar?.defaults?.academicYear
        academicYearDL.value = schoolCalendar?.academicYear.orEmpty()

        academicYear.value = schoolCalendar?.schoolCalendar?.find {
            it?.academicYear?.code == default
        }?.academicYear
    }

    private suspend fun loadFilters(program: String): List<DropdownState> =
        try {
            filterRepository.getFilters(program)?.dataElements?.mapNotNull { filter ->
                DropdownState(
                    filterType = getFilterType(filter?.code.orEmpty()),
                    displayName = filter?.label.orEmpty(),
                    order = filter?.order ?: -1,
                    data = dropdownItems(filter?.dataElement.orEmpty())
                )
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

    private suspend fun loadModules(program: String): List<Module> {
        return appModulesRepository.getModules(program)
    }

    private suspend fun getAcademicYearDropdown(): Pair<DropdownState, DropdownItem?> {
        val schoolCalendar = appConfigRepository.getSchoolCalendar()
        val filterData = dropdownItems(schoolCalendar?.academicYear.orEmpty())
        val academicYearCode = schoolCalendar?.defaults?.academicYear

        val options = schoolCalendar?.schoolCalendar?.mapNotNull { data ->
            filterData.find { data?.academicYear?.code == it.code }
        } ?: emptyList()

        val default = options.find { it.code == academicYearCode }

        return Pair(
            DropdownState(
                filterType = FilterType.ACADEMIC_YEAR,
                displayName = default?.itemName
                    ?: resourceManager.getString(R.string.academic_year),
                data = options
            ), default
        )
    }

    private suspend fun dropdownItems(dataElement: String): List<DropdownItem> {
        return filterRepository.getOptions(
            orgUnit = uiState.value.filterState.orgUnit?.uid.orEmpty(),
            program = uiState.value.program,
            dataElement = dataElement,
        ).map {
            DropdownItem(
                id = it.uid,
                itemName = it.displayName.orEmpty(),
                code = it.code,
                sortOrder = it.sortOrder
            )
        }.sortedBy { it.sortOrder }
    }

    private suspend fun reloadFilters(): MutableList<DropdownState> {
        val filters = uiState.value.filterState.filters.toMutableList()

        if (filters.isNotEmpty()) {
            val item = filters.find { it.filterType == FilterType.GRADE }
            val index = filters.indexOf(item)

            filters.remove(item)

            filters.add(
                index,
                DropdownState(
                    FilterType.GRADE,
                    displayName = item?.displayName.orEmpty(),
                    data = dropdownItems(registration.value?.grade.orEmpty()),
                ),
            )
        }
        filters.sortBy { it.order }

        return filters
    }

    private fun getFilterType(code: String): FilterType =
        UiDefaults.filterTypeMap[code] ?: FilterType.UNKNOWN

    fun hideShowFilter() {
        isAutoHideFilters.value = !isAutoHideFilters.value
        _uiState.update { it.copy(displayFilters = !it.displayFilters) }
    }

    fun handleFilterEvent(event: FilterComponentEvent) {
        when (event) {
            is FilterComponentEvent.Sync -> downloadTei()
            is FilterComponentEvent.FilterValueChange<*> -> handleFilterValueChange(event)
        }
    }

    private fun downloadTei() {
        viewModelScope.launch {
            if (uiState.value.filterState.isFilterSelectionNotEmpty()) {
                _uiState.update { it.copy(isLoading = true) }

                val result = teiDownloaderRepository.downloadTei(
                    ou = uiState.value.filterState.orgUnit?.uid.orEmpty(),
                    program = uiState.value.program,
                    dataElementIds = listOfNotNull(
                        academicYearDL.value,
                        registration.value?.grade,
                        registration.value?.section
                    ),
                    dataValues = listOfNotNull(
                        academicYear.value?.code,
                        uiState.value.filterState.selectedFilters[FilterType.GRADE]?.code,
                        uiState.value.filterState.selectedFilters[FilterType.SECTION]?.code
                    )
                )

                result.onSuccess {
                    loadTeis()
                }.onFailure { f ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = f.message) }
                }
            } else {
                _uiState.update { it.copy(errorMessage = resourceManager.getString(R.string.apply_filters)) }
            }
        }
    }

    private suspend fun loadTeis() {
        teiRepository.getTrackerEntities(
            ou = uiState.value.filterState.orgUnit?.uid.orEmpty(),
            program = uiState.value.program,
            stage = registration.value?.programStage.orEmpty(),
            dataElementIds = listOfNotNull(
                academicYearDL.value,
                registration.value?.grade,
                registration.value?.section
            ),
            dataValues = listOfNotNull(
                academicYear.value?.code,
                uiState.value.filterState.selectedFilters[FilterType.GRADE]?.code,
                uiState.value.filterState.selectedFilters[FilterType.SECTION]?.code
            )
        ).collectLatest { data ->
            val currentFieldState = uiState.value.filterState
            val current = currentFieldState.filterDetailsState

            val updateCount = current.copy(count = data.size)
            val updateFieldState = currentFieldState.copy(filterDetailsState = updateCount)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    filterState = updateFieldState,
                    tei = data
                )
            }
        }
    }

    private fun handleFilterValueChange(event: FilterComponentEvent.FilterValueChange<*>) {
        viewModelScope.launch {
            val current = uiState.value.filterState

            val updatedFilterState = when (val obj = event.obj) {
                is OrgUnit -> current.withOUAndFilters(obj, reloadFilters())
                is DropdownItem -> current.withSelectedFilter(event.filterType, obj)
                else -> current
            }

            val lastUpdatedFilterState = updateFilterDetails(updatedFilterState)

            _uiState.update { it.copy(filterState = lastUpdatedFilterState) }
            updateToolbarHeader(updatedFilterState)
            autoHideFilters()
            loadTeis()
        }
    }

    private fun updateToolbarHeader(filterState: FilterComponentState) {
        val toolbarHeader = uiState.value.toolbarHeaders
        val updatedToolbarHeader = toolbarHeader
            .withSubtitle("${filterState.orgUnit?.displayName ?: resourceManager.getString(R.string.school)} | ${filterState.getAcademicYearSelection()?.itemName}")
        _uiState.update { it.copy(toolbarHeaders = updatedToolbarHeader) }
    }

    private fun updateFilterDetails(filterState: FilterComponentState): FilterComponentState {
        val current = filterState.filterDetailsState

        val updatedFilterDetailsState = current.copy(
            academicYear = filterState.getAcademicYearSelection()?.itemName
                ?: resourceManager.getString(R.string.academic_year),
            orgUnit = filterState.orgUnit?.displayName
                ?: resourceManager.getString(R.string.school),
            grade = filterState.selectedFilters[FilterType.GRADE]?.itemName,
            section = filterState.selectedFilters[FilterType.SECTION]?.itemName,
        )

        return filterState.withFilterDetails(updatedFilterDetailsState)
    }

    private fun autoHideFilters() {
        if (uiState.value.filterState.isFilterSelectionNotEmpty() && isAutoHideFilters.value) {
            _uiState.update { it.copy(isLoading = true, displayFilters = false) }
        }
    }
}
