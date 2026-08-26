package org.saudigitus.campaign.core.form.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.data.models.FormResult
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.form.data.repository.FormRepository
import org.saudigitus.campaign.core.form.ui.model.FormSection
import org.saudigitus.campaign.core.form.ui.state.FormEvent
import org.saudigitus.campaign.core.form.ui.state.FormSectionType
import org.saudigitus.campaign.core.form.ui.state.FormSectionUiState
import org.saudigitus.campaign.core.form.utils.CustomValueType
import org.saudigitus.campaign.core.form.utils.hasBlockingFields
import org.saudigitus.campaign.core.form.utils.phone.MozambiquePhoneValidator
import org.saudigitus.campaign.core.navigation.AppRoute
import org.saudigitus.campaign.core.navigation.FormType
import org.saudigitus.campaign.core.utils.DateHelper
import org.saudigitus.campaign.core.utils.formatBoolean
import org.saudigitus.campaign.core.utils.formatTrueOnly
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class FormNavigationEvent(
    val route: AppRoute?,
    val formType: FormSectionType,
    val result: FormResult,
)

@OptIn(FlowPreview::class)
class FormViewModel @Inject constructor(
    private val formRepository: FormRepository,
    private val resourceManager: ResourceManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<FormSectionUiState>(FormSectionUiState.Loading)

    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            _uiState.value
        )

    private val formType = MutableStateFlow(FormSectionType.NEW_ENROLLMENT)
    private val formResult = MutableStateFlow<FormResult?>(null)

    private val _navigationEvent = MutableSharedFlow<FormNavigationEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _handleSave = MutableSharedFlow<Boolean>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val handleSave = _handleSave.asSharedFlow()

    /**
     * Fields captured on a step that is part of a longer flow, handed to whoever drives that flow.
     *
     * Nothing is written when this is emitted: the caller accumulates the steps and decides when the
     * whole thing is committed, which is what keeps an interrupted flow from leaving a half made
     * record behind.
     */
    private val _stepCompleted = MutableSharedFlow<List<FormSectionModel>>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val stepCompleted = _stepCompleted.asSharedFlow()

    /** Message to show when the form could not be saved, already resolved for display. */
    private val _errorEvent = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val errorEvent = _errorEvent.asSharedFlow()

    private var ruleJob: Job? = null

    private var enrollmentUid = MutableStateFlow<String?>(null)

    /** True while this form only gathers values for a caller that saves later. */
    private var collectsOnly = false

    /**
     * True when finishing this form commits the whole flow, so the user is asked to confirm.
     *
     * The steps before it write nothing and move on, and asking to confirm those would be asking
     * about something that has not happened yet.
     */
    private var confirmsOnComplete = false

    /** Guards against a second submission while the first is still running. */
    private var submitting = false

    /**
     * Prepares the form.
     *
     * [restoredSections] carries values already captured for this form, so that returning to a step
     * of a longer flow shows what was typed instead of a blank form reloaded from configuration.
     */
    fun initialize(
        formSection: FormSection?,
        ouName: String? = null,
        collectOnly: Boolean = false,
        restoredSections: List<FormSectionModel>? = null,
        confirmOnComplete: Boolean = false,
    ) {
        _uiState.value = FormSectionUiState.Loading
        collectsOnly = collectOnly
        confirmsOnComplete = confirmOnComplete
        submitting = false
        when (formSection) {
            is FormSection.NewEnrollment -> {
                newEnrollment(formSection, ouName, restoredSections)
            }

            is FormSection.EditEnrollment -> {
                editEnrollment(formSection)
            }

            is FormSection.NewEvent -> {
                newEvent(formSection, ouName, restoredSections)
            }

            else -> Unit
        }
    }

    private fun newEvent(
        eventForm: FormSection.NewEvent,
        ouName: String? = null,
        restoredSections: List<FormSectionModel>? = null,
    ) {
        viewModelScope.launch {
            formType.value = eventForm.formType
            enrollmentUid.value = eventForm.enrollment
            formResult.value = FormResult(
                enrollment = eventForm.enrollment,
                tei = eventForm.trackerUid,
            )
            val programStage = eventForm.programStage ?: formRepository.getDefaultProgramStage(
                program = eventForm.program
            )

            val sections = restoredSections ?: formRepository.getFormSections(
                orgUnit = eventForm.orgUnit,
                program = eventForm.program,
                programStages = arrayOf(programStage),
                tei = eventForm.trackerUid,
                enrollment = eventForm.enrollment
            )

            if (sections.isEmpty()) {
                _uiState.value = FormSectionUiState.Idle
            } else {
                _uiState.value =
                    FormSectionUiState.HasFormSection(
                        formType = formType.value,
                        orgUnit = OrgUnit(eventForm.orgUnit, ouName),
                        program = eventForm.program,
                        programStage = programStage,
                        trackerId = eventForm.trackerUid,
                        enrollmentId = eventForm.enrollment,
                        date = DateHelper.formatDate(System.currentTimeMillis()).orEmpty(),
                        formSections = sections
                    )
            }
        }
    }

    private fun newEnrollment(
        enrollmentForm: FormSection.NewEnrollment,
        ouName: String? = null,
        restoredSections: List<FormSectionModel>? = null,
    ) {
        viewModelScope.launch {
            formType.value = enrollmentForm.formType
            val sections = restoredSections ?: formRepository.getFormSections(
                enrollmentForm.orgUnit,
                enrollmentForm.program
            )

            if (sections.isEmpty()) {
                _uiState.value = FormSectionUiState.Idle
            } else {
                _uiState.value = FormSectionUiState.HasFormSection(
                    formType = formType.value,
                    previousType = enrollmentForm.formType,
                    orgUnit = OrgUnit(enrollmentForm.orgUnit, ouName),
                    program = enrollmentForm.program,
                    programStage = formRepository.getDefaultProgramStage(
                        program = enrollmentForm.program
                    ),
                    date = DateHelper.formatDate(System.currentTimeMillis()).orEmpty(),
                    formSections = sections
                )
            }
        }
    }

    private fun editEnrollment(enrollmentForm: FormSection.EditEnrollment) {
        viewModelScope.launch {
            formType.value = enrollmentForm.formType
            formResult.value = FormResult(
                enrollment = enrollmentForm.enrollment,
                tei = enrollmentForm.tei
            )
            val sections = formRepository.getFormSections(
                enrollmentForm.orgUnit,
                enrollmentForm.program,
                enrollmentForm.tei,
            )

            if (sections.isEmpty()) {
                _uiState.value = FormSectionUiState.Idle
            } else {
                val orgUnitName = formRepository.getOrgUnitName(enrollmentForm.orgUnit)

                _uiState.value = FormSectionUiState.HasFormSection(
                    formType = formType.value,
                    previousType = enrollmentForm.formType,
                    orgUnit = OrgUnit(enrollmentForm.orgUnit, orgUnitName),
                    program = enrollmentForm.program,
                    programStage = formRepository.getDefaultProgramStage(
                        program = enrollmentForm.program
                    ),
                    enrollmentId = enrollmentForm.enrollment,
                    trackerId = enrollmentForm.tei,
                    date = DateHelper.formatDate(System.currentTimeMillis()).orEmpty(),
                    formSections = sections
                )
            }
        }
    }

    private fun setOrgUnit(orgUnit: OrgUnit) {
        when (val current = uiState.value) {
            is FormSectionUiState.HasFormSection -> {
                _uiState.update {
                    current.copy(orgUnit = orgUnit)
                }
            }

            else -> {}
        }
    }

    private fun setDate(date: String) {
        when (val current = uiState.value) {
            is FormSectionUiState.HasFormSection -> {
                _uiState.update {
                    current.copy(date = date)
                }
            }

            else -> {}
        }
    }

    private suspend fun applyRules(
        event: String,
        formSections: List<FormSectionModel>,
    ): List<FormSectionModel> {
        // The form can be torn down between the debounce and this call, so the state is read
        // defensively and the sections are handed back untouched rather than crashing.
        return when (formType.value) {
            FormSectionType.NEW_EVENT_WITH_REGISTRATION,
            FormSectionType.NEW_EVENT_WITHOUT_REGISTRATION -> {
                val current = uiState.value as? FormSectionUiState.HasFormSection
                    ?: return formSections

                formRepository.applyProgramRules(
                    orgUnit = current.orgUnit?.uid.orEmpty(),
                    program = current.program.orEmpty(),
                    programStage = current.programStage.orEmpty(),
                    event = event,
                    enrollment = enrollmentUid.value,
                    formSections = formSections
                )
            }

            else -> {
                val current = uiState.value as? FormSectionUiState.HasFormSection
                    ?: return formSections

                formRepository.applyProgramRules(
                    orgUnit = current.orgUnit?.uid.orEmpty(),
                    program = current.program.orEmpty(),
                    formSections = formSections
                )
            }
        }
    }

    private fun debounceRules(eventUid: String) {
        ruleJob?.cancel()

        ruleJob = viewModelScope.launch {

            delay(300.milliseconds)

            val evaluatedState = _uiState.value as? FormSectionUiState.HasFormSection
                ?: return@launch

            val evaluatedSections = withContext(Dispatchers.IO) {
                applyRules(eventUid, evaluatedState.formSections)
            }

            _uiState.update { latest ->
                val state = latest as? FormSectionUiState.HasFormSection ?: return@update latest

                // Whatever was typed while the rules were being evaluated lives in the state that
                // is current now, not in the snapshot they ran against. Writing that snapshot back
                // would silently drop those keystrokes, so only the outcome of the rules is taken
                // and the values are kept as they stand.
                state.copy(
                    formSections = state.formSections.withRuleOutcome(evaluatedSections),
                )
            }
        }
    }

    /**
     * Applies the outcome of a rule evaluation to these sections without touching their values.
     *
     * The rules decide what is shown, what is required and what is flagged; the values belong to
     * the user and are the one thing an evaluation that started earlier must not be allowed to
     * revert.
     */
    private fun List<FormSectionModel>.withRuleOutcome(
        evaluated: List<FormSectionModel>,
    ): List<FormSectionModel> {
        val evaluatedFields = evaluated
            .flatMap { section -> section.formFields }
            .associateBy { field -> field.uid }
        val evaluatedSections = evaluated.associateBy { section -> section.uid }

        return map { section ->
            section.copy(
                rendered = evaluatedSections[section.uid]?.rendered ?: section.rendered,
                formFields = section.formFields.map { field ->
                    val outcome = evaluatedFields[field.uid] ?: return@map field
                    field.copy(
                        rendered = outcome.rendered,
                        mandatory = outcome.mandatory,
                        enabled = outcome.enabled,
                        hasError = outcome.hasError,
                        errorMessage = outcome.errorMessage,
                        hasWarning = outcome.hasWarning,
                        warningMessage = outcome.warningMessage,
                    )
                },
            )
        }
    }

    private fun updateField(section: FormSectionModel, uid: String, value: String) {
        _uiState.update { current ->
            val state = current as? FormSectionUiState.HasFormSection
                ?: return@update current

            val updatedSections = state.formSections.map { currentSection ->
                // Matched by identity rather than by comparing the whole section: the section the
                // screen hands back was captured when it was last drawn, so while someone types
                // faster than the screen redraws it no longer matches the one held here, and every
                // keystroke after the first would be dropped without a trace.
                if (currentSection.uid != section.uid) return@map currentSection

                val updatedFields = currentSection.formFields.map { field ->
                    val cleanedValue = when (field.valueType) {
                        ValueType.PHONE_NUMBER -> MozambiquePhoneValidator.clean(value)
                        ValueType.BOOLEAN -> value.formatBoolean()
                        ValueType.TRUE_ONLY -> value.formatTrueOnly()
                        else -> value
                    }

                    if (field.uid == uid) {
                        field.copy(
                            value = cleanedValue,
                            hasError = field.valueType == ValueType.PHONE_NUMBER &&
                                (cleanedValue.isNotEmpty() && !MozambiquePhoneValidator.isValid(
                                    cleanedValue
                                )),
                            errorMessage = if (field.valueType == ValueType.PHONE_NUMBER &&
                                (cleanedValue.isNotEmpty() && !MozambiquePhoneValidator.isValid(
                                    cleanedValue
                                ))
                            ) {
                                resourceManager.getString(R.string.invalide_phone_num)
                            } else null
                        )
                    } else {
                        field
                    }
                }

                currentSection.copy(formFields = updatedFields)
            }

            state.copy(formSections = updatedSections)
        }
        debounceRules(section.eventUid.orEmpty())
    }

    private var searchJob: Job? = null

    fun searchFieldQuery(section: FormSectionModel, uid: String, query: String) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(300.milliseconds)

            val currentState = _uiState.value as? FormSectionUiState.HasFormSection
                ?: return@launch

            // Found by identity for the same reason the field update is: the section handed back
            // by the screen goes stale as soon as anything in it changes.
            val sectionIndex = currentState.formSections.indexOfFirst { it.uid == section.uid }
            if (sectionIndex == -1) return@launch

            val targetSection = currentState.formSections[sectionIndex]
            val fieldIndex = targetSection.formFields.indexOfFirst { it.uid == uid }
            if (fieldIndex == -1) return@launch

            val field = targetSection.formFields[fieldIndex]

            val updatedField = withContext(Dispatchers.Default) {
                filterField(currentState.orgUnit?.uid.orEmpty(), field, query)
            }

            _uiState.update { state ->
                val s = state as? FormSectionUiState.HasFormSection ?: return@update state

                val updatedSections = s.formSections.toMutableList()
                val updatedFields = targetSection.formFields.toMutableList()

                updatedFields[fieldIndex] = updatedField
                updatedSections[sectionIndex] = targetSection.copy(formFields = updatedFields)

                s.copy(formSections = updatedSections)
            }

            debounceRules(section.eventUid.orEmpty())
        }
    }

    private suspend fun filterField(ou: String, field: FormFieldModel, query: String): FormFieldModel {
        val normalizedQuery = query.lowercase()

        return field.copy(
            orgUnits = if (field.customValueType == CustomValueType.SEARCHABLE_ORG_UNIT_FIELD) {
                formRepository.searchOrgUnits(
                    normalizedQuery,
                    ou,
                    field.ouHideStrategy
                )
            } else field.orgUnits,

            optionSet = if (field.customValueType == CustomValueType.SEARCHABLE_FIELD) {
                field.optionSet?.filter {
                    it.displayName?.lowercase()?.contains(normalizedQuery) == true
                }
            } else field.optionSet
        )
    }

    private fun save() {
        // A repeated tap must not start a second submission: on this form that would create a
        // second learner, so the guard is checked before any work begins.
        if (submitting) return
        submitting = true

        viewModelScope.launch {
            val current = uiState.value as? FormSectionUiState.HasFormSection
            if (current == null) {
                submitting = false
                return@launch
            }

            if (current.formSections.hasBlockingFields()) {
                submitting = false
                return@launch
            }

            if (collectsOnly) {
                _stepCompleted.tryEmit(current.formSections)
                submitting = false
                return@launch
            }

            runCatching {
                if (formResult.value != null &&
                    !formResult.value?.tei.isNullOrEmpty() &&
                    !formResult.value?.enrollment.isNullOrEmpty()
                ) {
                    formRepository.save(
                        formType = formType.value,
                        orgUnit = current.orgUnit?.uid.orEmpty(),
                        program = current.program.orEmpty(),
                        tei = formResult.value?.tei,
                        enrollment = formResult.value?.enrollment,
                        date = current.date.orEmpty(),
                        formSections = current.formSections,
                    )
                } else {
                    formRepository.save(
                        formType = formType.value,
                        orgUnit = current.orgUnit?.uid.orEmpty(),
                        program = current.program.orEmpty(),
                        date = current.date.orEmpty(),
                        formSections = current.formSections,
                    )
                }
            }.onSuccess { result ->
                formResult.value = result
                submitting = false
                navigate(result, current)
            }.onFailure { error ->
                // Without this the failure would reach the coroutine scope uncaught and take the
                // app down instead of telling the user the record was not saved.
                submitting = false
                _errorEvent.tryEmit(
                    error.message?.takeIf(String::isNotBlank)
                        ?: resourceManager.getString(R.string.something_went_wrong_loading_form),
                )
            }
        }
    }

    private suspend fun navigate(
        result: FormResult,
        state: FormSectionUiState.HasFormSection
    ) {
        when (formType.value) {
            FormSectionType.NEW_ENROLLMENT -> {
                _navigationEvent.tryEmit(FormNavigationEvent(null, formType.value, result))
            }

            FormSectionType.EDIT_ENROLLMENT -> {
                _navigationEvent.tryEmit(FormNavigationEvent(
                    AppRoute.TrackerDetailRoute(
                        programUid = state.program.orEmpty(),
                        trackedEntityUid = result.tei.orEmpty(),
                        enrollmentUid = result.enrollment.orEmpty(),
                    ), formType.value, result
                ))
            }

            FormSectionType.NEW_EVENT_WITH_REGISTRATION -> {
                _navigationEvent.tryEmit(FormNavigationEvent(
                    AppRoute.TrackerDetailRoute(
                        programUid = state.program.orEmpty(),
                        trackedEntityUid = formResult.value?.tei ?: state.trackerId.orEmpty(),
                        enrollmentUid = formResult.value?.enrollment ?: state.enrollmentId.orEmpty()
                    ), formType.value, result
                ))
            }

            FormSectionType.NEW_EVENT_WITHOUT_REGISTRATION -> {
                _navigationEvent.tryEmit(FormNavigationEvent(null, formType.value, result))
            }
        }
    }

    private fun deleteEventOnDiscard() {
        viewModelScope.launch {
            val current = uiState.value as? FormSectionUiState.HasFormSection
                ?: return@launch

            val event = current.formSections.firstOrNull()?.eventUid

            if (!event.isNullOrEmpty()) {
                formRepository.deleteEvent(event)
            }
        }
    }

    fun handleUiEvent(uiEvent: FormEvent) {
        viewModelScope.launch {
            when (uiEvent) {
                is FormEvent.LoadForm -> Unit

                is FormEvent.SelectedOU -> {
                    setOrgUnit(uiEvent.orgUnit)
                }

                is FormEvent.SelectedDate -> {
                    setDate(uiEvent.date)
                }

                is FormEvent.UpdateField -> {
                    updateField(uiEvent.section, uiEvent.uid, uiEvent.value)
                }

                is FormEvent.SearchFieldQuery -> {
                    searchFieldQuery(uiEvent.section, uiEvent.uid, uiEvent.query)
                }

                is FormEvent.CancelSave -> {
                    _handleSave.tryEmit(false)
                }

                is FormEvent.ConfirmSave -> {
                    if (collectsOnly && !confirmsOnComplete) {
                        // A step that only moves on writes nothing, so there is nothing to confirm.
                        save()
                    } else {
                        _handleSave.tryEmit(true)
                    }
                }

                is FormEvent.SaveEvent -> {
                    save()
                }

                else -> Unit
            }
        }
    }

    fun reset() {
        // The rule job reads the state a moment after the fact, so it has to stop before the state
        // is torn down, otherwise it wakes to a form that is no longer there.
        ruleJob?.cancel()
        ruleJob = null

        deleteEventOnDiscard()

        _uiState.value = FormSectionUiState.Idle
        formResult.value = null
        formType.value = FormSectionType.NEW_ENROLLMENT
        collectsOnly = false
        submitting = false
    }

}
