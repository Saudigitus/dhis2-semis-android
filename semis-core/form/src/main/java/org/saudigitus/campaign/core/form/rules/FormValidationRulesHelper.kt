package org.saudigitus.campaign.core.form.rules

import com.formrules.FormRules
import com.formrules.engine.DataContext
import com.formrules.engine.EventData
import com.formrules.engine.FormEvaluation
import com.formrules.model.ActionType
import com.formrules.model.RuleType
import com.formrules.model.asStringOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.event
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FormValidationType
import org.saudigitus.campaign.core.data.repository.DatastoreRepository
import org.saudigitus.campaign.core.data.utils.toJson
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.utils.DateHelper

class FormValidationRulesHelper(
    private val d2: D2,
    private val datastoreRepository: DatastoreRepository
) : FormValidationRulesRepository {

    override suspend fun evaluateEnrollment(
        program: String,
        tei: String
    ) = withContext(Dispatchers.IO) {
        val formValidationJson = datastoreRepository.getFormValidations(program)
            .toJson() ?: return@withContext null

        try {
            val formValidationRules = FormRules.fromJson(formValidationJson)
            val context = DataContext(
                attributes = d2.trackedEntityModule().trackedEntityAttributeValues()
                    .byTrackedEntityInstance().eq(tei)
                    .blockingGet()
                    .associate { it.trackedEntityAttribute().orEmpty() to it.value() }
            )

            formValidationRules.evaluate(context, RuleType.ENROLLMENT)
        } catch(_: Exception) {
            null
        }
    }

    override suspend fun evaluateEvent(
        program: String,
        stage: String,
        tei: String,
        event: String?,
        currentEvent: EventData?
    ): FormEvaluation? = withContext(Dispatchers.IO) {
        val formValidations = datastoreRepository.getFormValidations(program)
            .find { it.type == FormValidationType.EVENT.name && it.programStage == stage }
            ?: return@withContext null

        val formValidationJson = listOf(formValidations)
            .toJson() ?: return@withContext null

        try {
            val formValidationRules = FormRules.fromJson(formValidationJson)
            val ruleRequirements = formValidationRules.requirements()
            val incomingDataStage = ruleRequirements.programStageOf("entrada").orEmpty()
            val outgoingDataStage = ruleRequirements.programStageOf("saida").orEmpty()

            val incomingEvents = getEventData(program, incomingDataStage, tei)
            val outgoingEvents = getEventData(program, outgoingDataStage, tei)
            val events = listOf(incomingEvents, outgoingEvents).flatten()

            val context = DataContext(
                events = events,
                currentEvent = currentEvent ?: d2.event(event.orEmpty()).let {
                    EventData(
                        eventId = it?.uid().orEmpty(),
                        programStage = stage,
                        status = it?.status()?.name.orEmpty(),
                        occurredAt = DateHelper.formatDate(
                            it?.eventDate()?.time ?: System.currentTimeMillis()
                        ).orEmpty(),
                    )
                }
            )

            formValidationRules.evaluate(context, RuleType.EVENT)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun applyValidations(
        program: String,
        programStage: String?,
        event: String?,
        enrollment: String?,
        formSections: Map<String, FormSectionModel>
    ): MutableMap<String, FormSectionModel> = withContext(Dispatchers.IO) {
        val updatedSections = formSections.toMutableMap()
        val updatedFields = updatedSections.values
            .flatMap { section -> section.formFields }
            .associateBy { it.uid }
            .toMutableMap()

        val formSections = updatedSections.values

        val valueStore = formSections
            .flatMap { section -> section.formFields }
            .associate { field ->
                field.uid to field.value.orEmpty()
            }

        val evaluations = if (!event.isNullOrEmpty() && !programStage.isNullOrEmpty()) {
            val currentEvent = EventData(
                eventId = event,
                programStage = programStage,
                status = EventStatus.ACTIVE.name,
                occurredAt = DateHelper.formatDate(
                    System.currentTimeMillis()
                ).orEmpty(),
                values = valueStore
            )

            evaluateEvent(
                program = program,
                stage = programStage,
                tei = enrollment.orEmpty(),
                event = event,
                currentEvent = currentEvent,
            )
        } else {
            evaluateEnrollment(
                program = program,
                tei = enrollment.orEmpty()
            )
        }


        if (evaluations == null) return@withContext updatedSections

        for (evaluationResult in evaluations.results) {
            val action = evaluationResult.action

            when (action.type) {
                ActionType.ASSIGN_VALUE -> {
                    val fieldUid = action.target
                    val field = updatedFields[fieldUid] ?: continue

                    updatedFields[fieldUid] = field.copy(
                        value = action.value?.asStringOrNull(),
                        enabled = field.enabledOnAssign ?: false,
                    )
                }

                ActionType.SHOW_ERROR, ActionType.VALIDATE_FIELD -> {
                    val fieldUid = action.target
                    val field = updatedFields[fieldUid] ?: continue

                    if (evaluationResult.fired) {
                        updatedFields[fieldUid] = field.copy(
                            hasError = true,
                            errorMessage = action.message
                        )
                    }
                }

                ActionType.SHOW_WARNING -> {
                    val fieldUid = action.target
                    val field = updatedFields[fieldUid] ?: continue
                    updatedFields[fieldUid] = field.copy(
                        hasWarning = true,
                        warningMessage = action.message
                    )
                }

                ActionType.HIDE_FIELD -> {
                    val fieldUid = action.target
                    val field = updatedFields[fieldUid] ?: continue

                    if (field.mandatory != true) {
                        updatedFields[fieldUid] = field.copy(
                            rendered = false,
                            value = null
                        )
                    }
                }

                else -> Unit
            }
        }

        updatedSections.replaceAll { _, section ->
            val updatedSectionFields = section.formFields.map { sectionField ->
                updatedFields[sectionField.uid] ?: sectionField
            }

            section.copy(
                rendered = section.rendered &&
                    updatedSectionFields.any { it.rendered == true },
                formFields = updatedSectionFields
            )
        }

        return@withContext updatedSections
    }


    private fun getEventData(program: String, stage: String, enrollment: String): List<EventData> {
        return d2.eventModule().events()
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(stage)
            .byEnrollmentUid().eq(enrollment)
            .withTrackedEntityDataValues()
            .blockingGet().map {
                EventData(
                    eventId = it.uid(),
                    programStage = stage,
                    status = it.status()?.name.orEmpty(),
                    occurredAt = DateHelper.formatDate(it.eventDate()?.time!!).orEmpty(),
                    values = it.trackedEntityDataValues()
                        ?.associate { dataValue ->
                            dataValue.dataElement().orEmpty() to dataValue.value()
                        } ?: emptyMap(),
                )
            }
    }
}