package org.saudigitus.semis.attendance.ui.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.saudigitus.semis.attendance.ui.model.AttendanceStatus
import org.saudigitus.semis.attendance.ui.model.AttendanceStatusCandidate
import org.saudigitus.semis.attendance.ui.model.chooseAttendanceStatus
import org.saudigitus.semis.attendance.ui.model.attendanceStatusSummaryValues
import org.saudigitus.semis.attendance.ui.model.attendanceSummaryCounts
import org.saudigitus.semis.attendance.ui.model.isPresent
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

        writeStatusValues(
            attendanceStatus = attendanceStatus,
            program = program,
            filterDetailsState = filterDetailsState,
            totalLearners = totalLearners,
            attendanceEvents = attendanceEvents,
        )

        d2.eventModule().events().uid(attendanceStatus.event)
            .setStatus(EventStatus.COMPLETED)

        attendanceStatus.copy(status = EventStatus.COMPLETED)
    }

    override suspend fun updateAttendanceStatusSummary(
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
        ) ?: return@withContext null

        writeStatusValues(
            attendanceStatus = attendanceStatus,
            program = program,
            filterDetailsState = filterDetailsState,
            totalLearners = totalLearners,
            attendanceEvents = attendanceEvents,
        )

        attendanceStatus
    }

    private suspend fun writeStatusValues(
        attendanceStatus: AttendanceStatus,
        program: String,
        filterDetailsState: FilterDetailsState,
        totalLearners: Int,
        attendanceEvents: List<AttendanceEventWithDecorator>,
    ) {
        val summary = summaryValues(
            program = program,
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
        val candidates = d2.eventModule().events()
            .byOrganisationUnitUid().eq(orgUnit)
            .byProgramUid().eq(eventProgram)
            .byProgramStageUid().eq(programStage)
            .byEventDate().eq(Date.valueOf(date))
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()
            .map { event ->
                AttendanceStatusCandidate(
                    event = event,
                    status = event.status(),
                    lastUpdated = event.lastUpdated()?.time ?: 0L,
                    contextValues = event.trackedEntityDataValues().orEmpty().mapNotNull {
                        val dataElement = it.dataElement() ?: return@mapNotNull null
                        val value = it.value() ?: return@mapNotNull null
                        dataElement to value
                    },
                )
            }

        val event = chooseAttendanceStatus(candidates, contextValues)?.event
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
        totalLearners: Int,
        attendanceEvents: List<AttendanceEventWithDecorator>,
    ): List<Pair<String, String>> {
        val attendance = appConfigRepository.getAppConfig(program)?.attendance
        val configuredStatuses = attendance?.statusOptions
            ?.filterNotNull()
            .orEmpty()
        val summaryCounts = attendanceSummaryCounts(
            totalLearners = totalLearners,
            configuredStatusCodes = configuredStatuses
                .filterNot { it.isPresent() }
                .mapNotNull { it.code },
            attendanceValues = attendanceEvents.mapNotNull { it.event?.value },
        )
        val attendanceStatusConfig = attendance?.attendanceStatus
            ?: error("Attendance status configuration is missing")
        val totalRecordsDataElement = attendanceStatusConfig.totalRecords
            ?.takeIf { it.isNotBlank() }
            ?: error("Attendance status total records data element is missing")

        return attendanceStatusSummaryValues(
            statusOptions = configuredStatuses,
            totalRecordsDataElement = totalRecordsDataElement,
            counts = summaryCounts,
        )
    }

    private fun contextValue(dataElement: String?, value: String?): Pair<String, String>? =
        if (dataElement.isNullOrBlank() || value.isNullOrBlank()) {
            null
        } else {
            dataElement to value
        }

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
}
