package org.saudigitus.semis.core.form.data.repository.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Circle
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import timber.log.Timber
import org.saudigitus.semis.core.data.model.OptionModel
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.EventRepository
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.core.data.repository.ProgramStageRepository
import org.saudigitus.semis.core.data.rules.RuleEngineRepository
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonDecorator
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEvent
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel
import org.saudigitus.semis.core.designsystem.theme.light_error
import org.saudigitus.semis.core.designsystem.theme.light_success
import org.saudigitus.semis.core.designsystem.theme.white
import org.saudigitus.semis.core.designsystem.utils.UiDefaults
import org.saudigitus.semis.core.designsystem.utils.UiDefaults.getAttendanceStatusColor
import org.saudigitus.semis.core.form.data.AttendanceTransformation
import org.saudigitus.semis.core.form.data.model.FormFieldData
import org.saudigitus.semis.core.form.data.model.FormFieldState
import org.saudigitus.semis.core.form.data.repository.AttendanceOptionRepository
import org.saudigitus.semis.core.form.data.repository.FormRepository
import org.saudigitus.semis.core.utils.DateHelper
import javax.inject.Inject

private const val RULES_TAG = "FORM_RULES"

class FormRepositoryImpl @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val repository: ProgramStageRepository,
    private val optionRepository: OptionRepository,
    private val ruleEngineRepository: RuleEngineRepository,
    private val eventRepository: EventRepository,
    private val transformations: AttendanceTransformation,
    private val attendanceOptionRepository: AttendanceOptionRepository,
) : FormRepository {

    private val attendanceButtonState = MutableStateFlow(AttendanceButtonState())
    private var loadedAttendanceButtonState = AttendanceButtonState()

    override val attendanceButtonStateFlow: StateFlow<AttendanceButtonState> = attendanceButtonState

    private suspend fun buttonState(
        program: String,
        attendanceEvents: List<AttendanceEventWithDecorator>
    ): AttendanceButtonState {
        val current = attendanceButtonStateFlow.value
        val options = attendanceOptionRepository.getAttendanceStatusOptions(program)

        return current.copy(
            buttons = options,
            attendanceEvents = attendanceEvents
        )
    }

    override fun allowFormEdition(enabled: Boolean) {
        attendanceButtonState.update { it.copy(isEditing = enabled) }
    }

    override suspend fun updateAttendanceEvent(
        eventDate: String?,
        tei: SearchTeiModel?,
        buttonModel: AttendanceButtonModel
    ): AttendanceButtonState {
        val attendanceEvents = attendanceButtonStateFlow.value.attendanceEvents.toMutableList()

        val event = attendanceEvents.find {
            it.event?.tei == tei?.uid()
        }

        if (buttonModel.code == null) {
            event?.event?.event?.let { eventRepository.deleteEvent(it) }
            attendanceEvents.removeIf { it.event?.tei == tei?.uid() }

            return attendanceButtonState.updateAndGet {
                it.copy(attendanceEvents = attendanceEvents)
            }
        }

        if (event != null) {
            val currentEvent = event.event
            val updatedEvent = currentEvent?.copy(
                value = buttonModel.code.orEmpty(),
                reasonDataElement = currentEvent.reasonDataElement,
                reasonOfAbsence = currentEvent.reasonOfAbsence.takeIf { buttonModel.isAbsence },
            )
            val eventWithDecorator = event.copy(
                event = updatedEvent,
                decorator = AttendanceButtonDecorator(
                    buttonType = buttonModel.code.orEmpty(),
                    containerColor = buttonModel.color ?: getAttendanceStatusColor(
                        buttonModel.code.orEmpty()
                    ),
                    contentColor = white
                ),
            )

            val hasBeenRemoved = attendanceEvents.removeIf { it.event?.tei == tei?.uid() }

            if (hasBeenRemoved) {
                attendanceEvents.add(eventWithDecorator)
            }
        } else {
            attendanceEvents.add(
                AttendanceEventWithDecorator(
                    tei = tei,
                    event = AttendanceEvent(
                        tei = tei?.uid().orEmpty(),
                        enrollment = tei?.selectedEnrollment?.uid().orEmpty(),
                        dataElement = buttonModel.dataElement.orEmpty(),
                        value = buttonModel.code.orEmpty(),
                        date = eventDate ?: DateHelper.formatDate(System.currentTimeMillis())
                            .orEmpty()
                    ),
                    decorator = AttendanceButtonDecorator(
                        buttonType = buttonModel.code.orEmpty(),
                        containerColor = buttonModel.color ?: getAttendanceStatusColor(
                            buttonModel.code.orEmpty()
                        ),
                        contentColor = white
                    ),
                    icon = buttonModel.icon
                        ?: UiDefaults.dynamicIcons(buttonModel.iconName.orEmpty()),
                    iconName = buttonModel.iconName.orEmpty(),
                    iconColor = Color.White,
                )
            )
        }

        return attendanceButtonState.updateAndGet {
            it.copy(attendanceEvents = attendanceEvents)
        }
    }

    override suspend fun deleteAttendance(
        absencesOnly: Boolean,
    ): AttendanceButtonState = withContext(Dispatchers.IO) {
        val current = attendanceButtonStateFlow.value
        val removable = removableAttendanceEvents(
            events = current.attendanceEvents,
            configuredStatusCodes = current.buttons.mapNotNull { it.code },
            absencesOnly = absencesOnly,
        )

        removable.forEach { event ->
            event.event?.event?.let { uid -> eventRepository.deleteEvent(uid) }
        }

        val remaining = current.attendanceEvents - removable.toSet()
        loadedAttendanceButtonState = loadedAttendanceButtonState.copy(
            attendanceEvents = remaining,
        )

        return@withContext attendanceButtonState.updateAndGet {
            it.copy(attendanceEvents = remaining)
        }
    }

    override fun updateAttendanceReason(
        tei: String,
        dataElement: String,
        value: String
    ): AttendanceButtonState? {
        val attendanceEvents = attendanceButtonStateFlow.value.attendanceEvents.toMutableList()

        val event = attendanceEvents.find {
            it.event?.tei == tei
        }

        if (event == null) return null

        val updatedEvent = event.event?.copy(
            reasonDataElement = dataElement,
            reasonOfAbsence = value
        )
        val eventWithDecorator = event.copy(
            event = updatedEvent,
        )

        val hasBeenRemoved = attendanceEvents.removeIf { it.event?.tei == tei }

        if (hasBeenRemoved) {
            attendanceEvents.add(eventWithDecorator)
        }

        return attendanceButtonState.updateAndGet {
            it.copy(attendanceEvents = attendanceEvents)
        }
    }

    override suspend fun loadAttendanceEvents(
        teiUids: List<String>,
        program: String,
        programStage: String,
        dataElement: String,
        reasonDataElement: String,
        eventDate: String?
    ) = withContext(Dispatchers.IO) {
        val config = appConfigRepository.getAppConfig(program)
        val attendanceConfig = config?.attendance

        val attendanceEvents = getAttendanceEvent(
            teiUids = teiUids,
            program = program,
            programStage = programStage,
            dataElement = attendanceConfig?.status.orEmpty(),
            reasonDataElement = attendanceConfig?.absenceReason.orEmpty(),
            eventDate = eventDate ?: DateHelper.formatDate(System.currentTimeMillis())
                .orEmpty()
        )

        val loadedState = buttonState(program, attendanceEvents)
        loadedAttendanceButtonState = loadedState
        attendanceButtonState.value = loadedState

        return@withContext attendanceButtonStateFlow.value
    }


    private suspend fun getOptionModels(
        program: String,
        dl: String,
    ): List<OptionModel> {
        return optionRepository.getOptions(program = program, dataElement = dl)
            .map {
                OptionModel(
                    uid = it.uid(),
                    code = it.code(),
                    displayName = it.displayName(),
                    sortOrder = it.sortOrder(),
                )
            }
    }

    override suspend fun getFormFields(
        program: String,
        stage: String,
        dl: String?
    ) = withContext(Dispatchers.IO) {
        val attendance = appConfigRepository.getAppConfig(program)?.attendance

        repository.getProgramStageDataElements(stage, dl)
            .map {
                val options = if (it.dataElement?.optionSetUid().isNullOrBlank()) {
                    emptyList()
                } else {
                    getOptionModels(program, it.dataElement?.uid().orEmpty())
                }

                FormFieldState(
                    dataElementUid = it.dataElement?.uid().orEmpty(),
                    label = it.dataElement?.displayFormName()
                        ?: it.dataElement?.displayName().orEmpty(),
                    valueType = it.dataElement?.valueType() ?: ValueType.TEXT,
                    optionSet = options,
                    mandatory = it.compulsory == true,
                    isAttendanceType = attendance?.status == it.dataElement?.uid(),
                    isAttendanceReason = attendance?.absenceReason == it.dataElement?.uid(),
                )
            }

    }

    /**
     * Applies the rules to the shape of the form, which is what every person shares.
     *
     * Only what does not depend on whose value it is can be decided here: whether a field is
     * shown at all, whether it is required, and what a rule assigns when it does not read a
     * value. Anything that judges a value belongs to the person who holds it and is applied on
     * their record instead.
     */
    override suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String,
        fields: List<FormFieldState>,
    ) = withContext(Dispatchers.IO) {
        val effects = runCatching {
            ruleEngineRepository.evaluateUnsavedEvent(
                ou = orgUnit,
                program = program,
                programStage = programStage,
                dataValues = emptyMap(),
            )
        }.getOrElse { error ->
            Timber.tag(RULES_TAG).e(error)
            return@withContext fields
        }

        val hidden = mutableSetOf<String>()
        val mandatory = mutableSetOf<String>()
        val assigned = mutableMapOf<String, String?>()

        effects.forEach { effect ->
            val field = effect.ruleAction.field() ?: return@forEach

            when (effect.ruleAction.type) {
                ProgramRuleActionType.HIDEFIELD.name -> hidden += field
                ProgramRuleActionType.SETMANDATORYFIELD.name -> mandatory += field
                ProgramRuleActionType.ASSIGN.name -> assigned[field] = effect.data
                else -> Unit
            }
        }

        // Every evaluation starts from what the configuration alone says, so an effect whose
        // condition stopped holding disappears instead of staying behind.
        return@withContext fields.map { field ->
            field.copy(
                rendered = field.dataElementUid !in hidden,
                mandatory = field.mandatory || field.dataElementUid in mandatory,
                value = if (field.dataElementUid in assigned) {
                    assigned[field.dataElementUid]
                } else {
                    field.value
                },
            )
        }
    }

    override suspend fun applyProgramRulesToRecords(
        orgUnit: String,
        program: String,
        programStage: String,
        fieldsData: List<FormFieldData>,
    ): List<FormFieldData> = withContext(Dispatchers.IO) {
        if (fieldsData.isEmpty()) return@withContext fieldsData

        val byPerson = fieldsData.groupBy { it.tei }
        val outcomes = byPerson.mapValues { (_, records) ->
            val values = records.mapNotNull { record ->
                record.value?.takeIf { it.isNotBlank() }?.let { record.dataElement to it }
            }.toMap()

            if (values.isEmpty()) {
                emptyMap()
            } else {
                runCatching {
                    ruleEngineRepository.evaluateUnsavedEvent(
                        ou = orgUnit,
                        program = program,
                        programStage = programStage,
                        dataValues = values,
                    )
                }.getOrElse { error ->
                    Timber.tag(RULES_TAG).e(error)
                    emptyList()
                }.mapNotNull { effect ->
                    val field = effect.ruleAction.field() ?: return@mapNotNull null
                    val message = listOfNotNull(effect.ruleAction.content(), effect.data)
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .joinToString(" ")

                    when (effect.ruleAction.type) {
                        ProgramRuleActionType.SHOWERROR.name -> field to (true to message)
                        ProgramRuleActionType.SHOWWARNING.name -> field to (false to message)
                        else -> null
                    }
                }.toMap()
            }
        }

        return@withContext fieldsData.map { record ->
            val outcome = outcomes[record.tei]?.get(record.dataElement)
            val isError = outcome?.first == true
            val isWarning = outcome != null && !isError

            record.copy(
                hasError = isError,
                errorMessage = outcome?.second?.takeIf { isError },
                hasWarning = isWarning,
                warningMessage = outcome?.second?.takeIf { isWarning },
            )
        }
    }

    override fun individualFormSummary(
        formFieldsData: List<FormFieldData>,
    ): List<BottomSheetModel> {
        val countUpdatedFields = formFieldsData.count { it.isUpdated }
        val countNotUpdatedFields = formFieldsData.count { !it.isUpdated }

        return listOf(
            BottomSheetModel(
                icon = Icons.Filled.Circle,
                value = "$countUpdatedFields",
                color = light_success
            ),
            BottomSheetModel(
                icon = Icons.Default.Block,
                value = "$countNotUpdatedFields",
                color = light_error
            )
        )

    }

    override suspend fun attendanceSummary(
        program: String,
        totalLearners: Int,
        getSummaries: (List<BottomSheetModel>) -> Unit
    ) = withContext(Dispatchers.IO) {
        attendanceButtonStateFlow.collectLatest { current ->
            val options =
                attendanceOptionRepository.getAttendanceStatusOptions(program)

            val nonPresentCount = options
                .filter { it.code != null }
                .sumOf { option ->
                    current.attendanceEvents.count { option.code == it.event?.value }
                }

            val summaries = options.map { option ->
                val count = if (option.code == null) {
                    (totalLearners - nonPresentCount).coerceAtLeast(0)
                } else {
                    current.attendanceEvents.count { option.code == it.event?.value }
                }

                BottomSheetModel(
                    icon = option.icon,
                    iconName = option.iconName,
                    label = option.name,
                    value = "$count",
                    color = option.color
                )
            }

            getSummaries(summaries)
        }
    }

    override fun reset() {
        attendanceButtonState.value = resetAttendanceFormState(
            current = attendanceButtonState.value,
            loaded = loadedAttendanceButtonState,
        )
    }

    override suspend fun saveAttendance(
        program: String,
        programStage: String,
        attendanceEvents: List<AttendanceEventWithDecorator>
    ) = withContext(Dispatchers.IO) {
        // A learner dropped from the form no longer holds a status, so the record loaded
        // for them has to go with it rather than linger as a stale event.
        orphanedAttendanceEvents(
            loaded = loadedAttendanceButtonState.attendanceEvents,
            current = attendanceEvents,
        ).forEach { orphan ->
            orphan.event?.event?.let { uid -> eventRepository.deleteEvent(uid) }
        }

        attendanceEvents.forEach { attendanceEvent ->
            eventRepository.saveEvent(
                event = attendanceEvent.event?.event,
                orgUnit = attendanceEvent.tei!!.tei.organisationUnit().orEmpty(),
                tei = attendanceEvent.tei!!,
                program = program,
                programStage = programStage,
                data = mapOf(
                    "dataElement" to Pair(
                        attendanceEvent.event?.dataElement.orEmpty(),
                        attendanceEvent.event?.value.orEmpty()
                    ),
                    "reasonDataElement" to Pair(
                        attendanceEvent.event?.reasonDataElement.orEmpty(),
                        attendanceEvent.event?.reasonOfAbsence.orEmpty()
                    ),
                ),
                eventDate = attendanceEvent.event?.date
            )
        }
    }

    override suspend fun saveAttendanceStatus(
        event: String?,
        orgUnit: String,
        program: String,
        programStage: String,
        data: List<Pair<String, String?>>,
        eventDate: String
    ) = withContext(Dispatchers.IO) {
        eventRepository.saveEvent(
            event = event,
            orgUnit = orgUnit,
            program = program,
            programStage = programStage,
            data = data,
            eventDate = eventDate
        )
    }

    override suspend fun getAttendanceEvent(
        teiUids: List<String>,
        program: String,
        programStage: String,
        dataElement: String,
        reasonDataElement: String,
        eventDate: String?
    ) = withContext(Dispatchers.IO) {
        val config = appConfigRepository.getAppConfig(program)

        eventRepository.getEvents(
            teiUids = teiUids,
            program = program,
            programStage = programStage,
            eventDate = eventDate,
        ).map {
            transformations.teiEventTransform(
                eventUid = it.uid(),
                program = program,
                attendanceDataElement = dataElement,
                reasonDataElement = reasonDataElement,
                config = config?.attendance,
            )
        }
    }

}
