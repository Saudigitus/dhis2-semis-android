package org.saudigitus.campaign.core.data.rules

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.organisationUnit
import org.dhis2.commons.bindings.program
import org.dhis2.commons.bindings.programStage
import org.dhis2.commons.rules.RuleEngineContextData
import org.dhis2.mobileProgramRules.toRuleAttributeValue
import org.dhis2.mobileProgramRules.toRuleDataValue
import org.dhis2.mobileProgramRules.toRuleEngineInstant
import org.dhis2.mobileProgramRules.toRuleEngineLocalDate
import org.dhis2.mobileProgramRules.toRuleEngineObject
import org.dhis2.mobileProgramRules.toRuleVariable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.api.RuleEngineContext
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAttributeValue
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.RuleEnrollmentStatus
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleEventStatus
import org.hisp.dhis.rules.models.RuleVariable
import java.util.Date

class RuleEngineRepository(
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

        val userRoleUids = d2.userModule().userRoles().blockingGetUids()
        val userGroupUids = d2.userModule().userGroups().blockingGetUids()
        suppData["USER_ROLES"] = userRoleUids
        suppData["USER_GROUPS"] = userGroupUids
        suppData["android_version"] = listOf(Build.VERSION.SDK_INT.toString())

        return@withContext suppData
    }

    private suspend fun ruleVariables(program: String) = withContext(Dispatchers.IO) {
        return@withContext d2.programModule().programRuleVariables()
            .byProgramUid().eq(program)
            .blockingGet()
            .map {
                it.toRuleVariable(
                    d2.optionModule().options(),
                    d2.trackedEntityModule().trackedEntityAttributes(),
                    d2.dataElementModule().dataElements(),
                )
            }
    }

    suspend fun rules(program: String, event: String? = null) = withContext(Dispatchers.IO) {
        val programStage = event
            ?.takeIf { it.isNotEmpty() }
            ?.let { d2.event(it)?.programStage() }

        return@withContext d2.programModule().programRules()
            .byProgramUid().eq(program)
            .withProgramRuleActions()
            .blockingGet()
            .map {
                it.toRuleEngineObject()
            }.filter { rule ->
                programStage == null || rule.programStage == null || rule.programStage == programStage
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
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()
            .map { event -> event.toRuleEvent() }
    }

    private suspend fun otherEvents(eventUid: String) = withContext(Dispatchers.IO) {
        return@withContext d2.eventModule().events()
            .uid(eventUid)
            .blockingGet()
            ?.let { eventToEvaluate ->
                getOtherEventList(eventToEvaluate).map { event ->
                    event.toRuleEvent()
                }
            } ?: emptyList()
    }

    private fun getOtherEventList(eventToEvaluate: Event): List<Event> {
        return if (!eventToEvaluate.enrollment().isNullOrEmpty()) {
            d2.eventModule().events()
                .byProgramUid().eq(eventToEvaluate.program())
                .byEnrollmentUid().eq(eventToEvaluate.enrollment())
                .byUid().notIn(eventToEvaluate.uid())
                .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                .byEventDate().beforeOrEqual(Date())
                .byDeleted().isFalse
                .withTrackedEntityDataValues()
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()
        } else {
            d2.eventModule().events()
                .byProgramUid().eq(eventToEvaluate.program())
                .byProgramStageUid().eq(eventToEvaluate.programStage())
                .byOrganisationUnitUid().eq(eventToEvaluate.organisationUnit())
                .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                .byEventDate().beforeOrEqual(Date())
                .byDeleted().isFalse
                .withTrackedEntityDataValues()
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()
                .let { list ->
                    val currentEventIndex = list.indexOfFirst { it.uid() == eventToEvaluate.uid() }

                    val newEvents = if (currentEventIndex != -1) {
                        list.subList(0, currentEventIndex).take(OTHER_EVENTS_LIMIT)
                    } else {
                        emptyList()
                    }
                    val previousEvents = if (currentEventIndex != -1) {
                        list.subList(currentEventIndex + 1, list.size).take(OTHER_EVENTS_LIMIT)
                    } else {
                        list.take(OTHER_EVENTS_LIMIT)
                    }

                    newEvents + previousEvents
                }
        }
    }

    private fun Event.toRuleEvent(): RuleEvent {
        return RuleEvent(
            event = uid(),
            programStage = programStage()!!,
            programStageName = d2.programModule().programStages()
                .uid(programStage())
                .blockingGet()!!.name()!!,
            status = if (status() == EventStatus.VISITED) {
                RuleEventStatus.ACTIVE
            } else {
                RuleEventStatus.valueOf(status()!!.name)
            },
            eventDate = Instant.fromEpochMilliseconds(
                eventDate()?.time ?: System.currentTimeMillis()
            ),
            dueDate = dueDate()?.let {
                Instant.fromEpochMilliseconds(it.time)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            },
            completedDate = completedDate()?.let {
                Instant.fromEpochMilliseconds(it.time)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            },
            organisationUnit = organisationUnit()!!,
            organisationUnitCode = d2.organisationUnitModule().organisationUnits()
                .uid(organisationUnit())
                .blockingGet()?.code(),
            dataValues = trackedEntityDataValues()?.toRuleDataValue() ?: emptyList(),
            createdDate = created()?.toRuleEngineInstant()
                ?: Instant.fromEpochMilliseconds(System.currentTimeMillis()),
        )
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
        event: String? = null,
    ) = withContext(Dispatchers.IO) {
        val rules = async { rules(program, event) }.await()
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
        enrollment: String? = null,
        event: String? = null,
    ) = withContext(Dispatchers.IO) {
        val engineContext = executeContext(ou, program, event)
        val ruleEvents = async {
            event?.takeIf { it.isNotEmpty() }
                ?.let { otherEvents(it) }
                ?: ruleEvents(ou, program)
        }.await()

        return@withContext RuleEngineContextData(
            ruleEngineContext = engineContext,
            ruleEnrollment = getRuleEnrollment(enrollment),
            ruleEvents = ruleEvents,
        )
    }

    private suspend fun ruleEngineContextData(
        ou: String,
        program: String,
        enrollment: String,
        attributeValues: List<RuleAttributeValue>? = null
    ) = withContext(Dispatchers.IO) {
        val engineContext = executeContext(ou, program)

        return@withContext RuleEngineContextData(
            ruleEngineContext = engineContext,
            ruleEnrollment = getRuleEnrollment(ou, program, enrollment, attributeValues),
            ruleEvents = emptyList(),
        )
    }

    fun queryAttributeValues(enrollmentUid: String): List<RuleAttributeValue> =
        d2
            .enrollmentModule()
            .enrollments()
            .uid(enrollmentUid)
            .blockingGet()
            ?.let { enrollment ->
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributeValues()
                    .byTrackedEntityInstance()
                    .eq(enrollment.trackedEntityInstance())
                    .blockingGet()
                    .toRuleAttributeValue(d2)
            } ?: emptyList()

    private fun getRuleEnrollment(enrollmentUid: String?): RuleEnrollment? {
        if (enrollmentUid == null) return null

        val enrollment = d2.enrollment(enrollmentUid) ?: return null

        return RuleEnrollment(
            enrollment = enrollment.uid(),
            programName = d2.program(enrollment.program()!!)?.name()!!,
            incidentDate = (enrollment.incidentDate() ?: Date()).toRuleEngineLocalDate(),
            enrollmentDate = (enrollment.enrollmentDate() ?: Date()).toRuleEngineLocalDate(),
            status = RuleEnrollmentStatus.valueOf(enrollment.status()!!.name),
            organisationUnit = enrollment.organisationUnit()!!,
            organisationUnitCode =
                d2
                    .organisationUnit(enrollment.organisationUnit()!!)
                    ?.code() ?: "",
            attributeValues = queryAttributeValues(enrollmentUid),
        )
    }

    private fun getRuleEnrollment(
        ou: String,
        program: String,
        enrollmentUid: String,
        attributeValues: List<RuleAttributeValue>? = null
    ): RuleEnrollment {
        val enrollment = d2.enrollment(enrollmentUid)

        return RuleEnrollment(
            enrollment = enrollment?.uid().orEmpty(),
            programName = d2.program(program)?.name()!!,
            incidentDate = (enrollment?.incidentDate() ?: Date()).toRuleEngineLocalDate(),
            enrollmentDate = (enrollment?.enrollmentDate() ?: Date()).toRuleEngineLocalDate(),
            status = RuleEnrollmentStatus.valueOf(
                enrollment?.status()?.name ?: EnrollmentStatus.ACTIVE.name
            ),
            organisationUnit = ou,
            organisationUnitCode =
                d2
                    .organisationUnit(ou)
                    ?.code().orEmpty(),
            attributeValues = attributeValues ?: queryAttributeValues(enrollmentUid),
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
            createdDate = event.created()!!.toRuleEngineInstant(),
        )
    }

    suspend fun evaluate(
        ou: String,
        program: String,
        event: String,
        enrollment: String? = null,
        dataValues: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        val ruleEngineContextData = ruleEngineContextData(
            ou = ou,
            program = program,
            enrollment = enrollment,
            event = event,
        )

        val ruleDataValues = dataValues.map {
            RuleDataValue(
                dataElement = it.key,
                value = it.value
            )
        }.filter {
            it.value.isNotEmpty()
        }

        return@withContext ruleEngine.evaluate(
            target = getRuleEvent(event, ruleDataValues),
            ruleEnrollment = ruleEngineContextData.ruleEnrollment,
            ruleEvents = ruleEngineContextData.ruleEvents,
            executionContext = ruleEngineContextData.ruleEngineContext,
        )
    }

    suspend fun evaluate(
        ou: String,
        program: String,
        enrollment: String = "",
        attributeValues: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        val ruleEngineContextData = ruleEngineContextData(ou, program, enrollment)

        val ruleAttributeValues = attributeValues.map {
            RuleAttributeValue(
                trackedEntityAttribute = it.key,
                value = it.value
            )
        }.filter {
            it.value.isNotEmpty()
        }

        return@withContext ruleEngine.evaluate(
            target = getRuleEnrollment(ou, program, enrollment, ruleAttributeValues),
            ruleEvents = ruleEngineContextData.ruleEvents,
            executionContext = ruleEngineContextData.ruleEngineContext,
        )
    }

    suspend fun evaluate(
        ou: String,
        program: String,
        enrollment: String = "",
    ) = withContext(Dispatchers.IO) {
        val ruleEngineContextData = ruleEngineContextData(ou, program, enrollment)

        return@withContext ruleEngine.evaluate(
            target = getRuleEnrollment(ou, program, enrollment),
            ruleEvents = ruleEngineContextData.ruleEvents,
            executionContext = ruleEngineContextData.ruleEngineContext,
        )
    }

    private companion object {
        const val OTHER_EVENTS_LIMIT = 10
    }
}
