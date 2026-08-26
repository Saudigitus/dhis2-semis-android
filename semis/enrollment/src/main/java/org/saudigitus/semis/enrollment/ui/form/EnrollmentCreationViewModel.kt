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
import org.saudigitus.campaign.core.data.models.OrgUnit
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
    val prefill: Map<String, String> = emptyMap(),
    val registrationStage: String? = null,
    val date: String? = null,
    val orgUnit: String = "",
    val orgUnitName: String? = null,
    val orgUnitLabel: String = "",
    val dateLabel: String = "",
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

    /**
     * Everything captured about the learner, in the order it was configured.
     *
     * The confirmation reads a learner out of this the same way the listings do, so that the same
     * person is recognised the same way in both places.
     */
    val learnerAttributes: List<Pair<String, String>>
        get() = captured[0].orEmpty()
            .flatMap { section -> section.formFields }
            .filter { field -> !field.value.isNullOrBlank() }
            .map { field -> field.label to field.value.orEmpty() }

    /**
     * What was captured on the registration stage, to be reported back on the confirmation.
     *
     * Which step that is comes from the configuration, so a deployment that orders its stages
     * differently still summarises the right one.
     */
    val registrationDetails: List<Pair<String, String>>
        get() {
            val index = plan.steps.indexOfFirst {
                it is EnrollmentStep.Stage && it.programStage == registrationStage
            }
            if (index == -1) return emptyList()

            val stageDetails = captured[index].orEmpty()
                .flatMap { section -> section.formFields }
                .filter { field -> !field.value.isNullOrBlank() }
                .map { field -> field.label to field.value.orEmpty() }

            // Where the record belongs and when it happened were settled on the way in, so they are
            // reported here rather than being left out of what the registration recorded.
            return listOfNotNull(
                orgUnitName?.takeIf { it.isNotBlank() }?.let { orgUnitLabel to it },
                date?.takeIf { it.isNotBlank() }?.let { dateLabel to it },
            ) + stageDetails
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

    /**
     * Prepares the flow.
     *
     * [academicYear], [grade] and [section] are what the user already picked to reach this screen.
     * They are carried in so the fields that ask for the same things arrive filled instead of
     * asking again for something that was just chosen.
     */
    fun initialize(
        program: String,
        orgUnit: String,
        orgUnitName: String? = null,
        academicYear: String? = null,
        grade: String? = null,
        section: String? = null,
    ) {
        if (_uiState.value.initialized) return
        this.program = program

        viewModelScope.launch {
            val config = runCatching { appConfigRepository.getAppConfig(program) }
                .getOrElse {
                    fail(resourceManager.getString(R.string.enrollment_config_unavailable))
                    return@launch
                }

            // Which field holds each of these is configured, never assumed, so a deployment that
            // maps them to other data elements keeps working without a change here.
            val academicYearField = runCatching { appConfigRepository.getSchoolCalendar() }
                .getOrNull()
                ?.academicYear

            val prefill = buildMap {
                academicYearField.putValue(this, academicYear)
                config?.registration?.grade.putValue(this, grade)
                config?.registration?.section.putValue(this, section)
            }

            _uiState.update {
                it.copy(
                    initialized = true,
                    plan = enrollmentPlan(config),
                    prefill = prefill,
                    orgUnit = orgUnit,
                    orgUnitName = orgUnitName,
                    orgUnitLabel = resourceManager.getString(R.string.enrollment_summary_org_unit),
                    dateLabel = resourceManager.getString(R.string.enrollment_summary_date),
                    registrationStage = config?.registration?.programStage
                        ?.trim()
                        ?.takeIf(String::isNotEmpty),
                )
            }
        }
    }

    /** Records [value] under this field, skipping anything the configuration left unset. */
    private fun String?.putValue(target: MutableMap<String, String>, value: String?) {
        val field = this?.trim()?.takeIf(String::isNotEmpty) ?: return
        val known = value?.trim()?.takeIf(String::isNotEmpty) ?: return
        target[field] = known
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

    /**
     * Records the date the user set for the record.
     *
     * It is settled once and applied to the enrollment and to every event the flow produces, so
     * that a record made for a past day is dated for that day throughout.
     */
    fun onDateSelected(date: String) {
        _uiState.update { it.copy(date = date.takeIf(String::isNotBlank)) }
    }

    /**
     * Records the school the user set for the record.
     *
     * Like the date, it is settled once and everything the flow produces is written against it, so
     * the enrollment lands where the user said rather than where they happened to come from.
     */
    fun onOrgUnitSelected(orgUnit: OrgUnit) {
        val uid = orgUnit.uid.takeIf(String::isNotBlank) ?: return

        _uiState.update { it.copy(orgUnit = uid, orgUnitName = orgUnit.displayName) }
    }

    /** Surfaces a failure raised while a step was being filled in. */
    fun onStepError(message: String) = fail(message)

    /** Clears a reported failure so the user can carry on from the step they were on. */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Starts a fresh enrollment, keeping the plan that was already resolved.
     *
     * Registering learners tends to happen in a sitting, one after another, so the flow returns to
     * the first step rather than making the user leave and come back for each one.
     */
    fun registerAnother() {
        _uiState.update {
            it.copy(
                stepIndex = 0,
                captured = emptyMap(),
                completed = false,
                tei = null,
                enrollment = null,
                date = null,
                errorMessage = null,
            )
        }
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
                    orgUnit = _uiState.value.orgUnit,
                    program = program,
                    date = _uiState.value.date
                        ?: DateHelper.formatDate(System.currentTimeMillis()).orEmpty(),
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
