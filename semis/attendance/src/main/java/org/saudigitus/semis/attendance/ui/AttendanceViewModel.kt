package org.saudigitus.semis.attendance.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rocket
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.maintenance.D2Error
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.attendance.ui.model.BottomSheetConfirmAction
import org.saudigitus.semis.attendance.ui.model.BottomSheetType
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.app_config.Attendance
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.form.data.repository.FormRepository
import org.saudigitus.semis.core.utils.ButtonStep
import org.saudigitus.semis.core.utils.DateHelper
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val formRepository: FormRepository,
    private val appConfigRepository: AppConfigRepository,
    private val resourceManager: ResourceManager
) : ViewModel() {

    private var attendanceConfig: Attendance? = null
    private var studentsIds: List<String> = emptyList()
    private var selectedDate: String = DateHelper.formatDate(System.currentTimeMillis())
        .orEmpty()

    private var cachedButtonModel: AttendanceButtonModel? = null

    private val _hasCachedData = MutableStateFlow(false)
    private val hasCachedData: StateFlow<Boolean> = _hasCachedData

    private val _snackbarEvent = MutableSharedFlow<String?>()
    val snackbarEvent: SharedFlow<String?> = _snackbarEvent

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
                    },
                    canTakeAttendance = appConfigRepository.allowedCalenderYearDates(
                        DateHelper.convertDateToMilliseconds(selectedDate),
                        schoolCalendar
                    )
                )
            }


        }
    }

    fun initialize(
        program: String,
        orgUnit: String,
        teis: List<SearchTeiModel>,
        filterDetailsState: FilterDetailsState
    ) {
        viewModelScope.launch {
            studentsIds = teis.mapNotNull { it.tei.uid() }

            val config = appConfigRepository.getAppConfig(program)
            attendanceConfig = config?.attendance

            val currentAttendanceSummaryState = uiState.value.attendanceSummaryState
            val currentFormState = uiState.value.formBuilderState

            _uiState.update {
                it.copy(
                    isLoading = false,
                    program = program,
                    teis = teis,
                    attendanceSummaryState = currentAttendanceSummaryState.copy(
                        filterDetailsState = filterDetailsState
                    ),
                    formBuilderState = currentFormState.copy(
                        orgUnit = orgUnit,
                        program = program,
                        programStage = config?.attendance?.programStage.orEmpty(),
                    )
                )
            }

            loadAttendanceEventsByDate()
            attendanceSummary()
        }
    }

    fun hasCachedValues(cached: Boolean) {
        _hasCachedData.value = cached
    }

    private fun loadAttendanceEventsByDate(date: String? = null) {
        selectedDate = date ?: DateHelper.formatDate(System.currentTimeMillis()).orEmpty()

        viewModelScope.launch {
            val currentToolbar = uiState.value.toolbarHeaders
            val currentBulkBottomSheet = uiState.value.genericsBottomSheetState
            val currentFormState = uiState.value.formBuilderState
            var updatedToolbar = currentToolbar

            val schoolCalendar = appConfigRepository.getSchoolCalendar()

            if (date != null) {
                updatedToolbar = currentToolbar.copy(
                    subtitle = DateHelper.formatDateWithWeekDay(date)
                )
            }

            val currentButtonState = formRepository.loadAttendanceEvents(
                teiUids = studentsIds,
                program = uiState.value.program,
                programStage = attendanceConfig?.programStage.orEmpty(),
                dataElement = attendanceConfig?.status.orEmpty(),
                reasonDataElement = attendanceConfig?.absenceReason.orEmpty(),
                eventDate = date ?: DateHelper.formatDate(System.currentTimeMillis())
                    .orEmpty()
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    toolbarHeaders = updatedToolbar,
                    genericsBottomSheetState = currentBulkBottomSheet.copy(
                        imageVector = Icons.Default.Rocket,
                        title = resourceManager.getString(R.string.bulk_attendance),
                        items = currentButtonState.buttons
                    ),
                    formBuilderState = currentFormState.copy(
                        date = selectedDate
                    ),
                    canTakeAttendance = appConfigRepository.allowedCalenderYearDates(
                        DateHelper.convertDateToMilliseconds(selectedDate),
                        schoolCalendar
                    )
                )
            }
        }
    }

    private fun attendanceSummary() {
        viewModelScope.launch {
            val attendanceSummaryState = uiState.value.attendanceSummaryState
            formRepository.attendanceSummary(uiState.value.program) { summaries ->
                _uiState.update {
                    it.copy(
                        attendanceSummaryState = attendanceSummaryState.copy(
                            bottomSheetModels = summaries,
                            enableBulk = it.buttonStep != ButtonStep.NONE
                        )
                    )
                }
            }
        }
    }

    private fun bulkAttendance(buttonModel: AttendanceButtonModel) {
        viewModelScope.launch {
            uiState.value.teis.forEach {
                formRepository
                    .updateAttendanceEvent(
                        selectedDate,
                        it,
                        buttonModel
                    )
            }
            attendanceSummary()
        }
    }

    private fun saveAttendanceEvents() {
        viewModelScope.launch {
            val current = uiState.value

            runCatching {
                formRepository.saveAttendance(
                    program = current.program,
                    programStage = attendanceConfig?.programStage.orEmpty(),
                    attendanceEvents = formRepository.attendanceButtonStateFlow.value.attendanceEvents
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(buttonStep = ButtonStep.NONE)
                }
                formRepository.allowFormEdition(false)
                loadAttendanceEventsByDate(selectedDate)
                _snackbarEvent.emit(resourceManager.getString(R.string.attendance_saved))
            }.onFailure { error ->
                val friendlyMessage = when (error) {
                    is D2Error -> {
                        "${error.errorCode()} – ${
                            error.message ?: resourceManager
                                .getString(R.string.error_unexpected)
                        }"
                    }

                    else -> error.message ?: resourceManager
                        .getString(R.string.error_unexpected)
                }

                _uiState.update {
                    it.copy(errorMessage = friendlyMessage)
                }
            }
        }
    }

    fun handleUiEvent(uiEvent: AttendanceUiEvent) {
        when (uiEvent) {
            is AttendanceUiEvent.OnDateSelect -> {
                loadAttendanceEventsByDate(uiEvent.date)
                attendanceSummary()
            }

            is AttendanceUiEvent.OnEditClicked -> {
                val currentAttendanceSummaryState = uiState.value.attendanceSummaryState

                formRepository.allowFormEdition(true)

                _uiState.update {
                    it.copy(
                        buttonStep = ButtonStep.EDITING,
                        attendanceSummaryState = currentAttendanceSummaryState.copy(
                            enableBulk = it.buttonStep != ButtonStep.NONE
                        ),
                    )
                }
            }

            is AttendanceUiEvent.ShowBottomSheet -> {
                if (uiEvent.type == BottomSheetType.BULK) {
                    _uiState.update {
                        it.copy(displayBulk = true)
                    }
                } else {
                    _uiState.update {
                        it.copy(displayDialog = true)
                    }
                }
            }

            is AttendanceUiEvent.DismissBottomSheet -> {
                _uiState.update {
                    it.copy(displayBulk = false)
                }
            }

            is AttendanceUiEvent.PerformBulk -> {
                if (hasCachedData.value) {
                    cachedButtonModel = uiEvent.buttonModel
                    _uiState.update {
                        it.copy(overrideBulk = true)
                    }
                } else {
                    bulkAttendance(uiEvent.buttonModel)
                    _uiState.update {
                        it.copy(displayBulk = false)
                    }
                }
            }

            is AttendanceUiEvent.BulkOverrideAttendance -> {
                if (cachedButtonModel != null) {
                    bulkAttendance(cachedButtonModel!!)
                    cachedButtonModel = null
                    _uiState.update {
                        it.copy(displayBulk = false, overrideBulk = false)
                    }
                }
            }

            is AttendanceUiEvent.BottomSheetAction -> {
                if (uiEvent.action == BottomSheetConfirmAction.PERFORM_BULK) {
                    _uiState.update {
                        it.copy(displayBulk = false)
                    }
                } else {
                    saveAttendanceEvents()
                    _uiState.update {
                        it.copy(displayDialog = false)
                    }
                }
            }

            else -> {}
        }
    }

    fun disableEditing() {
        formRepository.allowFormEdition(false)
    }

    fun resetForm() {
        formRepository.reset()
    }
}