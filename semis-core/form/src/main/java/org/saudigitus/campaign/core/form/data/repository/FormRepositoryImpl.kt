package org.saudigitus.campaign.core.form.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataElement
import org.dhis2.commons.bindings.formatData
import org.dhis2.commons.bindings.organisationUnit
import org.dhis2.commons.orgunitselector.OUTreeModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.saudigitus.campaign.core.data.models.OptionModel
import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.data.models.OuHideStrategy
import org.saudigitus.campaign.core.data.models.datastore.appconfig.AppConfigItem
import org.saudigitus.campaign.core.data.models.datastore.global.GlobalConfigItem
import org.saudigitus.campaign.core.data.repository.DatastoreRepository
import org.saudigitus.campaign.core.data.repository.EnrollmentRepository
import org.saudigitus.campaign.core.data.repository.EventRepository
import org.saudigitus.campaign.core.data.repository.GoalRepository
import org.saudigitus.campaign.core.data.repository.OptionRepository
import org.saudigitus.campaign.core.data.repository.ProgramRepository
import org.saudigitus.campaign.core.data.repository.ProgramStageRepository
import org.saudigitus.campaign.core.data.repository.TeiRepository
import org.saudigitus.campaign.core.data.rules.RuleEngineRepository
import org.saudigitus.campaign.core.data.utils.byAttribute
import org.saudigitus.campaign.core.data.utils.optionsNotInOptionGroup
import org.saudigitus.campaign.core.data.utils.optionsNotInOptionsSets
import org.saudigitus.campaign.core.data.utils.toOptionsModel
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.data.models.FormResult
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.form.rules.FormValidationRulesRepository
import org.saudigitus.campaign.core.form.ui.state.FormSectionType
import org.saudigitus.campaign.core.form.utils.CustomValueType
import org.saudigitus.campaign.core.form.utils.toEntities
import org.saudigitus.campaign.core.form.utils.toSectionEntities
import org.saudigitus.campaign.core.utils.Constants
import org.saudigitus.campaign.core.utils.IdGenerator
import javax.inject.Inject

class FormRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val repository: ProgramStageRepository,
    private val optionRepository: OptionRepository,
    private val ruleEngineRepository: RuleEngineRepository,
    private val programRepository: ProgramRepository,
    private val datastoreRepository: DatastoreRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val eventRepository: EventRepository,
    private val teiRepository: TeiRepository,
    private val goalRepository: GoalRepository,
    private val formValidationRules: FormValidationRulesRepository,
) : FormRepository {

    private val hideProgramStages = mutableListOf<String>()

    override suspend fun save(
        formType: FormSectionType,
        orgUnit: String,
        program: String,
        date: String,
        tei: String?,
        enrollment: String?,
        formSections: List<FormSectionModel>
    ): FormResult = withContext(Dispatchers.IO) {
        when (formType) {
            FormSectionType.NEW_ENROLLMENT -> {
                val fields = formSections
                    .filter { it.rendered }
                    .flatMap { section -> section.formFields.filter { field -> field.rendered == true } }
                    .toEntities()

                val result = enrollmentRepository.create(
                    orgUnit,
                    program,
                    date,
                    fields
                )

                goalRepository.incrementProgress(Constants.DEFAULT, 1)
                doGoalCalculations(formSections)

                FormResult(
                    enrollment = result.first,
                    tei = result.second,
                )
            }

            FormSectionType.EDIT_ENROLLMENT -> {
                val fields = formSections
                    .filter { it.rendered }
                    .flatMap { section -> section.formFields.filter { field -> field.rendered == true } }
                    .toEntities()

                enrollmentRepository.update(
                    enrollment!!,
                    tei!!,
                    date,
                    fields
                )

                FormResult(
                    enrollment = enrollment,
                    tei = tei,
                )
            }

            else -> {
                val isSaved = eventRepository.saveEvent(
                    orgUnit = orgUnit,
                    program = program,
                    tei = tei,
                    enrollment = enrollment,
                    eventDate = date,
                    formSections = formSections.toSectionEntities(),
                )

                if (isSaved) {
                    doGoalCalculations(formSections)
                }

                FormResult(isEventSaved = isSaved)
            }
        }
    }

    override suspend fun getFormSections(
        orgUnit: String,
        program: String,
        tei: String?,
        enrollment: String?,
        vararg programStages: String?
    ) = withContext(Dispatchers.IO) {
        val uids = programStages.filterNotNull()

        val ps = uids.ifEmpty {
            val uid = d2.programModule().programStages()
                .byProgramUid().eq(program)
                .one().blockingGet()?.uid().orEmpty()

            listOf(uid)
        }.toTypedArray()

        val event = eventRepository.getOrGenerateEvent(
            orgUnit = orgUnit,
            program = program,
            programStage = ps.firstOrNull().orEmpty(),
            tei = tei.orEmpty(),
            enrollment = enrollment.orEmpty()
        )

        val sections = repository.getProgramStageSectionsWithDataElements(*ps).map {
            val programStageUid = it.programStageUid
            val formFields = it.programStageDataElements.map { psDataElement ->
                val optionModels = getOptionModels(
                    dl = psDataElement.dataElement?.uid().orEmpty()
                )
                val userLevel = getUserOuLevel()
                val dataElement = getAppConfig(program)
                    ?.fields?.dataElements?.find { dl ->
                        val configuredProgramStages = dl.programStages.orEmpty()
                        val appliesToProgramStage = configuredProgramStages.isEmpty() ||
                            programStageUid?.let(configuredProgramStages::contains) == true
                        val appliesToUserLevel = dl.userLevel == null ||
                            dl.userLevel == userLevel

                        dl.dataElement == psDataElement.dataElement?.uid() &&
                            appliesToProgramStage && appliesToUserLevel
                    }

                val userId =
                    if (psDataElement.dataElement?.valueType() == ValueType.ORGANISATION_UNIT) {
                        d2.userModule().user().blockingGet()?.uid()
                    } else null

                if (dataElement != null) {
                    val ouHideStrategy = OuHideStrategy.entries.find { strategy ->
                        dataElement.ouHideStrategy == strategy.name
                    }

                    val customValueType = CustomValueType.entries.find { type ->
                        type.name == dataElement.valueType
                    }

                    val customOptions = if (userLevel == dataElement.userLevel) {
                        dataElement.options?.toOptionsModel()
                    } else emptyList()

                    FormFieldModel(
                        label = psDataElement.dataElement?.displayFormName().orEmpty(),
                        uid = psDataElement.dataElement?.uid().orEmpty(),
                        value = when {
                            dataElement.autoGenerate == true -> IdGenerator.generate()
                            dataElement.initialValue != null -> dataElement.initialValue.toString()
                            else -> null
                        },
                        valueType = if (dataElement.valueType == null) {
                            psDataElement.dataElement?.valueType()
                        } else null,
                        customValueType = if (dataElement.valueType != null) {
                            customValueType
                        } else null,
                        minValue = dataElement.minValue,
                        maxValue = dataElement.maxValue,
                        initialValue = dataElement.initialValue,
                        dlToLimit = dataElement.limit,
                        optionSet = when (customValueType) {
                            CustomValueType.DROPDOWN,
                            CustomValueType.SCANNABLE_DROPDOWN_FIELD -> customOptions
                            else -> optionModels
                        },
                        enabled = dataElement.enabled,
                        enabledOnAssign = dataElement.enabledOnAssign,
                        mandatory = psDataElement.compulsory,
                        userId = userId,
                        orgUnits = if (customValueType == CustomValueType.SEARCHABLE_ORG_UNIT_FIELD
                        ) {
                            getOrgUnits(orgUnit, ouHideStrategy)
                        } else null,
                        ouTreeModel = if (customValueType == CustomValueType.ORG_UNIT) {
                            ouToHide(
                                orgUnit,
                                ouHideStrategy,
                                dataElement.canSelectOUParent,
                                dataElement.isOUTreeOpen ?: true,
                            )
                        } else null,
                        ouHideStrategy = ouHideStrategy,
                        canSelectOUParent = dataElement.canSelectOUParent,
                        userLevel = dataElement.userLevel,
                    )
                } else {
                    FormFieldModel(
                        label = psDataElement.dataElement?.displayFormName().orEmpty(),
                        uid = psDataElement.dataElement?.uid().orEmpty(),
                        valueType = psDataElement.dataElement?.valueType(),
                        optionSet = optionModels,
                        mandatory = psDataElement.compulsory,
                        userId = userId
                    )
                }
            }

            FormSectionModel(
                uid = it.uid,
                programStage = it.programStageUid,
                eventUid = event?.uid(),
                name = it.displayName,
                description = it.description,
                code = it.code,
                formFields = formFields,
                sortOrder = it.sortOrder
            )
        }

        val eventUid = sections.firstOrNull()?.eventUid.orEmpty()
        val sectionsWithRules = applyProgramRules(
            orgUnit = orgUnit,
            program = program,
            programStage = ps.firstOrNull().orEmpty(),
            event = eventUid,
            enrollment = enrollment,
            formSections = sections
        )

        evaluateSectionsWithRules(
            orgUnit = orgUnit,
            program = program,
            programStage = ps.firstOrNull().orEmpty(),
            event = eventUid,
            enrollment = enrollment,
            sectionWithRules = sectionsWithRules
        )
    }

    override suspend fun getFormSections(orgUnit: String, program: String, tei: String?) =
        withContext(Dispatchers.IO) {
            val sections = programRepository.getTrackedEntityAttributeWithSection(program).map {
                val formFields = it.attributes.map { attribute ->
                    val optionModels = getOptionModels(optionSetUid = attribute.optionSetUid)
                    val dsAttribute = getAppConfig(program)
                        ?.fields?.attributes?.find { attr -> attr.attribute == attribute.uid }

                    val userId = if (attribute.valueType == ValueType.ORGANISATION_UNIT) {
                        d2.userModule().user().blockingGet()?.uid()
                    } else null

                    val teiAttributeValue = attributeValue(tei = tei, attr = attribute.uid)

                    if (dsAttribute != null) {
                        val ouHideStrategy = OuHideStrategy.entries.find { strategy ->
                            dsAttribute.ouHideStrategy == strategy.name
                        }

                        val customValueType = CustomValueType.entries.find { type ->
                            type.name == dsAttribute.valueType
                        }

                        FormFieldModel(
                            label = attribute.displayFormName.orEmpty(),
                            uid = attribute.uid,
                            value = when {
                                !tei.isNullOrEmpty() && teiAttributeValue != null -> teiAttributeValue.value()
                                dsAttribute.autoGenerate == true -> IdGenerator.generate()
                                dsAttribute.initialValue != null -> dsAttribute.initialValue.toString()
                                else -> null
                            },
                            valueType = if (dsAttribute.valueType == null) attribute.valueType else null,
                            customValueType = if (dsAttribute.valueType != null) {
                                customValueType
                            } else null,
                            minValue = dsAttribute.minValue,
                            maxValue = dsAttribute.maxValue,
                            initialValue = dsAttribute.initialValue,
                            optionSet = optionModels,
                            enabled = dsAttribute.enabled,
                            enabledOnAssign = dsAttribute.enabledOnAssign,
                            mandatory = attribute.mandatory ?: dsAttribute.mandatory,
                            userId = userId,
                            orgUnits = if (customValueType == CustomValueType.SEARCHABLE_ORG_UNIT_FIELD
                            ) {
                                getOrgUnits(orgUnit, ouHideStrategy)
                            } else null,
                            ouTreeModel = if (customValueType == CustomValueType.ORG_UNIT) {
                                ouToHide(
                                    orgUnit,
                                    ouHideStrategy,
                                    dsAttribute.canSelectOUParent,
                                    dsAttribute.isOUTreeOpen ?: true,
                                )
                            } else null,
                            ouHideStrategy = ouHideStrategy,
                            canSelectOUParent = dsAttribute.canSelectOUParent,
                        )
                    } else {
                        FormFieldModel(
                            label = attribute.displayFormName.orEmpty(),
                            uid = attribute.uid,
                            value = teiAttributeValue?.value(),
                            valueType = attribute.valueType,
                            optionSet = optionModels,
                            mandatory = attribute.mandatory,
                            userId = userId,
                        )
                    }
                }

                val registrationDate = enrollmentRepository.getEnrollmentDate(tei)

                FormSectionModel(
                    uid = it.uid,
                    name = it.displayName,
                    description = it.description,
                    code = it.code,
                    formFields = formFields,
                    sortOrder = it.sortOrder,
                    registrationDate = registrationDate,
                )
            }

            val sectionWithRules = applyProgramRules(
                orgUnit = orgUnit,
                program = program,
                formSections = sections
            )

            evaluateSectionsWithRules(
                orgUnit = orgUnit,
                program = program,
                sectionWithRules = sectionWithRules
            )
        }

    override suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String?,
        event: String?,
        enrollment: String?,
        formSections: List<FormSectionModel>
    ): List<FormSectionModel> = withContext(Dispatchers.IO) {
        var updatedSections = formSections
            .map { section ->
                section.copy(
                    rendered = true,
                    formFields = section.formFields.map { field ->
                        field.copy(
                            rendered = true,
                            mandatory = field.baseMandatory,
                            hasError = false,
                            errorMessage = null,
                            hasWarning = false,
                            warningMessage = null
                        )
                    }
                )
            }
            .associateBy { it.uid }
            .toMutableMap()

        updatedSections = formValidationRules.applyValidations(
            program = program,
            programStage = programStage,
            event = event,
            enrollment = enrollment,
            formSections = updatedSections
        )

        val updatedFields = updatedSections.values
            .flatMap { section -> section.formFields }
            .associateBy { it.uid }
            .toMutableMap()

        val valueStore = formSections
            .flatMap { section -> section.formFields }
            .associate { field ->
                field.uid to field.value.orEmpty()
            }

        val ruleEffects = if (event?.isNotEmpty() == true) {
            ruleEngineRepository.evaluate(
                ou = orgUnit,
                program = program,
                event = event,
                enrollment = enrollment,
                dataValues = valueStore,
            )
        } else {
            ruleEngineRepository.evaluate(
                ou = orgUnit,
                program = program,
                attributeValues = valueStore
            )
        }

        val hideOptions = mutableListOf<String>()

        for (ruleEffect in ruleEffects) {
            val action = ruleEffect.ruleAction

            when (ProgramRuleActionType.valueOf(action.type)) {
                ProgramRuleActionType.HIDEFIELD -> {
                    val fieldUid = action.field() ?: continue
                    val field = updatedFields[fieldUid] ?: continue

                    if (field.mandatory != true) {
                        updatedFields[fieldUid] = field.copy(
                            rendered = false,
                            value = null
                        )
                    }
                }

                ProgramRuleActionType.HIDESECTION -> {
                    val sectionUid = action.values["programStageSection"]
                        ?: action.values["programSection"]
                        ?: continue
                    val section = updatedSections[sectionUid] ?: continue

                    updatedSections[sectionUid] = section.copy(
                        rendered = false,
                        formFields = section.formFields.map { sectionField ->
                            updatedFields[sectionField.uid] = sectionField.copy(
                                rendered = false,
                                value = null,
                                hasError = false,
                                errorMessage = null,
                                hasWarning = false,
                                warningMessage = null
                            )
                            updatedFields[sectionField.uid]!!
                        }
                    )
                }

                ProgramRuleActionType.HIDEOPTION -> {
                    val fieldUid = action.field() ?: continue
                    val field = updatedFields[fieldUid] ?: continue
                    hideOptions.add(action.values["option"] ?: continue)

                    updatedFields[fieldUid] = field.copy(
                        optionSet = getOptions(
                            field.uid,
                            hideOptions,
                            ProgramRuleActionType.HIDEOPTION
                        )
                    )
                }

                ProgramRuleActionType.HIDEOPTIONGROUP -> {
                    val fieldUid = action.field() ?: continue
                    val field = updatedFields[fieldUid] ?: continue
                    hideOptions.add(action.values["optionGroup"] ?: continue)

                    updatedFields[fieldUid] = field.copy(
                        optionSet = getOptions(
                            field.uid,
                            hideOptions,
                            ProgramRuleActionType.HIDEOPTIONGROUP
                        )
                    )
                }

                ProgramRuleActionType.SHOWERROR -> {
                    val fieldUid = action.field() ?: continue
                    val field = updatedFields[fieldUid] ?: continue
                    updatedFields[fieldUid] = field.copy(
                        hasError = true,
                        errorMessage = action.content() ?: action.data.orEmpty()
                    )
                }

                ProgramRuleActionType.SHOWWARNING -> {
                    val fieldUid = action.field() ?: continue
                    val field = updatedFields[fieldUid] ?: continue
                    updatedFields[fieldUid] = field.copy(
                        hasWarning = true,
                        warningMessage = action.content() ?: action.data.orEmpty()
                    )
                }

                ProgramRuleActionType.ASSIGN -> {
                    val fieldUid = action.field() ?: continue
                    val field = updatedFields[fieldUid] ?: continue

                    val formattedValue = ruleEffect.data?.formatRuleValue(field)
                    if (formattedValue != null) {
                        updatedFields[fieldUid] = field.copy(
                            value = formattedValue,
                            enabled = field.enabledOnAssign ?: false,
                        )
                    }
                }

                ProgramRuleActionType.SETMANDATORYFIELD -> {
                    val fieldUid = action.field() ?: continue
                    val field = updatedFields[fieldUid] ?: continue
                    updatedFields[fieldUid] = field.copy(
                        mandatory = true,
                        rendered = true
                    )
                }

                ProgramRuleActionType.HIDEPROGRAMSTAGE -> {
                    hideProgramStages.add(action.values["programStage"] ?: continue)
                }

                else -> Unit
            }
        }

        return@withContext updatedSections.values.map { section ->
            val updatedSectionFields = section.formFields.map { sectionField ->
                updatedFields[sectionField.uid] ?: sectionField
            }

            section.copy(
                rendered = section.rendered &&
                    updatedSectionFields.any { field -> field.rendered == true },
                formFields = updatedSectionFields
            )
        }.ifEmpty { formSections }
    }

    override suspend fun searchOrgUnits(
        query: String?,
        orgUnit: String,
        ouHideStrategy: OuHideStrategy?
    ): List<OrgUnit> = withContext(Dispatchers.IO) {
        getOrgUnits(orgUnit, ouHideStrategy, query)
    }

    override suspend fun getDefaultProgramStage(
        program: String
    ) = withContext(Dispatchers.IO) {
        val stages = repository.getActiveProgramStages(program)
        if (stages.size == 1) stages.first().uid else null
    }

    override suspend fun programStagesToHide(): List<String> {
        return hideProgramStages
    }

    override suspend fun getOrgUnitName(ou: String) = withContext(Dispatchers.IO) {
        d2.organisationUnit(ou)?.displayName()
    }

    override suspend fun deleteEvent(event: String) = withContext(Dispatchers.IO) {
        eventRepository.deleteEvent(event)
    }

    private suspend fun getOptionModels(
        optionSetUid: String? = null,
        dl: String? = null,
    ): List<OptionModel> {
        return if (optionSetUid == null && dl != null) {
            optionRepository.getOptionsByDataElement(dl)
                .map {
                    OptionModel(
                        uid = it.uid(),
                        code = it.code(),
                        displayName = it.displayName(),
                        sortOrder = it.sortOrder(),
                    )
                }.sortedBy { it.sortOrder }
        } else {
            optionRepository.getOptions(optionSetUid.orEmpty()).map {
                OptionModel(
                    uid = it.uid(),
                    code = it.code(),
                    displayName = it.displayName(),
                    sortOrder = it.sortOrder(),
                )
            }.sortedBy { it.sortOrder }
        }
    }

    private fun getOptions(
        uid: String,
        hideOptions: List<String>,
        actionType: ProgramRuleActionType
    ): List<OptionModel> {
        val optionSet = d2.dataElement(uid)?.optionSetUid()
            ?: d2.trackedEntityModule()
                .trackedEntityAttributes()
                .byUid().eq(uid)
                .one().blockingGet()
                ?.optionSet()?.uid()

        return when (actionType) {
            ProgramRuleActionType.HIDEOPTION -> {
                d2.optionsNotInOptionsSets(hideOptions, optionSet).map {
                    OptionModel(
                        uid = it.uid(),
                        code = it.code(),
                        displayName = it.displayName(),
                        sortOrder = it.sortOrder(),
                    )
                }.sortedBy { it.sortOrder }
            }

            ProgramRuleActionType.HIDEOPTIONGROUP -> {
                d2.optionsNotInOptionGroup(hideOptions, optionSet)
                    .map {
                        OptionModel(
                            uid = it.uid(),
                            code = it.code(),
                            displayName = it.displayName(),
                            sortOrder = it.sortOrder(),
                        )
                    }.sortedBy { it.sortOrder }
            }

            else -> emptyList()
        }
    }

    private suspend fun evaluateSectionsWithRules(
        orgUnit: String,
        program: String,
        programStage: String? = null,
        event: String? = null,
        enrollment: String? = null,
        sectionWithRules: List<FormSectionModel>,
    ): List<FormSectionModel> {
        var sectionsWithRules = sectionWithRules

        if (sectionWithRules.flatMap { section -> section.formFields }
                .any { field -> field.dlToLimit != null }) {
            val fieldMap = sectionWithRules
                .flatMap { section -> section.formFields }
                .associateBy { field -> field.uid }

            val updatedSections = sectionWithRules.map { section ->
                section.copy(
                    formFields = section.formFields.map { field ->
                        val sourceField = field.dlToLimit?.let { limit -> fieldMap[limit] }

                        if (sourceField != null) {
                            field.copy(
                                value = sourceField.value ?: field.value,
                                initialValue = sourceField.value?.toIntOrNull() ?: field.maxValue,
                                maxValue = sourceField.value?.toIntOrNull() ?: field.maxValue
                            )
                        } else {
                            field
                        }
                    }
                )
            }

            sectionsWithRules = applyProgramRules(
                orgUnit = orgUnit,
                program = program,
                programStage = programStage,
                event = event,
                enrollment = enrollment,
                formSections = updatedSections
            )
        }

        return sectionsWithRules
    }

    private suspend fun getAppConfig(program: String): AppConfigItem? {
        return datastoreRepository.getAppConfig(program)
    }

    private suspend fun getGlobalConfig(): GlobalConfigItem? {
        return datastoreRepository.getGlobalConfig(Constants.DS_GLOBAL_KEY)
    }

    private fun String.formatRuleValue(field: FormFieldModel): String? {
        val valueType = when (field.customValueType) {
            CustomValueType.COUNTER -> ValueType.INTEGER
            else -> field.valueType
        }

        return formatData(valueType)
    }

    private suspend fun getGoalDataElement() = withContext(Dispatchers.IO) {
        val goals = getGlobalConfig()?.goals.orEmpty()

        goals.mapNotNull { it.uid }
            .toList()
    }

    private suspend fun ouToHide(
        parentOu: String,
        ouHideStrategy: OuHideStrategy? = null,
        canSelectOUParent: Boolean = true,
        isOUTreeOpen: Boolean = true,
    ): OUTreeModel? =
        withContext(Dispatchers.IO) {
            val ouRepository = d2.organisationUnitModule()
                .organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byParentUid().eq(parentOu)

            val orgUnit = d2.organisationUnit(parentOu)
                ?: return@withContext null

            when (ouHideStrategy) {
                OuHideStrategy.PARENT_AND_REMAINING -> {
                    val children = ouRepository.blockingGetUids()
                        .toMutableList()

                    val toHide = d2.organisationUnitModule()
                        .organisationUnits()
                        .byUid().notIn(children)
                        .blockingGet()

                    OUTreeModel(
                        hideOrgUnits = toHide,
                        canSelectOUParent = canSelectOUParent,
                        isOUTreeOpen = isOUTreeOpen,
                    )
                }

                OuHideStrategy.PARENT_AND_CHILDREN -> {
                    val childrenAndParent = ouRepository
                        .blockingGet()
                        .toMutableList()

                    childrenAndParent.add(orgUnit)

                    OUTreeModel(
                        hideOrgUnits = childrenAndParent,
                        canSelectOUParent = canSelectOUParent,
                        isOUTreeOpen = isOUTreeOpen,
                    )
                }

                else -> null
            }
        }

    private suspend fun getOrgUnits(
        parentOu: String,
        ouHideStrategy: OuHideStrategy? = null,
        query: String? = null
    ) =
        withContext(Dispatchers.IO) {
            val ouRepository = d2.organisationUnitModule()
                .organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byParentUid().eq(parentOu)

            when (ouHideStrategy) {
                OuHideStrategy.PARENT_AND_REMAINING -> {
                    val data = if (!query.isNullOrEmpty()) {
                        ouRepository
                            .byDisplayName().like(query)
                            .blockingGet()
                            .map {
                                OrgUnit(
                                    uid = it.uid(),
                                    displayName = it.displayName().orEmpty()
                                )
                            }
                    } else {
                        ouRepository
                            .blockingGet()
                            .map {
                                OrgUnit(
                                    uid = it.uid(),
                                    displayName = it.displayName().orEmpty()
                                )
                            }
                    }

                    data
                }

                OuHideStrategy.PARENT_AND_CHILDREN -> {
                    val children = ouRepository
                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                        .blockingGetUids()
                        .toMutableList()

                    val orgUnit = d2.organisationUnit(parentOu)
                        ?: return@withContext emptyList()

                    children.add(orgUnit.uid().orEmpty())

                    val ouRepository2 = d2.organisationUnitModule()
                        .organisationUnits()
                        .byUid().notIn(children)
                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)

                    val data = if (!query.isNullOrEmpty()) {
                        ouRepository2
                            .byDisplayName().like(query)
                            .blockingGet()
                            .map {
                                OrgUnit(
                                    uid = it.uid(),
                                    displayName = it.displayName().orEmpty()
                                )
                            }
                    } else {
                        ouRepository2.blockingGet()
                            .map {
                                OrgUnit(
                                    uid = it.uid(),
                                    displayName = it.displayName().orEmpty()
                                )
                            }
                    }

                    data
                }

                else -> emptyList()
            }
        }

    suspend fun doGoalCalculations(formSections: List<FormSectionModel>) {
        val dataElements = getGoalDataElement()

        val fields = formSections.flatMap { it.formFields }
            .filter { it.uid in dataElements }

        if (fields.isNotEmpty()) {
            fields.forEach { dataElement ->
                goalRepository.incrementProgress(
                    dataElement.uid,
                    dataElement.value?.toInt() ?: 0
                )
            }
        }
    }

    suspend fun getUserOuLevel(): Int?  = withContext(Dispatchers.IO) {
        val level = d2.databaseAdapter()
            .rawQuery(
                """
                    SELECT MIN (level) as level FROM organisationunit 
                    WHERE  uid IN (
                    SELECT uid FROM userorganisationunit 
                    WHERE organisationUnitScope = 'SCOPE_DATA_CAPTURE')
                """.trimIndent()
            )[0]["level"]

        return@withContext level?.toInt()
    }

    suspend fun attributeValue(tei: String?, attr: String): TrackedEntityAttributeValue? {
        if (tei.isNullOrEmpty()) return null

        return teiRepository.getTrackedEntityAttributeValues(tei)
            .byAttribute(attr)
    }
}
