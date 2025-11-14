package org.saudigitus.semis.core.form.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.form.data.model.FormFieldState
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


    fun initialize(formBuilderState: FormBuilderState) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(formBuilderState = formBuilderState)
            }
            loadForm(formBuilderState.program, formBuilderState.programStage)
        }
    }


    fun handleUiEvent(uiEvent: FormEvent) {
        viewModelScope.launch {
            when (uiEvent) {
                is FormEvent.LoadForm -> {
                    loadForm(
                        uiState.value.formBuilderState.program,
                        uiState.value.formBuilderState.programStage
                    )
                }
                is FormEvent.UpdateField -> updateField(uiEvent.dataElementUid, uiEvent.value)
                else -> {}
            }
        }
    }

    private suspend fun loadForm(program: String, programStage: String) {
        _uiState.update {
            it.copy(isLoading = true)
        }
        val fields = formRepository.getFormFields(program, programStage)
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
                        R.string.required_field, field.label)
                )
            }

            else -> field.copy(hasError = false, errorMessage = null)
        }
    }
}