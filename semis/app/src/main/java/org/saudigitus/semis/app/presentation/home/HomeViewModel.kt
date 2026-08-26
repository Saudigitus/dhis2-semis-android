package org.saudigitus.semis.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
import org.saudigitus.semis.app.presentation.home.model.autoSelectedFilters
import org.saudigitus.semis.app.presentation.home.model.onlyOrgUnit
import org.saudigitus.semis.app.presentation.home.model.restoredOrgUnit
import org.saudigitus.semis.app.presentation.home.model.restoredSelectedFilters
import org.saudigitus.semis.app.presentation.home.model.storedFilterSelection
import org.saudigitus.semis.core.data.model.Module
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.data.model.StoredFilterSelection
import org.saudigitus.semis.core.data.model.app_config.Registration
import org.saudigitus.semis.core.data.model.schoolcalendar_config.AcademicYear
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.AppModulesRepository
import org.saudigitus.semis.core.data.repository.FilterRepository
import org.saudigitus.semis.core.data.repository.FilterSelectionRepository
import org.saudigitus.semis.core.data.repository.OrgUnitRepository
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
    private val teiRepository: TeiRepository,
    private val filterSelectionRepository: FilterSelectionRepository,
    private val orgUnitRepository: OrgUnitRepository
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

    private var loadJob: Job? = null

    /**
     * Guards against building the filters again over a selection the user is already working with.
     *
     * The screen initialises this from an effect that runs again whenever it is recreated, while
     * this holds its state across that, so without the guard a rotation would rebuild the filters
     * and discard the current choice, remembered or not.
     */
    private var initialized = false

    fun initialize(program: String, programName: String? = null) {
        if (initialized) return
        initialized = true

        viewModelScope.launch {
            val filters = loadFilters(program).sortedBy { it.order }
            val modules = loadModules(program)
            setRegistration(program)
            setAcademicYear()

            val academicYearDropdown = getAcademicYearDropdown()

            _uiState.update {
                it.copy(
                    program = program,
                    programName = programName.orEmpty(),
                    filterState = FilterComponentState(
                        academicYear = academicYearDropdown.first,
                        filters = filters,
                        selectedFilters = academicYearDropdown.second
                            ?.let { default -> mapOf(FilterType.ACADEMIC_YEAR to default) }
                            .orEmpty()
                    ),
                    modules = modules
                )
            }

            settleInitialSelection(program, academicYearDropdown.first.data)
        }
    }

    /**
     * Settles the class the user starts on, from what was remembered and from what has no
     * alternative.
     *
     * The school comes first, because the lists that describe a class are read under it, and only
     * then are the remembered values looked for in those lists. Anything that no longer exists is
     * left unchosen rather than guessed at, so what comes back is always a selection the user could
     * have made by hand. Whatever is still unanswered and offers a single value is then filled in,
     * since one possibility is not a choice.
     *
     * This runs as a single ordered pass rather than by reacting to the state as it changes, which
     * is how resolving one field that reveals the next turns into a loop.
     *
     * Only what is already on the device is read. Opening a module never reaches the server on its
     * own, which would surprise the user and fail outright with no connection.
     */
    private suspend fun settleInitialSelection(program: String, academicYearOptions: List<DropdownItem>) {
        val available = orgUnitRepository.captureOrgUnits(program)
        val stored = filterSelectionRepository.read(program)

        val orgUnit = restoredOrgUnit(stored, available) ?: onlyOrgUnit(available)

        var filterState = uiState.value.filterState
        if (orgUnit != null) {
            filterState = filterState.copy(orgUnit = orgUnit)
            _uiState.update { it.copy(filterState = filterState) }
            filterState = filterState.withOUAndFilters(orgUnit, reloadFilters())
        }

        val restored = filterState.selectedFilters + restoredSelectedFilters(
            stored = stored,
            orgUnitRestored = orgUnit != null,
            academicYearOptions = academicYearOptions,
            filters = filterState.filters,
        )
        val resolved = restored + autoSelectedFilters(
            filters = filterState.filters,
            selected = restored,
            excluded = NEVER_RESOLVED_WITHOUT_THE_USER,
        )

        if (orgUnit == null && resolved == filterState.selectedFilters) return

        filterState = updateFilterDetails(filterState.copy(selectedFilters = resolved))

        _uiState.update { it.copy(isLoading = true, filterState = filterState) }
        updateToolbarHeader(filterState)
        autoHideFilters()
        rememberSelection(filterState)

        loadJob?.cancel()
        loadJob = viewModelScope.launch { loadTeis() }
    }

    /** Records the class the user is on, so that the next opening can start from it. */
    private suspend fun rememberSelection(filterState: FilterComponentState) {
        val program = uiState.value.program.takeIf { it.isNotBlank() } ?: return

        filterSelectionRepository.save(
            program = program,
            selection = storedFilterSelection(filterState.orgUnit, filterState.selectedFilters),
        )
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
            is FilterComponentEvent.ResetFilters -> resetFilters()
            is FilterComponentEvent.FilterValueChange<*> -> handleFilterValueChange(event)
        }
    }

    /**
     * Puts the filters back to how a first opening finds them.
     *
     * What was remembered is forgotten as well, otherwise the next opening would put back exactly
     * what the user has just asked to be rid of. The academic year keeps its configured default,
     * which is where it starts from anyway.
     *
     * Nothing is settled again on the user's behalf here, not even a value that has no
     * alternative: they asked for a clean sheet and they get one. The next opening resolves it
     * afresh, as it always does.
     */
    private fun resetFilters() {
        viewModelScope.launch {
            loadJob?.cancel()

            val current = uiState.value.filterState
            val cleared = updateFilterDetails(
                current.copy(
                    orgUnit = null,
                    selectedFilters = current.selectedFilters
                        .filterKeys { it == FilterType.ACADEMIC_YEAR },
                    // The details are recomputed from the cleared state below, but the count comes
                    // from the records that were loaded and has to be dropped here.
                    filterDetailsState = current.filterDetailsState.copy(
                        grade = null,
                        section = null,
                        count = 0,
                        enable = false,
                    ),
                ),
            )

            filterSelectionRepository.save(uiState.value.program, StoredFilterSelection())

            _uiState.update {
                it.copy(
                    isLoading = false,
                    filterState = cleared,
                    tei = emptyList(),
                    errorMessage = null,
                )
            }
            updateToolbarHeader(cleared)
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

            val updateCount = current.copy(count = data.size, enable = data.isNotEmpty())
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

    fun refreshTeis() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { loadTeis() }
    }

    private fun handleFilterValueChange(event: FilterComponentEvent.FilterValueChange<*>) {
        viewModelScope.launch {
            loadJob?.cancel()

            val current = uiState.value.filterState

            val updatedFilterState = when (val obj = event.obj) {
                is OrgUnit -> current.withOUAndFilters(obj, reloadFilters())
                is DropdownItem -> current.withSelectedFilter(event.filterType, obj)
                else -> current
            }

            val lastUpdatedFilterState = updateFilterDetails(updatedFilterState)

            _uiState.update {
                it.copy(
                    isLoading = true,
                    filterState = lastUpdatedFilterState,
                    tei = emptyList()
                )
            }
            updateToolbarHeader(updatedFilterState)
            autoHideFilters()
            rememberSelection(lastUpdatedFilterState)

            loadJob = launch { loadTeis() }
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

    private companion object {
        /**
         * The filters that are never settled without the user looking at them.
         *
         * The academic year already arrives chosen from the school calendar, and a second rule
         * competing for it would make the outcome depend on ordering. The section is served by a
         * list that is built before any school is known and is never rebuilt afterwards, so what it
         * offers cannot be trusted enough to choose from on the user's behalf. It joins the rest
         * once that list is rebuilt under the chosen school and grade.
         */
        val NEVER_RESOLVED_WITHOUT_THE_USER = setOf(FilterType.ACADEMIC_YEAR, FilterType.SECTION)
    }

    private fun autoHideFilters() {
        if (uiState.value.filterState.isFilterSelectionNotEmpty() && isAutoHideFilters.value) {
            _uiState.update { it.copy(isLoading = true, displayFilters = false) }
        }
    }
}
