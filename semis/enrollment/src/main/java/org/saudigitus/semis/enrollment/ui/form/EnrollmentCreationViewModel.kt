package org.saudigitus.semis.enrollment.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.form.data.repository.FormRepository
import org.saudigitus.campaign.core.navigation.FormType
import org.saudigitus.campaign.core.utils.DateHelper
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.enrollment.R
import org.saudigitus.semis.enrollment.ui.form.model.EnrollmentPlan
import org.saudigitus.semis.enrollment.ui.form.model.EnrollmentStep
import org.saudigitus.semis.enrollment.ui.form.model.enrollmentPlan
import javax.inject.Inject

data class EnrollmentCreationUiState(
    val initialized: Boolean = false,
    val plan: EnrollmentPlan = EnrollmentPlan(),
    val stepIndex: Int = 0,
    val captured: Map<Int, List<FormSectionModel>> = emptyMap(),
    val isProcessing: Boolean = false,
    val completed: Boolean = false,
    val tei: String? = null,
    val enrollment: String? = null,
    val errorMessage: String? = null,
) {
    /** The step being filled in, or null once the plan has been walked through. */
    val currentStep: EnrollmentStep? get() = plan.steps.getOrNull(stepIndex)

    /** True on the step whose action commits the enrollment rather than moving on. */
    val isLastStep: Boolean get() = stepIndex >= plan.steps.lastIndex

    /** Step position as the user counts it, starting at one. */
    val stepNumber: Int get() = stepIndex + 1

    /** True while there is an earlier step to return to. */
    val canGoBack: Boolean get() = stepIndex > 0

    /** Values already captured for the step being shown, so returning to it is not a blank form. */
    val currentStepSections: List<FormSectionModel>? get() = captured[stepIndex]

    /** The program stage of the step being shown, absent on the step that captures attributes. */
    val currentProgramStage: String?
        get() = (currentStep as? EnrollmentStep.Stage)?.programStage

    /** Which kind of form the step being shown needs. */
    val currentFormType: String
        get() = when (currentStep) {
            is EnrollmentStep.Stage -> FormType.NEW_EVENT_WITH_REGISTRATION
            else -> FormType.NEW_ENROLLMENT
        }
}

/**
 * Walks the user through the enrollment and commits it once, at the end.
 *
 * The steps come from the configuration and are resolved before the first one is shown, so nothing
 * is written until the last step is completed. An enrollment abandoned or failing partway therefore
 * leaves nothing behind, instead of the half made learner that saving each form separately produced.
 */
@HiltViewModel
class EnrollmentCreationViewModel @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val formRepository: FormRepository,
    private val resourceManager: ResourceManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnrollmentCreationUiState())
    val uiState: StateFlow<EnrollmentCreationUiState> = _uiState.asStateFlow()

    private var program: String = ""
    private var orgUnit: String = ""

    fun initialize(program: String, orgUnit: String) {
        if (_uiState.value.initialized) return
        this.program = program
        this.orgUnit = orgUnit

        viewModelScope.launch {
            val plan = runCatching { appConfigRepository.getAppConfig(program) }
                .map(::enrollmentPlan)
                .getOrElse {
                    fail(resourceManager.getString(R.string.enrollment_config_unavailable))
                    return@launch
                }

            _uiState.update { it.copy(initialized = true, plan = plan) }
        }
    }

    /**
     * Keeps what the current step captured and moves on, committing once the last step is done.
     */
    fun onStepCompleted(formSections: List<FormSectionModel>) {
        val current = _uiState.value
        val captured = current.captured + (current.stepIndex to formSections)

        if (!current.isLastStep) {
            _uiState.update {
                it.copy(captured = captured, stepIndex = it.stepIndex + 1, errorMessage = null)
            }
            return
        }

        _uiState.update { it.copy(captured = captured, errorMessage = null) }
        commit(captured)
    }

    /** Returns to the previous step, keeping what was captured so far. */
    fun onBack() {
        _uiState.update {
            if (it.canGoBack) it.copy(stepIndex = it.stepIndex - 1, errorMessage = null) else it
        }
    }

    /** Surfaces a failure raised while a step was being filled in. */
    fun onStepError(message: String) = fail(message)

    /** Clears a reported failure so the user can carry on from the step they were on. */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun commit(captured: Map<Int, List<FormSectionModel>>) {
        val plan = _uiState.value.plan
        val attributes = captured[0].orEmpty()
        val stages = plan.steps.withIndex()
            .mapNotNull { (index, step) ->
                val stage = (step as? EnrollmentStep.Stage)?.programStage ?: return@mapNotNull null
                stage to captured[index].orEmpty()
            }
            .toMap()

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            runCatching {
                formRepository.saveEnrollment(
                    orgUnit = orgUnit,
                    program = program,
                    date = DateHelper.formatDate(System.currentTimeMillis()).orEmpty(),
                    attributes = attributes,
                    stages = stages,
                    backgroundStages = plan.backgroundStages,
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        tei = result.tei,
                        enrollment = result.enrollment,
                        isProcessing = false,
                        completed = true,
                    )
                }
            }.onFailure { error ->
                fail(
                    error.message?.takeIf(String::isNotBlank)
                        ?: resourceManager.getString(R.string.enrollment_not_saved),
                )
            }
        }
    }

    private fun fail(message: String) {
        _uiState.update { it.copy(isProcessing = false, errorMessage = message) }
    }
}
