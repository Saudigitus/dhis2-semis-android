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
import org.saudigitus.semis.attendance.ui.repository.AttendanceRepository
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.app_config.Attendance
import org.saudigitus.semis.core.data.model.app_config.isEnabledAndConfigured
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
    private val attendanceRepository: AttendanceRepository,
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

    private val _syncEvent = MutableSharedFlow<Unit>()
    val syncEvent: SharedFlow<Unit> = _syncEvent

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent

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
                    allowAttendanceStatus = config?.attendance?.attendanceStatus
                        .isEnabledAndConfigured(),
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

    private suspend fun loadAttendanceEventsByDate(date: String? = null) {
        selectedDate = date ?: DateHelper.formatDate(System.currentTimeMillis()).orEmpty()

        val currentToolbar = uiState.value.toolbarHeaders
        val currentBulkBottomSheet = uiState.value.genericsBottomSheetState
        val currentFormState = uiState.value.formBuilderState
        var updatedToolbar = currentToolbar

        val schoolCalendar = appConfigRepository.getSchoolCalendar()
        val attendanceStatus = attendanceRepository.getAttendanceStatus(
            orgUnit = uiState.value.formBuilderState.orgUnit,
            program = uiState.value.program,
            date = selectedDate,
            filterDetailsState = uiState.value.attendanceSummaryState.filterDetailsState,
        ) ?: if (uiState.value.buttonStep != ButtonStep.NONE) {
            attendanceRepository.createAttendanceStatus(
                orgUnit = uiState.value.formBuilderState.orgUnit,
                program = uiState.value.program,
                date = selectedDate,
                filterDetailsState = uiState.value.attendanceSummaryState.filterDetailsState,
            )
        } else {
            null
        }

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
            eventDate = selectedDate,
        )

        _uiState.update {
            it.copy(
                isLoading = false,
                toolbarHeaders = updatedToolbar,
                attendanceStatus = attendanceStatus,
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

    private fun attendanceSummary() {
        viewModelScope.launch {
            val attendanceSummaryState = uiState.value.attendanceSummaryState
            formRepository.attendanceSummary(
                program = uiState.value.program,
                totalLearners = studentsIds.size,
            ) { summaries ->
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

    private fun save() {
        saveAttendanceEvents()
    }

    private fun startAttendance() {
        viewModelScope.launch {
            val current = uiState.value

            runCatching {
                if (current.allowAttendanceStatus) {
                    attendanceRepository.createAttendanceStatus(
                        orgUnit = current.formBuilderState.orgUnit,
                        program = current.program,
                        date = selectedDate,
                        filterDetailsState = current.attendanceSummaryState.filterDetailsState,
                    ) ?: error("Attendance status event could not be created")
                } else {
                    null
                }
            }.onSuccess { attendanceStatus ->
                formRepository.allowFormEdition(true)
                _uiState.update {
                    it.copy(
                        buttonStep = ButtonStep.EDITING,
                        attendanceStatus = attendanceStatus,
                        attendanceSummaryState = it.attendanceSummaryState.copy(
                            enableBulk = true,
                        ),
                    )
                }
            }.onFailure { error ->
                val message = friendlyErrorMessage(error)
                _uiState.update {
                    it.copy(errorMessage = message)
                }
                _errorEvent.emit(message)
            }
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
                if (current.allowAttendanceStatus) {
                    attendanceRepository.completeAttendanceStatus(
                        orgUnit = current.formBuilderState.orgUnit,
                        program = current.program,
                        date = selectedDate,
                        filterDetailsState = current.attendanceSummaryState.filterDetailsState,
                        totalLearners = studentsIds.size,
                        attendanceEvents = formRepository.attendanceButtonStateFlow.value.attendanceEvents,
                    ) ?: error("Attendance status event could not be completed")
                } else {
                    null
                }
            }.onSuccess { completedAttendanceStatus ->
                formRepository.allowFormEdition(false)
                _uiState.update {
                    it.copy(
                        buttonStep = ButtonStep.NONE,
                        attendanceStatus = completedAttendanceStatus ?: it.attendanceStatus,
                        attendanceSummaryState = it.attendanceSummaryState.copy(
                            enableBulk = false,
                        ),
                    )
                }
                loadAttendanceEventsByDate(selectedDate)
                _hasCachedData.value = false
                _snackbarEvent.emit(resourceManager.getString(R.string.attendance_saved))
                _syncEvent.emit(Unit)
            }.onFailure { error ->
                val message = friendlyErrorMessage(error)
                _uiState.update {
                    it.copy(errorMessage = message)
                }
                _errorEvent.emit(message)
            }
        }
    }

    private fun friendlyErrorMessage(error: Throwable) = when (error) {
        is D2Error -> {
            "${error.errorCode()} – ${
                error.message ?: resourceManager.getString(R.string.error_unexpected)
            }"
        }

        else -> error.message ?: resourceManager.getString(R.string.error_unexpected)
    }

    fun handleUiEvent(uiEvent: AttendanceUiEvent) {
        when (uiEvent) {
            is AttendanceUiEvent.OnDateSelect -> {
                viewModelScope.launch {
                    loadAttendanceEventsByDate(uiEvent.date)
                    attendanceSummary()
                }
            }

            is AttendanceUiEvent.OnEditClicked -> {
                startAttendance()
            }

            is AttendanceUiEvent.OnAttendanceClick -> {
                if (!uiState.value.allowAttendanceStatus) {
                    viewModelScope.launch {
                        formRepository.updateAttendanceEvent(
                            eventDate = selectedDate,
                            tei = uiEvent.tei,
                            buttonModel = uiEvent.buttonModel,
                        )
                        _hasCachedData.value = true
                        attendanceSummary()
                    }
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
                    save()
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
