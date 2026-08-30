package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.model.app_config.isEnabledAndConfigured
import org.saudigitus.semis.core.data.model.app_config.Profile
import org.saudigitus.semis.core.data.model.app_config.ProfileComponent
import org.saudigitus.semis.core.data.model.profile.AttendanceHistory
import org.saudigitus.semis.core.data.model.profile.ConfiguredProfile
import org.saudigitus.semis.core.data.model.profile.ProfileIdentity
import org.saudigitus.semis.core.data.model.profile.ProfilePanel
import org.saudigitus.semis.core.data.model.profile.ProfilePanelKind
import org.saudigitus.semis.core.data.model.profile.ProfileRecord
import org.saudigitus.semis.core.data.model.profile.ProfileSection
import org.saudigitus.semis.core.data.model.profile.ProfileTabContent
import org.saudigitus.semis.core.data.model.profile.ProfileValue
import org.saudigitus.semis.core.data.model.profile.AttendanceRecord
import org.saudigitus.semis.core.data.model.profile.attendanceStatusCounts
import org.saudigitus.semis.core.data.model.profile.ProfileAttribute
import org.saudigitus.semis.core.data.model.profile.ProfileMark
import org.saudigitus.semis.core.data.model.profile.SocioEconomicRecord
import org.saudigitus.semis.core.data.model.profile.SubjectPerformance
import org.saudigitus.semis.core.data.model.profile.TeiProfile
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.data.utils.Transformations
import org.saudigitus.semis.core.utils.DateHelper
import java.util.Date
import javax.inject.Inject

class TeiProfileRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val appConfigRepository: AppConfigRepository,
    private val transformations: Transformations,
) : TeiProfileRepository {

    override suspend fun getProfile(
        teiUid: String,
        program: String,
        academicYear: String?,
    ): TeiProfile? = withContext(Dispatchers.IO) {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .byUid().eq(teiUid)
            .withTrackedEntityAttributeValues()
            .one()
            .blockingGet()
            ?: return@withContext null

        val enrollment = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)
            .byProgram().eq(program)
            .byDeleted().isFalse
            .one()
            .blockingGet()
        val learner = transformations.transform(tei, program, enrollment)
        val identity = learner.learnerIdentity()
        val config = appConfigRepository.getAppConfig(program)
        val range = academicYearRange(academicYear)

        TeiProfile(
            teiUid = teiUid,
            name = identity.name,
            systemId = identity.firstAttributeValue,
            identity = learner.attributeValues.map { (label, attribute) ->
                ProfileAttribute(label = label, value = attribute.value())
            },
            socioEconomics = socioEconomicRecords(teiUid, program, config),
            attendance = attendanceHistory(teiUid, program, config, range),
            performance = subjectPerformance(teiUid, program, config, range),
        )
    }

    private fun socioEconomicRecords(
        teiUid: String,
        program: String,
        config: SEMISConfigItem?,
    ): List<SocioEconomicRecord> {
        val programStage = config?.socioEconomics?.programStage
            ?.takeIf { it.isNotBlank() }
            ?: return emptyList()
        val labels = dataElementLabels(programStage)

        return events(teiUid, program, programStage)
            .map { event ->
                SocioEconomicRecord(
                    eventUid = event.uid(),
                    occurredAt = event.eventDate(),
                    orgUnitName = orgUnitName(event.organisationUnit()),
                    isActive = event.status() == EventStatus.ACTIVE,
                    details = event.trackedEntityDataValues().orEmpty().map { value ->
                        ProfileAttribute(
                            label = labels[value.dataElement()] ?: value.dataElement().orEmpty(),
                            value = value.value(),
                        )
                    },
                )
            }
            .sortedByDescending { it.occurredAt }
    }

    private fun attendanceHistory(
        teiUid: String,
        program: String,
        config: SEMISConfigItem?,
        range: ClosedRange<Date>?,
    ): AttendanceHistory {
        val attendance = config?.attendance ?: return AttendanceHistory()
        val programStage = attendance.programStage?.takeIf { it.isNotBlank() }
            ?: return AttendanceHistory()
        val statusDataElement = attendance.status.orEmpty()
        val reasonDataElement = attendance.absenceReason.orEmpty()
        val statusLabels = optionLabels(statusDataElement)
        val reasonLabels = optionLabels(reasonDataElement)

        val records = events(teiUid, program, programStage)
            .filter { it.eventDate().inRange(range) }
            .mapNotNull { event ->
                val statusCode = event.dataValue(statusDataElement)
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                val reasonCode = event.dataValue(reasonDataElement)?.takeIf { it.isNotBlank() }

                AttendanceRecord(
                    eventUid = event.uid(),
                    date = event.eventDate(),
                    statusCode = statusCode,
                    statusLabel = statusLabels[statusCode] ?: statusCode,
                    absenceReason = reasonCode?.let { reasonLabels[it] ?: it },
                )
            }
            .sortedByDescending { it.date }

        return AttendanceHistory(
            records = records,
            statusCounts = attendanceStatusCounts(
                statusOptions = attendance.statusOptions?.filterNotNull().orEmpty(),
                optionLabels = statusLabels,
                recordedStatusCodes = records.map { it.statusCode },
                derivePresent = attendance.attendanceStatus.isEnabledAndConfigured(),
            ),
        )
    }

    private fun subjectPerformance(
        teiUid: String,
        program: String,
        config: SEMISConfigItem?,
        range: ClosedRange<Date>?,
    ): List<SubjectPerformance> {
        val performance = config?.performance?.takeIf { it.enabled == true } ?: return emptyList()

        return performance.programStages
            ?.mapNotNull { stage -> stage?.programStage?.takeIf { it.isNotBlank() } }
            .orEmpty()
            .map { programStage ->
                val labels = dataElementLabels(programStage)
                val marks = events(teiUid, program, programStage)
                    .filter { it.eventDate().inRange(range) }
                    .flatMap { event ->
                        event.trackedEntityDataValues().orEmpty().mapNotNull { value ->
                            val raw = value.value()?.takeIf { it.isNotBlank() }
                                ?: return@mapNotNull null

                            ProfileMark(
                                eventUid = event.uid(),
                                date = event.eventDate(),
                                label = labels[value.dataElement()]
                                    ?: value.dataElement().orEmpty(),
                                value = raw.toDoubleOrNull(),
                                displayValue = raw,
                            )
                        }
                    }
                    .sortedByDescending { it.date }

                SubjectPerformance(
                    programStage = programStage,
                    subject = programStageName(programStage),
                    marks = marks,
                )
            }
    }

    private fun events(teiUid: String, program: String, programStage: String): List<Event> =
        d2.eventModule().events()
            .byTrackedEntityInstanceUids(listOf(teiUid))
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()

    private fun dataElementLabels(programStage: String): Map<String, String> =
        d2.programModule().programStageDataElements()
            .byProgramStage().eq(programStage)
            .blockingGet()
            .mapNotNull { it.dataElement()?.uid() }
            .distinct()
            .mapNotNull { uid ->
                d2.dataElementModule().dataElements().uid(uid).blockingGet()?.let { element ->
                    uid to (element.displayFormName() ?: element.displayName() ?: uid)
                }
            }
            .toMap()

    /** Display names of the options of [dataElement], keyed by option code. */
    private fun optionLabels(dataElement: String): Map<String, String> {
        if (dataElement.isBlank()) return emptyMap()

        val optionSet = d2.dataElementModule().dataElements()
            .uid(dataElement)
            .blockingGet()
            ?.optionSetUid()
            ?: return emptyMap()

        return d2.optionModule().options()
            .byOptionSetUid().eq(optionSet)
            .blockingGet()
            .mapNotNull { option ->
                option.code()?.let { code -> code to (option.displayName() ?: code) }
            }
            .toMap()
    }

    private fun programStageName(programStage: String): String =
        d2.programModule().programStages()
            .uid(programStage)
            .blockingGet()
            ?.displayName()
            .orEmpty()
            .ifBlank { programStage }

    private fun orgUnitName(orgUnit: String?): String =
        orgUnit?.let {
            d2.organisationUnitModule().organisationUnits().uid(it).blockingGet()?.displayName()
        }.orEmpty()

    /**
     * Start and end of the selected academic year, taken from the school calendar. Returns
     * null when the year cannot be resolved, which keeps the whole history visible instead
     * of silently emptying the dashboard.
     */
    private suspend fun academicYearRange(academicYear: String?): ClosedRange<Date>? {
        val year = academicYear?.takeIf { it.isNotBlank() } ?: return null
        val configured = appConfigRepository.getSchoolCalendar()?.schoolCalendar
            ?.filterNotNull()
            ?.firstOrNull { entry ->
                entry.academicYear?.let { it.label == year || it.code == year } == true
            }
            ?.academicYear
            ?: return null
        val start = parseDate(configured.startDate) ?: return null
        val end = parseDate(configured.endDate) ?: return null

        return start..end
    }

    private fun parseDate(value: String?): Date? = value
        ?.takeIf { it.isNotBlank() }
        ?.let { date -> runCatching { java.sql.Date.valueOf(date) as Date }.getOrNull() }

    private fun Date?.inRange(range: ClosedRange<Date>?): Boolean = when {
        range == null -> true
        this == null -> false
        else -> this in range
    }

    private fun Event.dataValue(dataElement: String): String? =
        trackedEntityDataValues()?.find { it.dataElement() == dataElement }?.value()

    override suspend fun getConfiguredProfile(
        teiUid: String,
        program: String,
    ): ConfiguredProfile? = withContext(Dispatchers.IO) {
        val profile = appConfigRepository.getAppConfig(program)?.profile
            ?: return@withContext null

        val attributes = attributeValues(teiUid)

        ConfiguredProfile(
            identity = identityOf(profile, attributes),
            tabs = profile.tabs
                ?.filterNotNull()
                ?.sortedBy { it.order ?: Int.MAX_VALUE }
                ?.mapNotNull { tab ->
                    val panels = tab.components
                        ?.filterNotNull()
                        ?.sortedBy { it.order ?: Int.MAX_VALUE }
                        ?.mapNotNull { component -> panelOf(component, teiUid, program, attributes) }
                        .orEmpty()

                    if (panels.isEmpty()) {
                        null
                    } else {
                        ProfileTabContent(
                            id = tab.id ?: tab.displayName.orEmpty(),
                            title = tab.displayName.orEmpty(),
                            panels = panels,
                        )
                    }
                }
                .orEmpty(),
        )
    }

    /**
     * Builds the header from the identity card configuration.
     *
     * Attributes with no value are left out of the joined text rather than contributing an empty
     * gap, which is what keeps a name from reading as a stray separator when a middle name is
     * missing.
     */
    private fun identityOf(
        profile: Profile,
        attributes: Map<String, String>,
    ): ProfileIdentity {
        val card = profile.identityCard

        fun joined(attributeUids: List<String?>?, separator: String?): String =
            attributeUids
                ?.filterNotNull()
                ?.mapNotNull { attributes[it]?.takeIf(String::isNotBlank) }
                ?.joinToString(separator ?: " ")
                .orEmpty()

        return ProfileIdentity(
            title = joined(card?.title?.attributes, card?.title?.separator),
            subtitle = joined(card?.subtitle?.attributes, card?.subtitle?.separator),
            photo = card?.photo?.attribute?.let { attributes[it] }?.takeIf(String::isNotBlank),
            badges = card?.badges
                ?.filterNotNull()
                ?.sortedBy { it.order ?: Int.MAX_VALUE }
                ?.mapNotNull { badge ->
                    // Only a badge naming an attribute can be resolved here. The other sources
                    // are derived elsewhere in the record and are left out rather than guessed.
                    badge.variable
                        ?.takeIf { badge.source == BADGE_SOURCE_ATTRIBUTE }
                        ?.let { attributes[it] }
                        ?.takeIf(String::isNotBlank)
                }
                .orEmpty(),
        )
    }

    /**
     * Reads one panel, or nothing when this version cannot draw what it asks for.
     *
     * A panel type the app does not know is skipped instead of failing the tab, so that a
     * deployment configuring ahead of the app still gets the rest of the page.
     */
    private fun panelOf(
        component: ProfileComponent,
        teiUid: String,
        program: String,
        attributes: Map<String, String>,
    ): ProfilePanel? {
        val title = component.displayName.orEmpty()
        val editable = component.editable == true

        return when (component.type) {
            COMPONENT_TEI_FORM -> ProfilePanel(
                title = title,
                kind = ProfilePanelKind.CARDS,
                editable = editable,
                target = null,
                records = listOf(
                    ProfileRecord(id = teiUid, sections = attributeSections(program, attributes)),
                ),
            )

            COMPONENT_EVENT_CARDS, COMPONENT_EVENT_TABLE -> {
                val stage = component.details?.programStage?.takeIf { it.isNotBlank() }
                    ?: return null

                ProfilePanel(
                    title = title.ifBlank { programStageName(stage) },
                    kind = if (component.type == COMPONENT_EVENT_TABLE) {
                        ProfilePanelKind.TABLE
                    } else {
                        ProfilePanelKind.CARDS
                    },
                    editable = editable,
                    target = stage,
                    records = eventRecords(teiUid, program, stage, component.details.pageSize),
                )
            }

            else -> null
        }
    }

    /**
     * The events of [programStage], newest first and limited to what the panel asked for.
     *
     * A history can hold thousands of records, so the configured page size is a limit and not a
     * hint: reading them all to show ten would make the page wait on records nobody sees.
     */
    private fun eventRecords(
        teiUid: String,
        program: String,
        programStage: String,
        pageSize: Int?,
    ): List<ProfileRecord> {
        val labels = dataElementLabels(programStage)
        val optionLabels = labels.keys.associateWith { optionLabels(it) }

        return events(teiUid, program, programStage)
            .sortedByDescending { it.eventDate() }
            .let { events -> pageSize?.takeIf { it > 0 }?.let(events::take) ?: events }
            .map { event ->
                val values = event.trackedEntityDataValues()
                    ?.mapNotNull { dataValue ->
                        val uid = dataValue.dataElement() ?: return@mapNotNull null
                        val raw = dataValue.value()?.takeIf(String::isNotBlank)
                            ?: return@mapNotNull null

                        uid to ProfileValue(
                            label = labels[uid] ?: uid,
                            value = optionLabels[uid]?.get(raw) ?: raw,
                        )
                    }
                    ?.toMap()
                    .orEmpty()

                ProfileRecord(
                    id = event.uid(),
                    heading = event.eventDate()?.let { DateHelper.formatDateWithWeekDay(it.time) },
                    sections = grouped(sections = stageSections(programStage), values = values),
                )
            }
    }

    /**
     * The sections of [programStage], each with the data elements it holds in the order the
     * section puts them in.
     *
     * The order comes from the section itself and not from the stage: the stage numbers its data
     * elements across all of its sections at once, so reading that instead would show a section
     * shuffled.
     */
    private fun stageSections(programStage: String): List<Pair<String?, List<String>>> =
        d2.programModule().programStageSections()
            .byProgramStageUid().eq(programStage)
            .withDataElements()
            .blockingGet()
            .sortedBy { it.sortOrder() ?: Int.MAX_VALUE }
            .map { section ->
                section.displayName() to section.dataElements().orEmpty().map { it.uid() }
            }

    /** The attributes of [program], grouped as the program groups them. */
    private fun attributeSections(
        program: String,
        attributes: Map<String, String>,
    ): List<ProfileSection> {
        val labels = attributeLabels(program)
        val values = labels.mapNotNull { (uid, label) ->
            attributes[uid]
                ?.takeIf(String::isNotBlank)
                ?.let { uid to ProfileValue(label = label, value = it) }
        }.toMap()

        val sections = d2.programModule().programSections()
            .byProgramUid().eq(program)
            .withAttributes()
            .blockingGet()
            .sortedBy { it.sortOrder() ?: Int.MAX_VALUE }
            .map { section ->
                section.displayName() to section.attributes().orEmpty().map { it.uid() }
            }

        return grouped(sections = sections, values = values)
    }

    /**
     * Lays [values] out in [sections], keeping the order each section states.
     *
     * What no section claims is not dropped: it is gathered into one untitled group at the end,
     * so a value the configuration forgot to place still reaches whoever is reading the record.
     * An empty section is left out rather than shown as a heading over nothing.
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
            val sectionValues = uids.mapNotNull { values[it] }
            sectionValues.takeIf { it.isNotEmpty() }
                ?.let { ProfileSection(title = title, values = it) }
        }
        val unclaimed = values.filterKeys { it !in claimed }.values.toList()

        return if (unclaimed.isEmpty()) {
            grouped
        } else {
            grouped + ProfileSection(title = null, values = unclaimed)
        }
    }

    /** The attribute values of the person, keyed by attribute uid. */
    private fun attributeValues(teiUid: String): Map<String, String> =
        d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityInstance().eq(teiUid)
            .blockingGet()
            .mapNotNull { value ->
                val uid = value.trackedEntityAttribute() ?: return@mapNotNull null
                val raw = value.value()?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                uid to raw
            }
            .toMap()

    /** The attributes the program shows, in the order it configures, keyed by uid. */
    private fun attributeLabels(program: String): Map<String, String> =
        d2.programModule().programTrackedEntityAttributes()
            .byProgram().eq(program)
            .byDisplayInList().isTrue
            .blockingGet()
            .sortedBy { it.sortOrder() ?: Int.MAX_VALUE }
            .mapNotNull { programAttribute ->
                val uid = programAttribute.trackedEntityAttribute()?.uid()
                    ?: return@mapNotNull null
                val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                    .uid(uid)
                    .blockingGet()

                uid to (attribute?.displayFormName() ?: attribute?.displayName() ?: uid)
            }
            .toMap()

    private companion object {
        const val BADGE_SOURCE_ATTRIBUTE = "ATTRIBUTE"
        const val COMPONENT_TEI_FORM = "TEI_FORM"
        const val COMPONENT_EVENT_CARDS = "EVENT_CARDS"
        const val COMPONENT_EVENT_TABLE = "EVENT_TABLE"
    }
}
