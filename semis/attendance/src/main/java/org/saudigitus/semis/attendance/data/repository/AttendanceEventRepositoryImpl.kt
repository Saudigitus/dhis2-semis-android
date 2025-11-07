package org.saudigitus.semis.attendance.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.saudigitus.semis.attendance.data.AttendanceTransformation
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.EventRepository
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import javax.inject.Inject

class AttendanceEventRepositoryImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val appConfigRepository: AppConfigRepository,
    private val transformations: AttendanceTransformation,
) : AttendanceEventRepository {
    override suspend fun save(
        program: String,
        programStage: String,
        attendanceEvents: List<AttendanceEventWithDecorator>
    ) =
        withContext(Dispatchers.IO) {
            attendanceEvents.forEach { attendanceEvent ->
                eventRepository.saveEvent(
                    event = attendanceEvent.event?.event,
                    orgUnit = attendanceEvent.tei!!.tei.organisationUnit().orEmpty(),
                    tei = attendanceEvent.tei!!,
                    program = program,
                    programStage = programStage,
                    data = mapOf(
                        "dataElement" to Pair(
                            attendanceEvent.event?.dataElement.orEmpty(),
                            attendanceEvent.event?.value.orEmpty()
                        ),
                        "reasonDataElement" to Pair(
                            attendanceEvent.event?.reasonDataElement.orEmpty(),
                            attendanceEvent.event?.reasonOfAbsence.orEmpty()
                        ),
                    ),
                    eventDate = attendanceEvent.event?.date
                )
            }
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