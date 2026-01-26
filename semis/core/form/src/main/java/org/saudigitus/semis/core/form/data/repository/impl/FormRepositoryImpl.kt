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

        if (event != null) {
            val updatedEvent = event.event?.copy(value = buttonModel.code.orEmpty())
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

        attendanceButtonState.value = buttonState(program, attendanceEvents)

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
                val options = getOptionModels(program, it.dataElement?.uid().orEmpty())

                FormFieldState(
                    dataElementUid = it.dataElement?.uid().orEmpty(),
                    label = it.dataElement?.displayFormName()
                        .orEmpty(),
                    valueType = it.dataElement?.valueType() ?: ValueType.TEXT,
                    optionSet = options,
                    mandatory = it.compulsory == true,
                    isAttendanceType = attendance?.status == it.dataElement?.uid()
                )
            }

    }

    override suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String,
        fields: List<FormFieldState>,
    ) = withContext(Dispatchers.IO) {
        val currentFields = fields.toMutableList()
        currentFields.clear()

        for (field in fields) {
            var currentField = field

            val ruleEffects = ruleEngineRepository.evaluateDataEntry(
                ou = orgUnit,
                program = program,
                dataElement = field.dataElementUid,
                event = field.eventUid.orEmpty(),
                value = field.value.orEmpty(),
            )

            for (ruleEffect in ruleEffects) {
                when (ruleEffect.ruleAction.type) {
                    ProgramRuleActionType.HIDEFIELD.name -> {
                        currentField = currentField.copy(rendered = false)
                    }

                    ProgramRuleActionType.SHOWERROR.name -> {
                        currentField = currentField.copy(
                            hasError = true,
                            errorMessage = ruleEffect.ruleAction.content().orEmpty()
                        )
                    }

                    ProgramRuleActionType.ASSIGN.name -> {
                        currentField = currentField.copy(
                            value = ruleEffect.ruleAction.content().orEmpty()
                        )
                    }

                    ProgramRuleActionType.SETMANDATORYFIELD.name -> {
                        currentField = currentField.copy(mandatory = true)
                    }
                }
            }

            currentFields.add(currentField)
        }

        return@withContext currentFields
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
        getSummaries: (List<BottomSheetModel>) -> Unit
    ) = withContext(Dispatchers.IO) {
        attendanceButtonStateFlow.collectLatest { current ->
            val options =
                attendanceOptionRepository.getAttendanceStatusOptions(program)

            val summaries = options.map { option ->
                val count = current.attendanceEvents.count { option.code == it.event?.value }

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
        attendanceButtonState.value = AttendanceButtonState()
    }

    override suspend fun saveAttendance(
        program: String,
        programStage: String,
        attendanceEvents: List<AttendanceEventWithDecorator>
    ) = withContext(Dispatchers.IO) {
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