package org.saudigitus.semis.core.data.rules

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.organisationUnit
import org.dhis2.commons.bindings.programStage
import org.dhis2.commons.rules.RuleEngineContextData
import org.dhis2.mobileProgramRules.toRuleDataValue
import org.dhis2.mobileProgramRules.toRuleEngineInstant
import org.dhis2.mobileProgramRules.toRuleEngineLocalDate
import org.dhis2.mobileProgramRules.toRuleEngineObject
import org.dhis2.mobileProgramRules.toRuleVariable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.api.RuleEngineContext
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleEventStatus
import org.hisp.dhis.rules.models.RuleVariable
import java.util.Collections
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class RuleEngineRepository @Inject constructor(
    private val d2: D2,
) {

    private val ruleEngine by lazy { RuleEngine.getInstance() }

    private suspend fun supplementaryData(ou: String) = withContext(Dispatchers.IO) {
        val suppData = HashMap<String, List<String>>()

        d2.organisationUnitModule().organisationUnits()
            .withOrganisationUnitGroups()
            .uid(ou).blockingGet()
            .let { orgUnit ->
                orgUnit?.organisationUnitGroups()?.mapNotNull {
                    if (it.code() != null) {
                        suppData[it.code()!!] = listOf(orgUnit.uid())
                    }
                    suppData[it.uid()] = listOf(orgUnit.uid())
                }
            }

        return@withContext suppData
    }

    private suspend fun ruleVariables(program: String) = withContext(Dispatchers.IO) {
        return@withContext d2.programModule().programRuleVariables()
            .byProgramUid().eq(program)
            .blockingGet()
            .map {
                it.toRuleVariable(
                    d2.trackedEntityModule().trackedEntityAttributes(),
                    d2.dataElementModule().dataElements(),
                )
            }
    }

    suspend fun rules(program: String) = withContext(Dispatchers.IO) {
        return@withContext d2.programModule().programRules()
            .byProgramUid().eq(program)
            .withProgramRuleActions()
            .blockingGet()
            .map {
                it.toRuleEngineObject()
            }
    }

    suspend fun constants() = withContext(Dispatchers.IO) {
        return@withContext d2.constantModule()
            .constants().blockingGet()
            .associate { constant ->
                Pair(constant.uid(), "${constant.value()}")
            }
    }

    @Suppress("DEPRECATION")
    private suspend fun ruleEvents(
        ou: String,
        program: String,
    ) = withContext(Dispatchers.IO) {
        return@withContext d2.eventModule().events()
            .byOrganisationUnitUid().eq(ou)
            .byProgramUid().eq(program)
            .withTrackedEntityDataValues()
            .blockingGet()
            .map { event ->
                RuleEvent(
                    event = event.uid(),
                    programStage = event.programStage()!!,
                    programStageName = d2.programModule().programStages()
                        .uid(event.programStage())
                        .blockingGet()!!.name()!!,
                    status = if (event.status() == EventStatus.VISITED) {
                        RuleEventStatus.ACTIVE
                    } else {
                        RuleEventStatus.valueOf(event.status()!!.name)
                    },
                    eventDate = Instant.fromEpochMilliseconds(event.eventDate()!!.time),
                    dueDate = event.dueDate()?.let {
                        Instant.fromEpochMilliseconds(it.time)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    },
                    completedDate = event.completedDate()?.let {
                        Instant.fromEpochMilliseconds(it.time)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    },
                    organisationUnit = event.organisationUnit()!!,
                    organisationUnitCode = d2.organisationUnitModule().organisationUnits()
                        .uid(
                            event.organisationUnit(),
                        ).blockingGet()?.code(),
                    dataValues = event.trackedEntityDataValues()?.toRuleDataValue(
                        event,
                        dataElementRepository = d2.dataElementModule().dataElements(),
                        ruleVariableRepository = d2.programModule().programRuleVariables(),
                        optionRepository = d2.optionModule().options(),
                    ) ?: emptyList(),
                    createdDate = Instant.fromEpochMilliseconds(event.created()!!.time),
                )
            }
    }

    private suspend fun ruleContext(
        ruleVariables: List<RuleVariable>,
        rules: List<Rule>,
        supplementaryData: Map<String, List<String>> = emptyMap(),
        constants: Map<String, String>,
    ) = withContext(Dispatchers.IO) {
        return@withContext RuleEngineContext(
            rules = rules,
            ruleVariables = ruleVariables,
            supplementaryData = supplementaryData,
            constantsValues = constants,
        )
    }

    private suspend fun executeContext(
        ou: String? = null,
        program: String,
    ) = withContext(Dispatchers.IO) {
        val rules = async { rules(program) }.await()
        val ruleVariables = ruleVariables(program)
        val constants = async { constants() }.await()
        val supplementaryData = ou?.let {
            async { supplementaryData(it) }.await()
        } ?: emptyMap()

        return@withContext ruleContext(
            ruleVariables,
            rules,
            supplementaryData,
            constants,
        )
    }

    private suspend fun ruleEngineContextData(
        ou: String,
        program: String,
    ) = withContext(Dispatchers.IO) {
        val rules = async { rules(program) }.await()
        val ruleVariables = ruleVariables(program)
        val constants = async { constants() }.await()
        val ruleEvents = async { ruleEvents(ou, program) }.await()
        val supplementaryData = async { supplementaryData(ou) }.await()

        return@withContext RuleEngineContextData(
            ruleEngineContext = ruleContext(
                ruleVariables,
                rules,
                supplementaryData,
                constants,
            ),
            ruleEnrollment = null,
            ruleEvents = ruleEvents,
        )
    }

    private fun getRuleEvent(
        eventUid: String,
        dataValues: List<RuleDataValue> = emptyList(),
    ): RuleEvent {
        val event = d2.event(eventUid) ?: throw NullPointerException()
        return RuleEvent(
            event = event.uid(),
            programStage = event.programStage()!!,
            programStageName = d2.programStage(event.programStage()!!)?.name()!!,
            status = RuleEventStatus.valueOf(event.status()!!.name),
            eventDate = event.eventDate()!!.toRuleEngineInstant(),
            dueDate = event.dueDate()?.toRuleEngineLocalDate(),
            completedDate = event.completedDate()?.toRuleEngineLocalDate(),
            organisationUnit = event.organisationUnit()!!,
            organisationUnitCode = d2.organisationUnit(event.organisationUnit()!!)?.code(),
            dataValues = dataValues,
            createdDate = Instant.fromEpochSeconds(event.created()!!.time),
        )
    }

    /**
     * Resolves the option level rules that restrict [dataElement] of [program] for the school [ou].
     *
     * These rules carry conditions such as `d2:inOrgUnitGroup('SSS')`, so their actions only apply to
     * the schools the condition matches. Reading the actions off the configured rules would apply
     * every restriction to every school, so the rules are evaluated instead: the engine returns the
     * effects of the rules whose condition held, and discards the rest.
     *
     * The engine only evaluates conditions against a target it can read the school from, so a
     * throwaway target carrying [ou] stands in for the record being filled. Without [ou] no
     * organisation unit condition can hold and the field keeps its full option set.
     */
    suspend fun applyOptionRules(
        ou: String? = null,
        program: String,
        dataElement: String,
    ): OptionRuleEffects = withContext(Dispatchers.IO) {
        val optionsToHide = mutableListOf<String>()
        val optionGroupsToHide = mutableListOf<String>()
        val optionGroupsToShow = mutableListOf<String>()

        ruleEngine.evaluate(
            target = optionRuleTarget(ou),
            ruleEnrollment = null,
            ruleEvents = emptyList(),
            executionContext = executeContext(ou, program),
        ).asSequence()
            .map { it.ruleAction }
            .filter { it.field() == dataElement }
            .forEach { action ->
                when (action.type) {
                    ProgramRuleActionType.HIDEOPTION.name ->
                        action.values["option"]?.let(optionsToHide::add)

                    ProgramRuleActionType.HIDEOPTIONGROUP.name ->
                        action.values["optionGroup"]?.let(optionGroupsToHide::add)

                    ProgramRuleActionType.SHOWOPTIONGROUP.name ->
                        action.values["optionGroup"]?.let(optionGroupsToShow::add)

                    // Every other action type shapes the form, not the option list of this field.
                    else -> Unit
                }
            }

        return@withContext OptionRuleEffects(
            optionsToHide = optionsToHide,
            optionGroupsToHide = optionGroupsToHide,
            optionGroupsToShow = optionGroupsToShow,
        )
    }

    /**
     * Builds the throwaway target the option rules are evaluated against.
     *
     * Nothing here is persisted and no program stage is involved: the target exists only to carry
     * [ou], which is the value `d2:inOrgUnitGroup` matches against the organisation unit groups
     * published in the context supplementary data. Filtering an option list happens before any
     * record exists, so there is no real event to evaluate instead. The current date stands in for
     * the event date so that a date based condition is evaluated as of now.
     */
    private fun optionRuleTarget(ou: String?): RuleEvent {
        val now = Date()
        return RuleEvent(
            event = UUID.randomUUID().toString(),
            programStage = "",
            programStageName = "",
            status = RuleEventStatus.ACTIVE,
            eventDate = now.toRuleEngineInstant(),
            createdDate = now.toRuleEngineInstant(),
            dueDate = null,
            completedDate = null,
            organisationUnit = ou.orEmpty(),
            organisationUnitCode = ou?.let { d2.organisationUnit(it)?.code() },
            dataValues = emptyList(),
        )
    }

    private fun dataEntry(
        dataElement: String,
        value: String,
    ) = RuleDataValue(
        dataElement = dataElement,
        value = value
    )

    suspend fun evaluateDataEntry(
        ou: String,
        program: String,
        dataElement: String,
        event: String,
        value: String
    ) = try {
        evaluate(
            ou,
            program,
            event,
            Collections.singletonList(
                dataEntry(
                    dataElement,
                    value,
                )
            )
        )
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun evaluate(
        ou: String,
        program: String,
        event: String,
        dataValues: List<RuleDataValue> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val ruleEngineContextData = ruleEngineContextData(ou, program)
        val events = ruleEngineContextData.ruleEvents.filter {
            it.event != event
        }

        return@withContext ruleEngine.evaluate(
            target = getRuleEvent(event, dataValues),
            ruleEnrollment = ruleEngineContextData.ruleEnrollment,
            ruleEvents = events,
            executionContext = ruleEngineContextData.ruleEngineContext,
        )
    }

    /**
     * Evaluates the rules for a record that has not been saved yet.
     *
     * The values a person is being given are not in the database while they are being typed, and
     * for a person with nothing recorded yet there is no event at all, so a throwaway target
     * carries the school, the stage and [dataValues] as they stand. Rules scoped to another stage
     * are left out, and no neighbouring events are loaded: what is being judged is this record.
     */
    suspend fun evaluateUnsavedEvent(
        ou: String,
        program: String,
        programStage: String,
        dataValues: Map<String, String>,
    ) = withContext(Dispatchers.IO) {
        val stageRules = rules(program).filter { rule ->
            rule.programStage == null || rule.programStage == programStage
        }
        val engineContext = ruleContext(
            ruleVariables = ruleVariables(program),
            rules = stageRules,
            supplementaryData = supplementaryData(ou),
            constants = constants(),
        )

        val ruleDataValues = dataValues.mapNotNull { (dataElement, value) ->
            engineValue(program, dataElement, value)
                ?.takeIf { it.isNotEmpty() }
                ?.let { RuleDataValue(dataElement = dataElement, value = it) }
        }

        return@withContext ruleEngine.evaluate(
            target = unsavedEvent(ou, programStage, ruleDataValues),
            ruleEnrollment = null,
            ruleEvents = emptyList(),
            executionContext = engineContext,
        )
    }

    /** The stand-in target carrying what an unsaved record knows: the school, the stage, the values. */
    private fun unsavedEvent(
        ou: String,
        programStage: String,
        dataValues: List<RuleDataValue>,
    ): RuleEvent {
        val now = Date()
        return RuleEvent(
            event = UUID.randomUUID().toString(),
            programStage = programStage,
            programStageName = d2.programStage(programStage)?.name().orEmpty(),
            status = RuleEventStatus.ACTIVE,
            eventDate = now.toRuleEngineInstant(),
            createdDate = now.toRuleEngineInstant(),
            dueDate = null,
            completedDate = null,
            organisationUnit = ou,
            organisationUnitCode = d2.organisationUnit(ou)?.code(),
            dataValues = dataValues,
        )
    }

    /**
     * The value the engine reads for [dataElement], given [raw] as the form holds it.
     *
     * A field backed by an option set holds the option code, but a rule variable is configured to
     * read either that code or the option name, and the conditions are written against whichever
     * it reads. Translating here is what keeps a rule from holding on a saved record and failing
     * on one still being typed.
     */
    private fun engineValue(program: String, dataElement: String, raw: String): String? {
        val optionSet = d2.dataElementModule().dataElements()
            .uid(dataElement)
            .blockingGet()
            ?.optionSetUid()
            ?: return raw

        val readsCode = !d2.programModule().programRuleVariables()
            .byProgramUid().eq(program)
            .byDataElementUid().eq(dataElement)
            .byUseCodeForOptionSet().isTrue
            .blockingIsEmpty()

        if (readsCode) return raw

        return d2.optionModule().options()
            .byOptionSetUid().eq(optionSet)
            .byCode().eq(raw)
            .one().blockingGet()?.name()
    }
}
