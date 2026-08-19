package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.model.app_config.isEnabledAndConfigured
import org.saudigitus.semis.core.data.model.profile.AttendanceHistory
import org.saudigitus.semis.core.data.model.profile.AttendanceRecord
import org.saudigitus.semis.core.data.model.profile.attendanceStatusCounts
import org.saudigitus.semis.core.data.model.profile.ProfileAttribute
import org.saudigitus.semis.core.data.model.profile.ProfileMark
import org.saudigitus.semis.core.data.model.profile.SocioEconomicRecord
import org.saudigitus.semis.core.data.model.profile.SubjectPerformance
import org.saudigitus.semis.core.data.model.profile.TeiProfile
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.data.utils.Transformations
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
}
