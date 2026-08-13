package org.saudigitus.semis.attendance.ui.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataElement
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.attendance.ui.model.AttendanceStatus
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.utils.eventsWithTrackedDataValuesByDate
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.utils.UiDefaults.sectionWritingType
import org.saudigitus.semis.core.form.data.repository.FormRepository
import org.saudigitus.semis.core.utils.Constants

class AttendanceRepositoryImpl(
    private val d2: D2,
    private val appConfigRepository: AppConfigRepository,
    private val formRepository: FormRepository,
) : AttendanceRepository {
    override suspend fun saveAttendanceStatus(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
        attendanceStatus: AttendanceStatus
    ) = withContext(Dispatchers.IO) {
        val data = mutableListOf<Pair<String, String?>>()
        val dataElements = appConfigRepository.getAppConfig(program)
            ?.filters
            ?.dataElements
            ?.filterNotNull() ?: emptyList()
        val academicYear = appConfigRepository.getSchoolCalendar()
            ?.academicYear.orEmpty()

        if (dataElements.isNotEmpty() && !filterDetailsState.grade.isNullOrEmpty()
            && !filterDetailsState.section.isNullOrEmpty()
        ) {
            val grade = dataElements.find { it.code == Constants.GRADE }
            val section = dataElements.find { it.code in sectionWritingType }

            data.add(Pair(grade?.dataElement.orEmpty(), filterDetailsState.grade))
            data.add(Pair(section?.dataElement.orEmpty(), filterDetailsState.section))
        }

        data.add(Pair(academicYear, filterDetailsState.academicYear))
        data.add(Pair(attendanceStatus.dataElement.orEmpty(), attendanceStatus.value))

        formRepository.saveAttendanceStatus(
            event = attendanceStatus.event,
            orgUnit = orgUnit,
            program = attendanceStatus.program.orEmpty(),
            programStage = attendanceStatus.programStage.orEmpty(),
            data = data,
            eventDate = date
        )
    }

    override suspend fun getAttendanceStatus(program: String, date: String): AttendanceStatus? =
        withContext(Dispatchers.IO) {
            val attendanceStatus = appConfigRepository.getAppConfig(program)
                ?.attendance
                ?.attendanceStatus

            if (attendanceStatus == null) return@withContext null

            val dataElement = d2.dataElement(attendanceStatus.status.orEmpty())

            val event = d2.eventsWithTrackedDataValuesByDate(
                program = attendanceStatus.program.orEmpty(),
                stage = attendanceStatus.programStage.orEmpty(),
                date = date
            )

            val dataValue = event?.trackedEntityDataValues()
                ?.find { dv -> dv.dataElement() == dataElement?.uid() }

            AttendanceStatus(
                event = event?.uid(),
                program = attendanceStatus.program,
                programStage = attendanceStatus.programStage,
                dataElement = dataElement?.uid(),
                displayName = dataElement?.displayFormName(),
                value = dataValue?.value()
            )
        }
}