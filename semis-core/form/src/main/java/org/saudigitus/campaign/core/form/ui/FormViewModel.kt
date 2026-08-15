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

    private var ruleJob: Job? = null

    private var enrollmentUid = MutableStateFlow<String?>(null)


    fun initialize(formSection: FormSection?, ouName: String? = null) {
        _uiState.value = FormSectionUiState.Loading
        when (formSection) {
            is FormSection.NewEnrollment -> {
                newEnrollment(formSection, ouName)
            }

            is FormSection.EditEnrollment -> {
                editEnrollment(formSection)
            }

            is FormSection.NewEvent -> {
                newEvent(formSection, ouName)
            }

            else -> Unit
        }
    }

    private fun newEvent(eventForm: FormSection.NewEvent, ouName: String? = null) {
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

            val sections = formRepository.getFormSections(
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

    private fun newEnrollment(enrollmentForm: FormSection.NewEnrollment, ouName: String? = null) {
        viewModelScope.launch {
            formType.value = enrollmentForm.formType
            val sections = formRepository.getFormSections(
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
        return when (formType.value) {
            FormSectionType.NEW_EVENT_WITH_REGISTRATION,
            FormSectionType.NEW_EVENT_WITHOUT_REGISTRATION -> {
                val current = uiState.value as FormSectionUiState.HasFormSection

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
                val current = uiState.value as FormSectionUiState.HasFormSection

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

            val currentState = _uiState.value as? FormSectionUiState.HasFormSection
                ?: return@launch

            val updatedSections = withContext(Dispatchers.IO) {
                applyRules(eventUid, currentState.formSections)
            }

            _uiState.update {
                currentState.copy(formSections = updatedSections)
            }
        }
    }

    private fun updateField(section: FormSectionModel, uid: String, value: String) {
        _uiState.update { current ->
            val state = current as? FormSectionUiState.HasFormSection
                ?: return@update current

            val updatedSections = state.formSections.map { currentSection ->
                if (currentSection != section) return@map currentSection

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

            val sectionIndex = currentState.formSections.indexOf(section)
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
        viewModelScope.launch {
            val current = uiState.value as? FormSectionUiState.HasFormSection
                ?: return@launch

            if (current.formSections.hasBlockingFields()) {
                return@launch
            }

            val result =
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

            formResult.value = result

            navigate(result, current)
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
                    _handleSave.tryEmit(true)
                }

                is FormEvent.SaveEvent -> {
                    save()
                }

                else -> Unit
            }
        }
    }

    fun reset() {
        deleteEventOnDiscard()

        _uiState.value = FormSectionUiState.Idle
        formResult.value = null
        formType.value = FormSectionType.NEW_ENROLLMENT
        ruleJob = null
    }

}
