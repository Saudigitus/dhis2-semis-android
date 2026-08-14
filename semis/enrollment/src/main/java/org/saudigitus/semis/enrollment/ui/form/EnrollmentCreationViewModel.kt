package org.saudigitus.semis.enrollment.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.saudigitus.campaign.core.form.data.models.FormResult
import org.saudigitus.campaign.core.form.ui.state.FormSectionType
import org.saudigitus.campaign.core.navigation.FormType
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.EventRepository
import javax.inject.Inject

data class EnrollmentCreationUiState(
    val initialized: Boolean = false,
    val formType: String = FormType.NEW_ENROLLMENT,
    val programStage: String? = null,
    val tei: String? = null,
    val enrollment: String? = null,
    val pendingInteractiveStages: List<String> = emptyList(),
    val backgroundStages: List<String> = emptyList(),
    val isProcessing: Boolean = false,
    val completed: Boolean = false,
    val errorMessage: String? = null,
)

/** Coordinates the complete enrollment transaction defined in SEMISConfig. */
@HiltViewModel
class EnrollmentCreationViewModel @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnrollmentCreationUiState())
    val uiState: StateFlow<EnrollmentCreationUiState> = _uiState.asStateFlow()

    private var program: String = ""
    private var orgUnit: String = ""

    fun initialize(program: String, orgUnit: String) {
        if (_uiState.value.initialized) return
        this.program = program
        this.orgUnit = orgUnit
        _uiState.update { it.copy(initialized = true) }
    }

    fun onFormSaved(formType: FormSectionType, result: FormResult) {
        when (formType) {
            FormSectionType.NEW_ENROLLMENT -> startConfiguredStages(result)
            FormSectionType.NEW_EVENT_WITH_REGISTRATION -> advanceInteractiveStages(result)
            else -> Unit
        }
    }

    private fun startConfiguredStages(result: FormResult) {
        val tei = result.tei?.takeIf(String::isNotBlank)
        val enrollment = result.enrollment?.takeIf(String::isNotBlank)
        if (tei == null || enrollment == null) {
            fail("The enrollment was saved without a TEI or enrollment UID")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            runCatching {
                val config = appConfigRepository.getAppConfig(program)

                val interactiveStages = listOfNotNull(
                    config?.registration?.programStage.cleanUid(),
                    config?.socioEconomics?.programStage.cleanUid(),
                ).distinct()

                val backgroundStages = buildList {
                    config?.performance?.programStages.orEmpty().forEach { stage ->
                        stage?.programStage.cleanUid()?.let(::add)
                    }
                    config?.finalResult?.programStage.cleanUid()?.let(::add)
                }.distinct()

                Triple(interactiveStages, backgroundStages, tei to enrollment)
            }.onSuccess { (interactiveStages, backgroundStages, ids) ->
                if (interactiveStages.isNotEmpty()) {
                    _uiState.value = EnrollmentCreationUiState(
                        initialized = true,
                        formType = FormType.NEW_EVENT_WITH_REGISTRATION,
                        programStage = interactiveStages.first(),
                        tei = ids.first,
                        enrollment = ids.second,
                        pendingInteractiveStages = interactiveStages.drop(1),
                        backgroundStages = backgroundStages,
                    )
                } else {
                    createBackgroundEvents(ids.first, ids.second, backgroundStages)
                }
            }.onFailure { error ->
                fail(error.message ?: "Unable to read SEMISConfig")
            }
        }
    }

    private fun advanceInteractiveStages(result: FormResult) {
        val current = _uiState.value
        if (!result.isEventSaved) {
            fail("The program stage event could not be saved")
            return
        }

        val nextStage = current.pendingInteractiveStages.firstOrNull()
        if (nextStage != null) {
            _uiState.update {
                it.copy(
                    formType = FormType.NEW_EVENT_WITH_REGISTRATION,
                    programStage = nextStage,
                    pendingInteractiveStages = it.pendingInteractiveStages.drop(1),
                    errorMessage = null,
                )
            }
        } else {
            createBackgroundEvents(
                tei = current.tei.orEmpty(),
                enrollment = current.enrollment.orEmpty(),
                stages = current.backgroundStages,
            )
        }
    }

    private fun createBackgroundEvents(
        tei: String,
        enrollment: String,
        stages: List<String>,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            runCatching {
                stages.distinct().forEach { stage ->
                    eventRepository.createEmptyEvent(
                        orgUnit = orgUnit,
                        program = program,
                        programStage = stage,
                        enrollment = enrollment,
                    )
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        tei = tei,
                        enrollment = enrollment,
                        isProcessing = false,
                        completed = true,
                    )
                }
            }.onFailure { error ->
                fail(error.message ?: "Unable to create configured program stage events")
            }
        }
    }

    private fun fail(message: String) {
        _uiState.update { it.copy(isProcessing = false, errorMessage = message) }
    }

    private fun String?.cleanUid(): String? = this?.trim()?.takeIf(String::isNotEmpty)
}
