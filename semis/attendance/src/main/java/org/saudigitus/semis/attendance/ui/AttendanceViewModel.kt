package org.saudigitus.semis.attendance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.attendance.data.repository.AttendanceEventRepository
import org.saudigitus.semis.attendance.data.repository.AttendanceOptionRepository
import org.saudigitus.semis.attendance.ui.model.ButtonStep
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.app_config.Attendance
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.utils.DateHelper
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceOptionRepository: AttendanceOptionRepository,
    private val attendanceEventRepository: AttendanceEventRepository,
    private val appConfigRepository: AppConfigRepository,
    private val resourceManager: ResourceManager
) : ViewModel() {

    private var attendanceConfig: Attendance? = null
    private var studentsIds: List<String> = emptyList()

    private val _uiState = MutableStateFlow(
        AttendanceUiState(
            isLoading = true,
            toolbarHeaders = ToolbarHeaders(
                title = resourceManager.getString(R.string.attendance),
                subtitle = DateHelper.formatDateWithWeekDay(System.currentTimeMillis()).orEmpty()
            )
        ),
    )
    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            _uiState.value
        )

    init {
        viewModelScope.launch {
            val schoolCalendar = appConfigRepository.getSchoolCalendar()

            _uiState.update {
                it.copy(
                    dateValidator = { date ->
                        appConfigRepository.allowedCalenderYearDates(date, schoolCalendar)
                    }
                )
            }
        }
    }

    private suspend fun buttonState(
        program: String,
        attendanceEvents: List<AttendanceEventWithDecorator>
    ): AttendanceButtonState {
        val current = uiState.value.attendanceButtonState
        val options = attendanceOptionRepository.getAttendanceStatusOptions(program)

        return current.copy(
            isLoading = false,
            buttons = options,
            attendanceEvents = attendanceEvents
        )
    }

    fun initialize(
        program: String,
        teis: List<SearchTeiModel>,
        filterDetailsState: FilterDetailsState
    ) {
        viewModelScope.launch {
            studentsIds = teis.mapNotNull { it.tei.uid() }

            val config = appConfigRepository.getAppConfig(program)
            attendanceConfig = config?.attendance

            _uiState.update {
                it.copy(
                    isLoading = false,
                    program = program,
                    teis = teis,
                    filterDetailsState = filterDetailsState
                )
            }

            loadAttendanceEventsByDate()
        }
    }

    private fun loadAttendanceEventsByDate(date: String? = null) {
        viewModelScope.launch {
            val currentToolbar = uiState.value.toolbarHeaders
            var updatedToolbar = currentToolbar

            if (date != null) {
                updatedToolbar = currentToolbar.copy(
                    subtitle = DateHelper.formatDateWithWeekDay(date)
                )
            }

            val attendanceEvents = attendanceEventRepository.getAttendanceEvent(
                teiUids = studentsIds,
                program = uiState.value.program,
                programStage = attendanceConfig?.programStage.orEmpty(),
                dataElement = attendanceConfig?.status.orEmpty(),
                reasonDataElement = attendanceConfig?.absenceReason.orEmpty(),
                eventDate = date ?: DateHelper.formatDate(System.currentTimeMillis())
                    .orEmpty()
            )

            val currentButtonState = buttonState(uiState.value.program, attendanceEvents)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    toolbarHeaders = updatedToolbar,
                    attendanceButtonState = currentButtonState,
                )
            }
        }
    }

    fun handleUiEvent(uiEvent: AttendanceUiEvent) {
        when (uiEvent) {
            is AttendanceUiEvent.OnDateSelect -> {
                loadAttendanceEventsByDate(uiEvent.date)
            }

            is AttendanceUiEvent.OnEditClicked -> {
                val current = uiState.value.attendanceButtonState

                _uiState.update {
                    it.copy(
                        buttonStep = ButtonStep.EDITING,
                        attendanceButtonState = current.copy(
                            isEditing = true,
                        )
                    )
                }
            }

            is AttendanceUiEvent.OnAttendanceClick -> {

            }

            is AttendanceUiEvent.OnSaveClicked -> {}
            else -> {}
        }
    }
}