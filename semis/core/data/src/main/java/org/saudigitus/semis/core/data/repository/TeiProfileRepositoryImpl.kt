package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.saudigitus.semis.core.data.R
import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.model.person.PersonTag
import org.saudigitus.semis.core.data.model.person.identity
import org.saudigitus.semis.core.data.model.profile.PersonProfile
import org.saudigitus.semis.core.data.model.profile.ProfileFigure
import org.saudigitus.semis.core.data.model.profile.ProfileFigureKind
import org.saudigitus.semis.core.data.model.profile.ProfilePanel
import org.saudigitus.semis.core.data.model.profile.ProfileRecord
import org.saudigitus.semis.core.data.model.profile.ProfileSection
import org.saudigitus.semis.core.data.model.profile.ProfileTabContent
import org.saudigitus.semis.core.data.model.profile.ProfileTabKind
import org.saudigitus.semis.core.data.model.profile.ProfileValue
import org.saudigitus.semis.core.data.utils.Transformations
import org.saudigitus.semis.core.data.utils.ValueDisplay
import org.saudigitus.semis.core.data.utils.stageDataElements
import org.saudigitus.semis.core.utils.DateHelper
import java.util.Locale
import javax.inject.Inject

/**
 * Builds the page of a person by the rules the web shares, from the program metadata alone.
 *
 * - The header is the shared identity, plus the final decision of the year as a tag.
 * - Personal details holds every attribute, the registration record and the socio-economics
 *   record. Attendance, Performance, Final result and Transfer follow, each only when its module
 *   is enabled for the program.
 * - Every record belongs to the enrollment of the selected year and shows the data elements marked
 *   "display in reports"; with none marked, it shows its school and date.
 * - The summary is counted from those records, so it needs neither analytics nor a connection.
 */
class TeiProfileRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val appConfigRepository: AppConfigRepository,
    private val transformations: Transformations,
    private val valueDisplay: ValueDisplay,
    private val resourceManager: ResourceManager,
) : TeiProfileRepository {

    override suspend fun getProfile(
        teiUid: String,
        program: String,
        academicYear: String?,
    ): PersonProfile? = withContext(Dispatchers.IO) {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .byUid().eq(teiUid)
            .withTrackedEntityAttributeValues()
            .one()
            .blockingGet()
            ?: return@withContext null

        val config = appConfigRepository.getAppConfig(program)
        val enrollment = enrollmentOfYear(teiUid, program, config, academicYear)
        val events = enrollment?.let { eventsOf(it.uid()) }.orEmpty()
        val identity = transformations.transform(tei, program, enrollment).identity()

        PersonProfile(
            identity = identity.copy(
                tags = identity.tags + listOfNotNull(finalDecisionTag(config, events)),
            ),
            academicYear = academicYear?.takeIf { it.isNotBlank() },
            summary = summary(config, events),
            tabs = tabs(tei, program, config, events),
        )
    }

    private fun tabs(
        tei: TrackedEntityInstance,
        program: String,
        config: SEMISConfigItem?,
        events: List<Event>,
    ): List<ProfileTabContent> = buildList {
        add(
            ProfileTabContent(
                kind = ProfileTabKind.PERSONAL_DETAILS,
                panels = listOfNotNull(
                    attributesPanel(tei, program),
                    config?.registration?.programStage.stage()?.let { stagePanel(it, events) },
                    config?.socioEconomics?.programStage.stage()?.let { stagePanel(it, events) },
                ),
            ),
        )

        config?.attendance
            ?.takeIf { it.enabled == true }
            ?.programStage.stage()
            ?.let { add(ProfileTabContent(ProfileTabKind.ATTENDANCE, listOf(stagePanel(it, events)))) }

        config?.performance
            ?.takeIf { it.enabled == true }
            ?.let {
                val panels = performanceStages(config).map { stagePanel(it, events) }
                if (panels.isNotEmpty()) add(ProfileTabContent(ProfileTabKind.PERFORMANCE, panels))
            }

        config?.finalResult
            ?.takeIf { it.enabled == true }
            ?.programStage.stage()
            ?.let { add(ProfileTabContent(ProfileTabKind.FINAL_RESULT, listOf(stagePanel(it, events)))) }

        config?.transfer
            ?.takeIf { it.enabled == true }
            ?.programStage.stage()
            ?.let {
                val panel = stagePanel(it, events, showsOrigin = true)
                add(ProfileTabContent(ProfileTabKind.TRANSFER, listOf(panel)))
            }
    }

    /**
     * The figures at the top of the page: the absences of the year when attendance is enabled, and
     * the average of each performance stage that holds marks.
     */
    private fun summary(config: SEMISConfigItem?, events: List<Event>): List<ProfileFigure> =
        buildList {
            val attendance = config?.attendance?.takeIf { it.enabled == true }
            val attendanceStage = attendance?.programStage.stage()
            val statusElement = attendance?.status.stage()
            if (attendanceStage != null && statusElement != null) {
                // Being present leaves no record, so every status the deployment lists is one the
                // learner was not present with; absent is what a teacher counts.
                val options = attendance?.statusOptions.orEmpty().filterNotNull()
                val absentCodes = options.filter { it.key == ABSENT_KEY }
                    .ifEmpty { options }
                    .mapNotNull { it.code?.takeIf(String::isNotBlank) }
                    .toSet()
                val absences = events.count { event ->
                    event.programStage() == attendanceStage &&
                        event.dataValue(statusElement) in absentCodes
                }
                add(ProfileFigure(kind = ProfileFigureKind.ABSENCES, value = absences.toString()))
            }

            if (config?.performance?.enabled == true) {
                performanceStages(config).forEach { stage ->
                    average(stage, events)?.let { average ->
                        add(
                            ProfileFigure(
                                kind = ProfileFigureKind.AVERAGE,
                                value = String.format(Locale.getDefault(), "%.1f", average),
                                subject = programStageName(stage),
                            ),
                        )
                    }
                }
            }
        }

    /** The mean of the numeric marks recorded in [stage], or null when it holds none. */
    private fun average(stage: String, events: List<Event>): Double? {
        val numeric = d2.stageDataElements(stage)
            .filter { it.valueType?.isNumeric == true && it.optionSet == null }
            .map { it.uid }
            .toSet()
        val marks = events
            .filter { it.programStage() == stage }
            .flatMap { event ->
                event.trackedEntityDataValues().orEmpty()
                    .filter { it.dataElement() in numeric }
                    .mapNotNull { it.value()?.toDoubleOrNull() }
            }

        return marks.takeIf { it.isNotEmpty() }?.average()
    }

    /** The final decision of the year, in the colour of its option when that option has one. */
    private fun finalDecisionTag(config: SEMISConfigItem?, events: List<Event>): PersonTag? {
        val finalResult = config?.finalResult?.takeIf { it.enabled == true } ?: return null
        val stage = finalResult.programStage.stage() ?: return null
        val statusElement = finalResult.status.stage() ?: return null
        val code = events
            .filter { it.programStage() == stage }
            .maxByOrNull { it.eventDate()?.time ?: 0L }
            ?.dataValue(statusElement)
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val optionSet = d2.dataElementModule().dataElements().uid(statusElement).blockingGet()
            ?.optionSetUid()
        val option = optionSet?.let {
            d2.optionModule().options()
                .byOptionSetUid().eq(it)
                .byCode().eq(code)
                .one()
                .blockingGet()
        }

        return PersonTag(
            label = option?.displayName() ?: code,
            color = option?.style()?.color()?.takeIf { it.isNotBlank() },
        )
    }

    /**
     * The enrollment of [academicYear], recognised by the academic year its registration record
     * carries.
     *
     * When the year or the way to recognise it is unknown, the latest enrollment stands in, so the
     * page is not emptied by a missing setting. When the year is known and no enrollment carries
     * it, there is none: the page then says that nothing was recorded for that year.
     */
    private suspend fun enrollmentOfYear(
        teiUid: String,
        program: String,
        config: SEMISConfigItem?,
        academicYear: String?,
    ): Enrollment? {
        val enrollments = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)
            .byProgram().eq(program)
            .byDeleted().isFalse
            .blockingGet()
        val latest = enrollments.maxByOrNull { it.enrollmentDate()?.time ?: 0L }

        val year = academicYear?.takeIf { it.isNotBlank() } ?: return latest
        val registrationStage = config?.registration?.programStage.stage() ?: return latest
        val calendar = appConfigRepository.getSchoolCalendar()
        val yearElement = calendar?.academicYear.stage() ?: return latest
        // The filter may hold the label or the code of the year; the record holds the code.
        val yearValues = calendar?.schoolCalendar.orEmpty()
            .filterNotNull()
            .mapNotNull { it.academicYear }
            .filter { it.code == year || it.label == year }
            .flatMap { listOfNotNull(it.code, it.label) }
            .toSet() + year

        return enrollments.firstOrNull { enrollment ->
            eventsOf(enrollment.uid()).any { event ->
                event.programStage() == registrationStage &&
                    event.dataValue(yearElement) in yearValues
            }
        }
    }

    /** Every event of [enrollment], newest first. */
    private fun eventsOf(enrollment: String): List<Event> =
        d2.eventModule().events()
            .byEnrollmentUid().eq(enrollment)
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()
            .sortedByDescending { it.eventDate()?.time ?: 0L }

    /** The stages of the performance module, in the order the deployment lists them. */
    private fun performanceStages(config: SEMISConfigItem): List<String> =
        config.performance?.programStages.orEmpty()
            .mapNotNull { it?.programStage.stage() }
            .distinct()

    /**
     * Every attribute of the person, grouped as the program groups them. A photo or a file is
     * left out: the photo is already the avatar, and a file path tells the reader nothing.
     */
    private fun attributesPanel(tei: TrackedEntityInstance, program: String): ProfilePanel {
        val stored = tei.trackedEntityAttributeValues().orEmpty()
            .associateBy { it.trackedEntityAttribute() }
        val values = d2.programModule().programTrackedEntityAttributes()
            .byProgram().eq(program)
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()
            .mapNotNull { it.trackedEntityAttribute()?.uid() }
            .mapNotNull { uid ->
                val attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(uid)
                    .blockingGet()
                    ?.takeUnless { it.valueType() in FILE_VALUE_TYPES }
                    ?: return@mapNotNull null
                val shown = valueDisplay.of(
                    stored[uid]?.value(),
                    attribute.valueType(),
                    attribute.optionSet()?.uid(),
                ) ?: return@mapNotNull null

                uid to ProfileValue(label = attribute.formLabel(), value = shown)
            }
            .toMap()

        val sections = d2.programModule().programSections()
            .byProgramUid().eq(program)
            .withAttributes()
            .blockingGet()
            .sortedBy { it.sortOrder() ?: Int.MAX_VALUE }
            .map { section -> section.displayName() to section.attributes().orEmpty().map { it.uid() } }

        val title = tei.trackedEntityType()?.let {
            d2.trackedEntityModule().trackedEntityTypes().uid(it).blockingGet()?.displayName()
        }.orEmpty()

        return ProfilePanel(
            title = title,
            target = null,
            records = listOf(ProfileRecord(id = tei.uid(), sections = grouped(sections, values))),
        )
    }

    /**
     * The records of [stage], newest first, each showing the data elements marked "display in
     * reports", or its school and date when the stage marks none.
     *
     * A transfer request also shows its origin when [showsOrigin] is set, whatever the stage
     * marks, as the transfer lists do: it is the school the request was raised in, read from the
     * event itself and never from a data element or the configuration.
     */
    private fun stagePanel(
        stage: String,
        events: List<Event>,
        showsOrigin: Boolean = false,
    ): ProfilePanel {
        val reported = d2.stageDataElements(stage).filter { it.displayInReports }
        val title = programStageName(stage)
        // A section named like its stage would only repeat the card title above it.
        val sections = stageSections(stage).map { (name, uids) -> name.takeUnless { it == title } to uids }

        return ProfilePanel(
            title = title,
            target = stage,
            records = events.filter { it.programStage() == stage }.map { event ->
                val heading = event.eventDate()?.let { DateHelper.formatDateWithWeekDay(it.time) }
                val record = if (reported.isEmpty()) {
                    listOf(
                        ProfileSection(
                            title = null,
                            values = listOf(
                                ProfileValue(
                                    label = resourceManager.getString(R.string.profile_school),
                                    value = orgUnitName(event.organisationUnit()),
                                ),
                            ),
                        ),
                    )
                } else {
                    val values = reported.mapNotNull { element ->
                        valueDisplay.of(event.dataValue(element.uid), element.valueType, element.optionSet)
                            ?.let { element.uid to ProfileValue(label = element.label, value = it) }
                    }.toMap()
                    grouped(sections, if (showsOrigin) values.withOrigin(event) else values)
                }

                ProfileRecord(id = event.uid(), heading = heading, sections = record)
            },
        )
    }

    /**
     * The sections of [stage], each with the data elements it holds in the order the section
     * puts them in.
     */
    private fun stageSections(stage: String): List<Pair<String?, List<String>>> =
        d2.programModule().programStageSections()
            .byProgramStageUid().eq(stage)
            .withDataElements()
            .blockingGet()
            .sortedBy { it.sortOrder() ?: Int.MAX_VALUE }
            .map { section -> section.displayName() to section.dataElements().orEmpty().map { it.uid() } }

    /**
     * Lays [values] out in [sections], keeping the order each section states.
     *
     * What no section claims is not dropped: it is gathered into one untitled group at the end. An
     * empty section is left out rather than shown as a heading over nothing.
     */
    private fun grouped(
        sections: List<Pair<String?, List<String>>>,
        values: Map<String, ProfileValue>,
    ): List<ProfileSection> {
        if (sections.isEmpty()) {
            return listOf(ProfileSection(title = null, values = values.values.toList()))
                .filter { it.values.isNotEmpty() }
        }

        val claimed = sections.flatMap { (_, uids) -> uids }.toSet()
        val grouped = sections.mapNotNull { (title, uids) ->
            uids.mapNotNull { values[it] }
                .takeIf { it.isNotEmpty() }
                ?.let { ProfileSection(title = title, values = it) }
        }
        val unclaimed = values.filterKeys { it !in claimed }.values.toList()

        return if (unclaimed.isEmpty()) grouped else grouped + ProfileSection(title = null, values = unclaimed)
    }

    /** [values] with the origin of the request after everything else. */
    private fun Map<String, ProfileValue>.withOrigin(event: Event): Map<String, ProfileValue> {
        val school = orgUnitName(event.organisationUnit()).ifBlank { return this }

        return this + (
            ORIGIN_KEY to ProfileValue(
                label = resourceManager.getString(R.string.transfer_origin_school),
                value = school,
            )
            )
    }

    private fun programStageName(stage: String): String =
        d2.programModule().programStages().uid(stage).blockingGet()?.displayName()
            .orEmpty()
            .ifBlank { stage }

    private fun orgUnitName(orgUnit: String?): String =
        orgUnit?.let {
            d2.organisationUnitModule().organisationUnits().uid(it).blockingGet()?.displayName()
        }.orEmpty()

    private fun TrackedEntityAttribute.formLabel(): String =
        displayFormName()?.takeIf { it.isNotBlank() } ?: displayName().orEmpty()

    private fun Event.dataValue(dataElement: String): String? =
        trackedEntityDataValues()?.find { it.dataElement() == dataElement }?.value()

    /** A configured uid, or null when the setting is missing or blank. */
    private fun String?.stage(): String? = this?.takeIf { it.isNotBlank() }

    private companion object {
        const val ABSENT_KEY = "absent"
        const val ORIGIN_KEY = "origin"
        val FILE_VALUE_TYPES = setOf(ValueType.IMAGE, ValueType.FILE_RESOURCE)
    }
}
