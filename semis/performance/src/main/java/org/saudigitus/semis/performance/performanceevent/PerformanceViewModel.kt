package org.saudigitus.semis.performance.performanceevent

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
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.app_config.Performance
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.core.form.data.model.FormFieldData
import org.saudigitus.semis.core.form.data.repository.FormRepository
import org.saudigitus.semis.core.utils.ButtonStep
import org.saudigitus.semis.core.utils.DateHelper
import org.saudigitus.semis.performance.data.repository.PerformanceRepository
import javax.inject.Inject

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val performanceRepository: PerformanceRepository,
    private val formRepository: FormRepository,
    private val appConfigRepository: AppConfigRepository,
    private val resourceManager: ResourceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PerformanceUiState(
            isLoading = true,
            toolbarHeaders = ToolbarHeaders(
                title = resourceManager.getString(R.string.performance),
                subtitle = DateHelper.formatDateWithWeekDay(System.currentTimeMillis()).orEmpty()
            )
        )
    )
    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            _uiState.value
        )

    private val _snackbarEvent = MutableSharedFlow<String?>()
    val snackbarEvent: SharedFlow<String?> = _snackbarEvent

    private val _formFieldData = MutableStateFlow(emptyList<FormFieldData>())
    private val formFields: StateFlow<List<FormFieldData>> = _formFieldData


    private var performanceConfig: Performance? = null

    fun initialize(
        program: String,
        orgUnit: String,
        programStage: String,
        dataElement: String,
        tei: List<SearchTeiModel>,
        filterState: FilterComponentState,
        initFormFieldData: (data: List<FormFieldData>) -> Unit
    ) {
        viewModelScope.launch {
            disableEditing()
            val currentSummaryState = uiState.value.summaryState
            val currentFormState = uiState.value.formBuilderState

            val config = appConfigRepository.getAppConfig(program)
            performanceConfig = config?.performance

            val teis = tei.map { it.tei.uid() }

            val events = performanceRepository.getEvents(
                ou = orgUnit,
                program = program,
                programStage = programStage,
                dataElement = dataElement,
                teis = teis
            )

            initFormFieldData(events)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    program = program,
                    tei = tei,
                    summaryState = currentSummaryState.copy(
                        filterDetailsState = filterState.filterDetailsState,
                        bottomSheetModels = formRepository.individualFormSummary(events)
                    ),
                    formBuilderState = currentFormState.copy(
                        orgUnit = orgUnit,
                        program = program,
                        programStage = programStage,
                        dataElement = dataElement
                    )
                )
            }
        }
    }

    fun updateFormFields(formFieldData: List<FormFieldData>) {
        _formFieldData.value = formFieldData
    }

    fun performanceSummary(summary: List<BottomSheetModel>) {
        val currentSummaryState = uiState.value.summaryState
        _uiState.update {
            it.copy(
                summaryState = currentSummaryState.copy(
                    bottomSheetModels = summary
                )
            )
        }

    }

    fun performSave(resetFormCache: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                performanceRepository.savePerformance(
                    orgUnit = uiState.value.formBuilderState.orgUnit,
                    program = uiState.value.formBuilderState.program,
                    programStage = uiState.value.formBuilderState.programStage,
                    teiList = uiState.value.tei,
                    formFieldsData = formFields.value
                )
            }.onSuccess {
                resetFormCache()
                _snackbarEvent.emit(resourceManager.getString(R.string.performance_saved))
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

    fun disableEditing() {
        formRepository.allowFormEdition(false)
    }

    fun resetForm() {
        formRepository.reset()
    }

    fun handleUiEvent(
        event: PerformanceUiEvent,
        enableForm: (enable: Boolean) -> Unit,
        resetFormCache: () -> Unit
    ) {
        when (event) {
            is PerformanceUiEvent.EditEvent -> {
                enableForm(true)
                _uiState.update { it.copy(buttonStep = ButtonStep.EDITING) }
            }

            is PerformanceUiEvent.CancelEventData -> {
                _uiState.update { it.copy(isConfirmDialog = false) }
            }

            is PerformanceUiEvent.ConfirmEventData -> {
                _uiState.update { it.copy(isConfirmDialog = true) }
            }

            is PerformanceUiEvent.SaveEvent -> {
                _uiState.update { it.copy(isConfirmDialog = false) }
                performSave(resetFormCache)
                enableForm(false)
            }

            else -> {}
        }
    }
}