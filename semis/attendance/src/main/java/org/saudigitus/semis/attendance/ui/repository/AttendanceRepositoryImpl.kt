package org.saudigitus.semis.attendance.ui.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.saudigitus.semis.attendance.ui.model.AttendanceStatus
import org.saudigitus.semis.attendance.ui.model.attendanceSummaryCounts
import org.saudigitus.semis.core.data.model.app_config.StatusOption
import org.saudigitus.semis.core.data.model.app_config.isEnabledAndConfigured
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.utils.UiDefaults.sectionWritingType
import org.saudigitus.semis.core.utils.Constants
import java.sql.Date

class AttendanceRepositoryImpl(
    private val d2: D2,
    private val appConfigRepository: AppConfigRepository,
) : AttendanceRepository {

    override suspend fun createAttendanceStatus(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
    ): AttendanceStatus? = withContext(Dispatchers.IO) {
        getAttendanceStatus(orgUnit, program, date, filterDetailsState)?.let {
            return@withContext it
        }

        val config = attendanceStatusConfig(program) ?: return@withContext null
        val eventProgram = config.program.orEmpty()
        val programStage = config.programStage.orEmpty()

        if (eventProgram.isEmpty() || programStage.isEmpty()) return@withContext null

        val event = d2.eventModule().events().blockingAdd(
            EventCreateProjection.builder()
                .organisationUnit(orgUnit)
                .program(eventProgram)
                .programStage(programStage)
                .attributeOptionCombo(defaultAttributeOptionCombo())
                .build()
        )

        contextValues(program, filterDetailsState).forEach { (dataElement, value) ->
            d2.trackedEntityModule().trackedEntityDataValues()
                .value(event, dataElement)
                .blockingSet(value)
        }

        d2.eventModule().events().uid(event).apply {
            setEventDate(Date.valueOf(date))
            setStatus(EventStatus.ACTIVE)
        }

        AttendanceStatus(
            event = event,
            program = eventProgram,
            programStage = programStage,
            status = EventStatus.ACTIVE,
        )
    }

    override suspend fun completeAttendanceStatus(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
        totalLearners: Int,
        attendanceEvents: List<AttendanceEventWithDecorator>,
    ): AttendanceStatus? = withContext(Dispatchers.IO) {
        val attendanceStatus = getAttendanceStatus(
            orgUnit,
            program,
            date,
            filterDetailsState,
        ) ?: createAttendanceStatus(
            orgUnit,
            program,
            date,
            filterDetailsState,
        ) ?: return@withContext null

        val summary = summaryValues(
            program = program,
            programStage = attendanceStatus.programStage.orEmpty(),
            totalLearners = totalLearners,
            attendanceEvents = attendanceEvents,
        )

        (contextValues(program, filterDetailsState) + summary)
            .distinctBy { it.first }
            .forEach { (dataElement, value) ->
                d2.trackedEntityModule().trackedEntityDataValues()
                    .value(attendanceStatus.event, dataElement)
                    .blockingSet(value)
            }

        d2.eventModule().events().uid(attendanceStatus.event)
            .setStatus(EventStatus.COMPLETED)

        attendanceStatus.copy(status = EventStatus.COMPLETED)
    }

    override suspend fun getAttendanceStatus(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
    ): AttendanceStatus? = withContext(Dispatchers.IO) {
        val config = attendanceStatusConfig(program) ?: return@withContext null
        val eventProgram = config.program.orEmpty()
        val programStage = config.programStage.orEmpty()

        if (eventProgram.isEmpty() || programStage.isEmpty()) return@withContext null

        val contextValues = contextValues(program, filterDetailsState)
        val event = d2.eventModule().events()
            .byOrganisationUnitUid().eq(orgUnit)
            .byProgramUid().eq(eventProgram)
            .byProgramStageUid().eq(programStage)
            .byEventDate().eq(Date.valueOf(date))
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()
            .firstOrNull { it.matches(contextValues) }
            ?: return@withContext null

        AttendanceStatus(
            event = event.uid(),
            program = eventProgram,
            programStage = programStage,
            status = event.status(),
        )
    }

    private suspend fun contextValues(
        program: String,
        filterDetailsState: FilterDetailsState,
    ): List<Pair<String, String>> {
        val appConfig = appConfigRepository.getAppConfig(program)
        val filterDataElements = appConfig?.filters?.dataElements
            ?.filterNotNull()
            .orEmpty()
        val academicYear = appConfigRepository.getSchoolCalendar()?.academicYear
        val grade = filterDataElements.find { it.code == Constants.GRADE }?.dataElement
        val section = filterDataElements.find { it.code in sectionWritingType }?.dataElement

        return listOfNotNull(
            contextValue(academicYear, filterDetailsState.academicYear),
            contextValue(grade, filterDetailsState.grade),
            contextValue(section, filterDetailsState.section),
        )
    }

    private suspend fun summaryValues(
        program: String,
        programStage: String,
        totalLearners: Int,
        attendanceEvents: List<AttendanceEventWithDecorator>,
    ): List<Pair<String, String>> {
        val attendance = appConfigRepository.getAppConfig(program)?.attendance
        val configuredStatuses = attendance?.statusOptions
            ?.filterNotNull()
            .orEmpty()
        val nonPresentStatuses = configuredStatuses
            .filterNot { it.isPresent() }
            .distinctBy { it.code }
        val summaryCounts = attendanceSummaryCounts(
            totalLearners = totalLearners,
            configuredStatusCodes = nonPresentStatuses.mapNotNull { it.code },
            attendanceValues = attendanceEvents.mapNotNull { it.event?.value },
        )
        val stageDataElements = programStageDataElements(programStage)
        val values = linkedMapOf<String, String>()
        val attendanceStatusConfig = attendance?.attendanceStatus
            ?: error("Attendance status configuration is missing")
        val totalAbsencesDataElement = attendanceStatusConfig.totalAbsences
            ?.takeIf { it.isNotBlank() }
            ?: error("Attendance status total absences data element is missing")
        val totalRecordsDataElement = attendanceStatusConfig.totalRecords
            ?.takeIf { it.isNotBlank() }
            ?: error("Attendance status total records data element is missing")

        values[totalAbsencesDataElement] = summaryCounts.totalAbsences.toString()
        values[totalRecordsDataElement] = summaryCounts.totalLearners.toString()

        nonPresentStatuses.forEach { status ->
            val dataElement = resolveDataElement(
                stageDataElements,
                listOfNotNull(status.configKey, status.key, status.code),
            )
            dataElement?.let {
                values[it.uid()] = (summaryCounts.statusCounts[status.code] ?: 0).toString()
            }
        }

        val presentStatus = configuredStatuses.firstOrNull { it.isPresent() }
        resolveDataElement(
            stageDataElements,
            listOfNotNull(
                presentStatus?.configKey,
                presentStatus?.key,
                presentStatus?.code,
                PRESENT_KEY,
            ),
        )?.let {
            values[it.uid()] = summaryCounts.presentLearners.toString()
        }

        return values.map { it.key to it.value }
    }

    private fun programStageDataElements(programStage: String): List<DataElement> =
        d2.programModule().programStageDataElements()
            .byProgramStage().eq(programStage)
            .blockingGet()
            .mapNotNull { it.dataElement()?.uid() }
            .distinct()
            .mapNotNull { d2.dataElementModule().dataElements().uid(it).blockingGet() }

    private fun resolveDataElement(
        dataElements: List<DataElement>,
        identifiers: List<String>,
        minimumPartialLength: Int = 1,
    ): DataElement? {
        val normalizedIdentifiers = identifiers
            .map(::normalize)
            .filter { it.isNotEmpty() }

        return dataElements.firstOrNull { dataElement ->
            dataElement.identifiers().any { it in normalizedIdentifiers }
        } ?: dataElements.firstOrNull { dataElement ->
            dataElement.identifiers().any { metadataIdentifier ->
                normalizedIdentifiers
                    .filter { it.length >= minimumPartialLength }
                    .any { identifier ->
                        metadataIdentifier.contains(identifier) || identifier.contains(
                            metadataIdentifier
                        )
                    }
            }
        }
    }

    private fun DataElement.identifiers() = listOfNotNull(
        uid(),
        code(),
        displayName(),
        displayFormName(),
    ).map(::normalize)

    private fun Event.matches(contextValues: List<Pair<String, String>>) =
        contextValues.all { (dataElement, value) ->
            trackedEntityDataValues().orEmpty().any {
                it.dataElement() == dataElement && it.value() == value
            }
        }

    private fun contextValue(dataElement: String?, value: String?): Pair<String, String>? =
        if (dataElement.isNullOrBlank() || value.isNullOrBlank()) {
            null
        } else {
            dataElement to value
        }

    private fun StatusOption.isPresent() =
        key.equals(PRESENT_KEY, ignoreCase = true) ||
            code.equals(PRESENT_KEY, ignoreCase = true)

    private fun normalize(value: String) = value
        .lowercase()
        .filter(Char::isLetterOrDigit)

    private suspend fun attendanceStatusConfig(program: String) =
        appConfigRepository.getAppConfig(program)
            ?.attendance
            ?.attendanceStatus
            ?.takeIf { it.isEnabledAndConfigured() }

    private fun defaultAttributeOptionCombo() =
        d2.categoryModule().categoryOptionCombos()
            .byDisplayName().eq(Constants.DEFAULT)
            .one()
            .blockingGet()
            ?.uid()

    private companion object {
        const val PRESENT_KEY = "present"

    }
}
