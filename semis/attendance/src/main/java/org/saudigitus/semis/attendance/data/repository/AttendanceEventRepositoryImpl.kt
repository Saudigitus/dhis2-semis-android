package org.saudigitus.semis.attendance.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.saudigitus.semis.attendance.data.AttendanceTransformation
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.EventRepository
import javax.inject.Inject

class AttendanceEventRepositoryImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val appConfigRepository: AppConfigRepository,
    private val transformations: AttendanceTransformation,
) : AttendanceEventRepository {
    override suspend fun save() {
        TODO("Not yet implemented")
    }

    override suspend fun getAttendanceEvent(
        teiUids: List<String>,
        program: String,
        programStage: String,
        dataElement: String,
        reasonDataElement: String,
        eventDate: String?
    ) = withContext(Dispatchers.IO) {
        val config = appConfigRepository.getAppConfig(program)

        eventRepository.getEvents(
            teiUids = teiUids,
            program = program,
            programStage = programStage,
            eventDate = eventDate,
        ).map {
            transformations.teiEventTransform(
                eventUid = it.uid(),
                program = program,
                attendanceDataElement = dataElement,
                reasonDataElement = reasonDataElement,
                config = config?.attendance,
            )
        }
    }
}