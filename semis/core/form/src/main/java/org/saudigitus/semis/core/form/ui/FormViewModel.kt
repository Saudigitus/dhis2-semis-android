package org.saudigitus.semis.core.form.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.form.data.model.FormFieldData
import org.saudigitus.semis.core.form.data.model.FormFieldState
import org.saudigitus.semis.core.form.data.model.FormType
import org.saudigitus.semis.core.form.data.repository.FormRepository
import org.saudigitus.semis.core.form.ui.state.FormBuilderState
import org.saudigitus.semis.core.form.ui.state.FormEvent
import org.saudigitus.semis.core.form.ui.state.FormUiState
import javax.inject.Inject

@HiltViewModel
class FormViewModel @Inject constructor(
    private val formRepository: FormRepository,
    private val resourceManager: ResourceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FormUiState(
            isLoading = true,
            toolbarHeaders = ToolbarHeaders(
                title = resourceManager.getString(R.string.form),
            )
        ),
    )
    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            _uiState.value
        )

    val attendanceState = formRepository
        .attendanceButtonStateFlow.map {
            it
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            formRepository.attendanceButtonStateFlow.value
        )

    init {
        viewModelScope.launch {
            formRepository.attendanceButtonStateFlow
                .collectLatest { newAttendanceState ->
                    _uiState.update { currentState ->
                        currentState.copy(attendanceButtonState = newAttendanceState)
                    }
                }
        }
    }

    fun initialize(formBuilderState: FormBuilderState) {
        viewModelScope.launch {
            val dl = formBuilderState.dataElement.ifEmpty { null }

            _uiState.update {
                it.copy(
                    formBuilderState = formBuilderState,
                    attendanceButtonState = attendanceState.value
                )
            }
            loadForm(formBuilderState.program, formBuilderState.programStage, dl)
        }
    }

    private fun updateAttendanceEvent(
        tei: SearchTeiModel?,
        buttonModel: AttendanceButtonModel
    ) {
        viewModelScope.launch {
            val attendanceButtonState = formRepository.updateAttendanceEvent(
                uiState.value.formBuilderState.date,
                tei,
                buttonModel
            )

            _uiState.update {
                it.copy(
                    hasCachedData = true,
                    attendanceButtonState = attendanceButtonState
                )
            }
        }
    }

    fun collectIndividualFormEvent(formFieldData: List<FormFieldData>) {
        _uiState.update {
            it.copy(fieldsData = formFieldData)
        }
    }

    fun collectIndividualSummary(getSummaries: (List<BottomSheetModel>) -> Unit) {
        viewModelScope.launch {
            uiState.collectLatest {
                val summary = formRepository.individualFormSummary(it.fieldsData)
                getSummaries(summary)
            }
        }
    }

    private fun collectIndividual(tei: String, dataElement: String, value: String) {
        val currentFormData = uiState.value.fieldsData.toMutableList()
        val index = currentFormData.indexOfFirst { it.tei == tei && it.dataElement == dataElement }

        if (index != -1) {
            val removedFormField = currentFormData.removeAt(index)
            val updatedField = removedFormField.copy(value = value, isUpdated = true)
            currentFormData.add(index, updatedField)

            _uiState.update {
                it.copy(hasCachedData = true, fieldsData = currentFormData)
            }
        }
    }

    fun enableForm(enable: Boolean = true) {
        _uiState.update { it.copy(isEnabled = enable) }
    }

    fun resetCacheStatus() {
        _uiState.update { it.copy(hasCachedData = false) }
    }

    fun handleUiEvent(uiEvent: FormEvent) {
        viewModelScope.launch {
            when (uiEvent) {
                is FormEvent.LoadForm -> {
                    val dl = uiState.value.formBuilderState.dataElement.ifEmpty { null }

                    loadForm(
                        uiState.value.formBuilderState.program,
                        uiState.value.formBuilderState.programStage,
                        dl,
                    )
                }

                is FormEvent.UpdateAttendance -> {
                    updateAttendanceEvent(uiEvent.tei, uiEvent.buttonModel)
                }

                is FormEvent.UpdateField -> {
                    when (uiEvent.formType) {
                        FormType.ATTENDANCE -> {
                            formRepository.updateAttendanceReason(
                                uiEvent.tei,
                                uiEvent.dataElementUid,
                                uiEvent.value
                            )
                        }

                        FormType.INDIVIDUAL -> {
                            collectIndividual(uiEvent.tei, uiEvent.dataElementUid, uiEvent.value)
                        }

                        else -> {
                            updateField(
                                uiEvent.dataElementUid,
                                uiEvent.value
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private suspend fun loadForm(program: String, programStage: String, dl: String? = null) {
        _uiState.update {
            it.copy(isLoading = true)
        }
        val fields = formRepository.getFormFields(program, programStage, dl)
        val updatedFields = formRepository.applyProgramRules(
            uiState.value.formBuilderState.orgUnit,
            uiState.value.formBuilderState.program,
            programStage,
            fields,
        )
        _uiState.update {
            it.copy(isLoading = false, fields = updatedFields)
        }
    }

    private suspend fun updateField(dataElement: String, value: String) {
        _uiState.update { current ->
            val fields = current.fields.map { field ->
                if (field.dataElementUid == dataElement) {
                    validateField(field.copy(value = value))
                } else field
            }

            val withRules = formRepository.applyProgramRules(
                uiState.value.formBuilderState.orgUnit,
                uiState.value.formBuilderState.program,
                uiState.value.formBuilderState.programStage,
                fields,
            )
            current.copy(fields = withRules)
        }
    }

    private fun validateField(field: FormFieldState): FormFieldState {

        if (!field.enabled || !field.rendered) return field.copy(
            hasError = false,
            errorMessage = null
        )

        return when {
            field.mandatory && field.value.isNullOrBlank() -> {
                field.copy(
                    hasError = true,
                    errorMessage = resourceManager.getString(
                        R.string.required_field, field.label
                    )
                )
            }

            else -> field.copy(hasError = false, errorMessage = null)
        }
    }
}