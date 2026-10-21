package org.saudigitus.campaign.core.form.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.saudigitus.campaign.core.data.models.FormFieldEntity
import org.saudigitus.campaign.core.data.models.OptionModel
import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.data.models.OuHideStrategy
import org.saudigitus.campaign.core.data.models.TrackedEntityAttributeModel
import org.saudigitus.campaign.core.data.repository.EnrollmentRepository
import org.saudigitus.campaign.core.data.repository.OptionRepository
import org.saudigitus.campaign.core.data.repository.ProgramRepository
import org.saudigitus.campaign.core.data.rules.RuleEngineRepository
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.data.models.FormResult
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.form.rules.applyRuleEffects
import org.saudigitus.campaign.core.form.ui.state.FormSectionType
import org.saudigitus.campaign.core.form.utils.toEntities
import timber.log.Timber
import org.saudigitus.campaign.core.utils.Constants
import java.sql.Date
import java.util.UUID
import javax.inject.Inject

/**
 * SEMIS adapter for the original Campaign form UI.  It retains the Campaign
 * form sections and field components while using only the tracker enrollment
 * data needed by SEMIS.
 */
class SemisEnrollmentFormRepository @Inject constructor(
    private val d2: D2,
    private val optionRepository: OptionRepository,
    private val programRepository: ProgramRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val ruleEngineRepository: RuleEngineRepository,
    private val resourceManager: ResourceManager,
) : FormRepository {

    private companion object {
        /**
         * How many reserved values may be discarded before giving up on finding a usable one.
         * Each attempt costs a value from the reserve, so the search stays deliberately short.
         */
        const val MAX_RESERVED_VALUE_ATTEMPTS = 5
    }
    override suspend fun save(
        formType: FormSectionType,
        orgUnit: String,
        program: String,
        date: String,
        tei: String?,
        enrollment: String?,
        formSections: List<FormSectionModel>,
    ): FormResult = withContext(Dispatchers.IO) {
        val fields = formSections
            .filter { it.rendered }
            .flatMap { it.formFields }
            .filter { it.rendered == true }
            .toEntities()

        when (formType) {
            FormSectionType.NEW_ENROLLMENT -> {
                val (enrollmentUid, teiUid) = enrollmentRepository.create(orgUnit, program, date, fields)
                FormResult(enrollment = enrollmentUid, tei = teiUid)
            }

            FormSectionType.EDIT_ENROLLMENT -> {
                val (teiUid, enrollmentUid) = enrollmentRepository.update(
                    enrollment = enrollment.orEmpty(),
                    tei = tei.orEmpty(),
                    date = date,
                    fields = fields,
                )
                FormResult(enrollment = enrollmentUid, tei = teiUid)
            }

            FormSectionType.NEW_EVENT_WITH_REGISTRATION,
            FormSectionType.NEW_EVENT_WITHOUT_REGISTRATION -> {
                val programStage = formSections.firstNotNullOfOrNull { it.programStage }
                    ?: throw IllegalStateException("Program stage not found in event form")
                val existingEvent = enrollment?.takeIf(String::isNotBlank)?.let { enrollmentUid ->
                    d2.eventModule().events()
                        .byEnrollmentUid().eq(enrollmentUid)
                        .byProgramUid().eq(program)
                        .byProgramStageUid().eq(programStage)
                        .byDeleted().isFalse
                        .one()
                        .blockingGet()
                        ?.uid()
                }
                val eventUid = formSections.firstNotNullOfOrNull { it.eventUid }
                    ?: existingEvent
                    ?: d2.eventModule().events().blockingAdd(
                        EventCreateProjection.builder()
                            .organisationUnit(orgUnit)
                            .program(program)
                            .programStage(programStage)
                            .attributeOptionCombo(defaultAttributeOptionCombo())
                            .apply {
                                if (!enrollment.isNullOrBlank()) enrollment(enrollment)
                            }
                            .build(),
                    )

                fields.forEach { field ->
                    d2.trackedEntityModule().trackedEntityDataValues()
                        .value(eventUid, field.uid)
                        .blockingSet(field.value)
                }
                d2.eventModule().events().uid(eventUid).apply {
                    setEventDate(Date.valueOf(date))
                    setStatus(EventStatus.COMPLETED)
                }
                FormResult(
                    enrollment = enrollment,
                    tei = tei,
                    isEventSaved = eventUid.isNotBlank(),
                )
            }
        }
    }

    override suspend fun saveEnrollment(
        orgUnit: String,
        program: String,
        date: String,
        attributes: List<FormSectionModel>,
        stages: Map<String, List<FormSectionModel>>,
        backgroundStages: List<String>,
    ): FormResult = withContext(Dispatchers.IO) {
        val (enrollmentUid, teiUid) = enrollmentRepository.create(
            orgUnit = orgUnit,
            program = program,
            date = date,
            fields = attributes.capturedFields(),
        )

        try {
            stages.forEach { (programStage, sections) ->
                createStageEvent(
                    orgUnit = orgUnit,
                    program = program,
                    programStage = programStage,
                    enrollment = enrollmentUid,
                    date = date,
                    fields = sections.capturedFields(),
                )
            }

            backgroundStages.forEach { programStage ->
                createStageEvent(
                    orgUnit = orgUnit,
                    program = program,
                    programStage = programStage,
                    enrollment = enrollmentUid,
                    date = date,
                    fields = emptyList(),
                )
            }
        } catch (error: Throwable) {
            discardEnrollment(teiUid, enrollmentUid)
            throw error
        }

        FormResult(enrollment = enrollmentUid, tei = teiUid)
    }

    /**
     * Creates the event for [programStage] and fills in whatever was captured for it.
     *
     * The completed status is deliberate and configured: an event produced by an enrollment is not
     * left open for later editing.
     */
    private fun createStageEvent(
        orgUnit: String,
        program: String,
        programStage: String,
        enrollment: String,
        date: String,
        fields: List<FormFieldEntity>,
    ) {
        val eventUid = d2.eventModule().events().blockingAdd(
            EventCreateProjection.builder()
                .organisationUnit(orgUnit)
                .program(program)
                .programStage(programStage)
                .enrollment(enrollment)
                .attributeOptionCombo(defaultAttributeOptionCombo())
                .build(),
        )

        fields.forEach { field ->
            d2.trackedEntityModule().trackedEntityDataValues()
                .value(eventUid, field.uid)
                .blockingSet(field.value)
        }

        d2.eventModule().events().uid(eventUid).apply {
            setEventDate(Date.valueOf(date))
            setStatus(EventStatus.COMPLETED)
        }
    }

    /**
     * Removes an enrollment that could not be written in full.
     *
     * A learner created during this transaction has never reached the server, so the whole record is
     * dropped. Should it somehow already have been uploaded, only the enrollment is removed, because
     * deleting the learner would then take away someone the server still knows about.
     */
    private fun discardEnrollment(tei: String, enrollment: String) {
        runCatching {
            val teiRepository = d2.trackedEntityModule().trackedEntityInstances().uid(tei)

            if (teiRepository.blockingGet()?.syncState() == State.TO_POST) {
                teiRepository.blockingDelete()
            } else {
                d2.enrollmentModule().enrollments().uid(enrollment).blockingDelete()
            }
        }
    }

    /** The fields a step actually presented, in the shape the data layer stores. */
    private fun List<FormSectionModel>.capturedFields(): List<FormFieldEntity> = this
        .filter { it.rendered }
        .flatMap { it.formFields }
        .filter { it.rendered == true }
        .toEntities()

    override suspend fun getFormSections(
        orgUnit: String,
        program: String,
        tei: String?,
        enrollment: String?,
        vararg programStages: String?,
    ): List<FormSectionModel> = withContext(Dispatchers.IO) {
        programStages.mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
            .distinct()
            .flatMap { stage -> eventSections(stage) }
    }

    private suspend fun eventSections(stage: String): List<FormSectionModel> {
        val sections = d2.programModule().programStageSections()
            .byProgramStageUid().eq(stage)
            .withDataElements()
            .blockingGet()

        if (sections.isEmpty()) {
            val stageMetadata = d2.programModule().programStages().uid(stage).blockingGet()
            return listOf(
                FormSectionModel(
                    uid = UUID.randomUUID().toString(),
                    programStage = stage,
                    code = stageMetadata?.code(),
                    name = stageMetadata?.displayName() ?: "Event",
                    formFields = eventFields(stage, null),
                ),
            )
        }

        return sections.sortedBy { it.sortOrder() }.map { section ->
            FormSectionModel(
                uid = section.uid(),
                programStage = stage,
                code = section.code(),
                name = section.displayName(),
                description = section.displayDescription(),
                sortOrder = section.sortOrder(),
                formFields = eventFields(
                    stage = stage,
                    sectionDataElements = section.dataElements().orEmpty().map { it.uid() },
                ),
            )
        }
    }

    private fun defaultAttributeOptionCombo(): String? =
        d2.categoryModule().categoryOptionCombos()
            .byDisplayName().eq(Constants.DEFAULT)
            .one()
            .blockingGet()
            ?.uid()

    /**
     * The fields a step presents, in the order the configuration puts them in.
     *
     * A section states the order of the data elements it holds, and that is the order the form
     * follows. The position the program stage gives the same data element is a different number
     * entirely: it is assigned across the whole stage, it repeats between sections, and reading it
     * instead leaves a section shuffled and its ties settled by nothing in particular. Only a stage
     * that groups nothing has no section order to follow, and there the stage order is the order.
     *
     * [sectionDataElements] is the section's own list, already in that order, and null for a stage
     * without sections.
     */
    private suspend fun eventFields(
        stage: String,
        sectionDataElements: List<String>?,
    ): List<FormFieldModel> {
        val positionInSection = sectionDataElements
            ?.withIndex()
            ?.associate { (position, dataElement) -> dataElement to position }

        return d2.programModule().programStageDataElements()
            .byProgramStage().eq(stage)
            .blockingGet()
            .filter { positionInSection == null || it.dataElement()?.uid() in positionInSection }
            .sortedBy { stageDataElement ->
                positionInSection?.get(stageDataElement.dataElement()?.uid())
                    ?: stageDataElement.sortOrder()
                    ?: Int.MAX_VALUE
            }
            .mapNotNull { stageDataElement ->
                val dataElementUid = stageDataElement.dataElement()?.uid() ?: return@mapNotNull null
                val dataElement = d2.dataElementModule().dataElements().uid(dataElementUid).blockingGet()
                    ?: return@mapNotNull null
                val optionSetUid = dataElement.optionSet()?.uid()
                FormFieldModel(
                    uid = dataElementUid,
                    label = dataElement.displayFormName().orEmpty(),
                    valueType = dataElement.valueType(),
                    mandatory = stageDataElement.compulsory() == true,
                    baseMandatory = stageDataElement.compulsory() == true,
                    optionSet = optionSetUid?.let { uid ->
                        optionRepository.getOptions(uid).map { option ->
                            OptionModel(
                                uid = option.uid(),
                                code = option.code(),
                                displayName = option.displayName(),
                                sortOrder = option.sortOrder(),
                            )
                        }
                    }.orEmpty(),
                )
            }
    }

    override suspend fun getFormSections(
        orgUnit: String,
        program: String,
        tei: String?,
    ): List<FormSectionModel> = withContext(Dispatchers.IO) {
        val storedValues = attributeValues(tei)

        programRepository.getTrackedEntityAttributeWithSection(program).map { section ->
            FormSectionModel(
                uid = section.uid,
                code = section.code,
                name = section.displayName,
                description = section.description,
                sortOrder = section.sortOrder,
                formFields = section.attributes.map { attribute ->
                    val optionSetUid = attribute.optionSetUid
                    val storedValue = storedValues[attribute.uid]
                    val reserved = if (attribute.generated && storedValue == null) {
                        reserveGeneratedValue(attribute, orgUnit)
                    } else {
                        null
                    }

                    FormFieldModel(
                        uid = attribute.uid,
                        label = attribute.displayFormName.orEmpty(),
                        valueType = attribute.valueType,
                        value = storedValue ?: reserved?.value,
                        // A generated value belongs to the server, so it is shown but not typed over.
                        enabled = !attribute.generated,
                        generated = attribute.generated,
                        hasWarning = reserved?.warning != null,
                        warningMessage = reserved?.warning,
                        mandatory = attribute.mandatory,
                        baseMandatory = attribute.mandatory,
                        optionSet = if (optionSetUid != null) {
                            optionRepository.getOptions(optionSetUid).map {
                                OptionModel(
                                    uid = it.uid(),
                                    code = it.code(),
                                    displayName = it.displayName(),
                                    sortOrder = it.sortOrder(),
                                )
                            }
                        } else emptyList(),
                    )
                },
            )
        }
    }

    /**
     * Value already stored for each attribute of [tei], keyed by attribute uid.
     *
     * Two things depend on this. An enrollment being edited shows what was captured before, and an
     * attribute that already holds a generated value stops consuming a fresh one from the reserve
     * every time the form is reopened, which would both exhaust the reserve and change an
     * identifier that other records already refer to.
     */
    private fun attributeValues(tei: String?): Map<String, String> {
        if (tei.isNullOrBlank()) return emptyMap()

        return d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityInstance().eq(tei)
            .blockingGet()
            .mapNotNull { attributeValue ->
                val uid = attributeValue.trackedEntityAttribute() ?: return@mapNotNull null
                val value = attributeValue.value()?.takeIf(String::isNotBlank)
                    ?: return@mapNotNull null
                uid to value
            }
            .toMap()
    }

    /**
     * Takes the next value the server reserved for [attribute] at [orgUnit].
     *
     * The pattern behind a generated attribute is deliberately never interpreted here. Sequential
     * values are allocated by the server so that two devices cannot mint the same identifier, and
     * the SDK owns the reserve, its refill and the patterns whose result varies per organisation
     * unit. Delegating to it is what keeps this working for whatever pattern a deployment
     * configures, instead of only for the ones seen so far.
     *
     * Every call consumes a value, hence it is only reached for an attribute with none stored yet.
     */
    private fun reserveGeneratedValue(
        attribute: TrackedEntityAttributeModel,
        orgUnit: String,
    ): ReservedValue {
        val value = try {
            if (orgUnit.isBlank()) null else nextUsableReservedValue(attribute, orgUnit)
        } catch (_: Exception) {
            // The reserve is refilled while syncing, so running dry offline is expected rather
            // than exceptional, and it must not take the whole form down with it.
            null
        }

        return when (value) {
            null -> ReservedValue(
                value = null,
                warning = resourceManager.getString(R.string.reserved_value_unavailable),
            )

            else -> ReservedValue(value = value, warning = null)
        }
    }

    /**
     * Next reserved value that is valid for the value type of [attribute], or null when none is.
     *
     * A number cannot carry a leading zero, so such a value is dropped instead of being stored as
     * something the server would later reject. The retries are capped because each one costs a
     * value from the reserve, and a pattern that only ever yields leading zeros is a configuration
     * problem that draining the reserve would not solve.
     */
    private fun nextUsableReservedValue(
        attribute: TrackedEntityAttributeModel,
        orgUnit: String,
    ): String? {
        val reservedValueManager = d2.trackedEntityModule().reservedValueManager()
        var value = reservedValueManager.blockingGetValue(attribute.uid, orgUnit)

        if (attribute.valueType != ValueType.NUMBER) return value

        var attempts = 1
        while (value.startsWith("0") && attempts < MAX_RESERVED_VALUE_ATTEMPTS) {
            value = reservedValueManager.blockingGetValue(attribute.uid, orgUnit)
            attempts++
        }

        return value.takeIf { !it.startsWith("0") }
    }

    /** Outcome of reserving a value: the value itself, or the warning to show when there is none. */
    private data class ReservedValue(val value: String?, val warning: String?)

    /**
     * Evaluates the program rules of [program] against the values as typed and applies their
     * effects to [formSections].
     *
     * Every evaluation starts from base state: what the configuration alone says about each
     * field, with the outcome of the previous evaluation cleared, so an effect whose condition
     * stopped holding disappears on its own. The one non rule flag a field can carry, the phone
     * number validation, is recomputed rather than kept, because keeping it would also keep a
     * stale rule error, the two being indistinguishable on the model.
     *
     * A step whose sections belong to a stage evaluates as an event, unsaved or not; the
     * attribute step evaluates as an enrollment. A failure inside the engine never reaches the
     * form: the rules then simply do not constrain it, which is also what a program with no
     * rules looks like.
     */
    override suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String?,
        event: String?,
        enrollment: String?,
        formSections: List<FormSectionModel>,
    ): List<FormSectionModel> {
        val baseSections = formSections.map { section ->
            section.copy(
                rendered = true,
                formFields = section.formFields.map { field -> field.withBaseFlags() },
            )
        }

        val effects = runCatching {
            if (programStage.isNullOrBlank()) {
                ruleEngineRepository.evaluate(
                    ou = orgUnit,
                    program = program,
                    enrollment = enrollment.orEmpty(),
                    attributeValues = baseSections.typedValues(),
                )
            } else if (event.isNullOrBlank()) {
                ruleEngineRepository.evaluateUnsavedEvent(
                    ou = orgUnit,
                    program = program,
                    programStage = programStage,
                    dataValues = baseSections.typedValues(),
                )
            } else {
                ruleEngineRepository.evaluate(
                    ou = orgUnit,
                    program = program,
                    event = event,
                    enrollment = enrollment,
                    dataValues = baseSections.typedValues(),
                )
            }
        }.getOrElse { error ->
            Timber.tag("ENROLLMENT_RULES").e(error)
            return baseSections
        }

        return baseSections.applyRuleEffects(effects)
    }

    /** The values as typed, keyed by field, which is what an in memory evaluation runs against. */
    private fun List<FormSectionModel>.typedValues(): Map<String, String> = this
        .flatMap { section -> section.formFields }
        .mapNotNull { field ->
            field.value?.takeIf { it.isNotBlank() }?.let { value -> field.uid to value }
        }
        .toMap()

    /** The field as the configuration alone describes it, with no rule outcome left in. */
    private fun FormFieldModel.withBaseFlags(): FormFieldModel = copy(
        rendered = true,
        mandatory = baseMandatory,
        enabled = !generated,
        hasError = false,
        errorMessage = null,
        hasWarning = false,
        warningMessage = null,
    )

    override suspend fun searchOrgUnits(
        query: String?,
        orgUnit: String,
        ouHideStrategy: OuHideStrategy?,
    ): List<OrgUnit> = emptyList()

    override suspend fun getDefaultProgramStage(program: String): String? = withContext(Dispatchers.IO) {
        d2.programModule().programStages()
            .byProgramUid().eq(program)
            .one()
            .blockingGet()
            ?.uid()
    }

    override suspend fun programStagesToHide(): List<String> = emptyList()

    override suspend fun getOrgUnitName(ou: String): String? = withContext(Dispatchers.IO) {
        d2.organisationUnitModule().organisationUnits()
            .uid(ou)
            .blockingGet()
            ?.displayName()
    }

    override suspend fun deleteEvent(event: String) = withContext(Dispatchers.IO) {
        d2.eventModule().events().uid(event).blockingDeleteIfExist()
    }
}
