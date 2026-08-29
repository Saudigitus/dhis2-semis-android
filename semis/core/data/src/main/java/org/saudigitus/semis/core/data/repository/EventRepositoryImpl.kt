package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.contextValuesHeldBy
import org.saudigitus.semis.core.utils.Constants
import org.saudigitus.semis.core.utils.DateHelper
import timber.log.Timber
import java.sql.Date
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    val d2: D2
) : EventRepository {

    override suspend fun createEvent(
        orgUnit: String,
        program: String,
        programStage: String,
        enrollmentUid: String?,
        data: List<Pair<String, String?>>,
        eventDate: String?,
        status: EventStatus,
    ): String = withContext(Dispatchers.IO) {
        val date = eventDate ?: DateHelper.formatDate(System.currentTimeMillis())!!
        val uid = createEventProjection(
            enrollment = enrollmentUid,
            ou = orgUnit,
            program = program,
            programStage = programStage,
        )
        data.forEach { (dataElement, value) ->
            d2.trackedEntityModule().trackedEntityDataValues()
                .value(uid, dataElement)
                .blockingSet(value)
        }
        d2.eventModule().events().uid(uid).apply {
            setEventDate(Date.valueOf(date))
            setStatus(status)
        }
        uid
    }

    override suspend fun setEventStatus(eventUid: String, status: EventStatus) {
        withContext(Dispatchers.IO) {
            d2.eventModule().events().uid(eventUid).setStatus(status)
        }
    }

    private fun getAttributeOptionCombo() =
        d2.categoryModule().categoryOptionCombos()
            .byDisplayName().eq(Constants.DEFAULT).one().blockingGet()?.uid()

    private fun createEventProjection(
        enrollment: String? = null,
        ou: String,
        program: String,
        programStage: String,
    ): String {
        val projection = EventCreateProjection.builder()
            .organisationUnit(ou)
            .program(program).programStage(programStage)
            .attributeOptionCombo(getAttributeOptionCombo())

        return d2.eventModule().events()
            .blockingAdd(
                if (!enrollment.isNullOrEmpty()) {
                    projection.enrollment(enrollment).build()
                } else {
                    projection.build()
                }
            )
    }

    override suspend fun createEmptyEvent(
        orgUnit: String,
        program: String,
        programStage: String,
        enrollment: String,
    ): String = withContext(Dispatchers.IO) {
        require(programStage.isNotBlank()) { "Program stage must not be blank" }
        require(enrollment.isNotBlank()) { "Enrollment must not be blank" }

        val existing = d2.eventModule().events()
            .byEnrollmentUid().eq(enrollment)
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .byDeleted().isFalse
            .one()
            .blockingGet()

        if (existing != null) return@withContext existing.uid()

        val uid = createEventProjection(
            enrollment = enrollment,
            ou = orgUnit,
            program = program,
            programStage = programStage,
        )
        val repository = d2.eventModule().events().uid(uid)
        repository.setEventDate(Date(DateUtils.getInstance().today.time))
        repository.setStatus(EventStatus.ACTIVE)
        uid
    }

    /**
     * The record a save should update instead of creating a new one, scoped to the school doing
     * the saving. Without the school in the match, a save for a transferred learner would pick up
     * the record their previous school wrote for the same day and stage, and the update would
     * belong to a school the user cannot write into.
     */
    private fun eventUid(
        tei: String?,
        orgUnit: String,
        program: String,
        programStage: String,
        date: String?,
    ): String? {
        val eventsRepo = d2.eventModule().events()

        return if (!tei.isNullOrEmpty()) {
            eventsRepo
            .byTrackedEntityInstanceUids(listOf(tei))
                .byOrganisationUnitUid().eq(orgUnit)
                .byProgramUid().eq(program)
                .byProgramStageUid().eq(programStage)
                .byDeleted().isFalse
                .byEventDate().eq(Date.valueOf(date))
                .one().blockingGet()?.uid()
        } else {
            eventsRepo
                .byOrganisationUnitUid().eq(orgUnit)
                .byProgramUid().eq(program)
                .byProgramStageUid().eq(programStage)
                .byDeleted().isFalse
                .byEventDate().eq(Date.valueOf(date))
                .one().blockingGet()?.uid()
        }
    }

    override suspend fun saveEvent(
        event: String?,
        orgUnit: String,
        program: String,
        programStage: String,
        tei: SearchTeiModel?,
        data: Map<String, Pair<String, String>>,
        eventDate: String?,
        contextValues: List<Pair<String, String>>
    ) {
        withContext(Dispatchers.IO) {
            val date = eventDate ?: DateHelper.formatDate(System.currentTimeMillis())!!

            try {
                val uid = event ?: eventUid(
                    tei?.uid(),
                    orgUnit,
                    program,
                    programStage,
                    date
                ) ?: createEventProjection(
                    tei?.selectedEnrollment?.uid(),
                    orgUnit,
                    program,
                    programStage,
                )

                val primaryDataValue = data["dataElement"]!!
                val secondaryDataValue = data.getOrElse("reasonDataElement") { null }

                d2.trackedEntityModule().trackedEntityDataValues()
                    .value(uid, primaryDataValue.first)
                    .blockingSet(primaryDataValue.second)

                if (secondaryDataValue != null && secondaryDataValue.first.isNotEmpty()) {
                    d2.trackedEntityModule().trackedEntityDataValues()
                        .value(uid, secondaryDataValue.first)
                        .blockingSet(secondaryDataValue.second.takeIf { it.isNotEmpty() })
                }

                // The class context is rewritten on every save, not only on the first, so that a
                // record edited after the class was corrected does not keep the old one.
                contextValuesHeldBy(
                    stageDataElements = stageDataElements(programStage),
                    contextValues = contextValues,
                ).forEach { (dataElement, value) ->
                    d2.trackedEntityModule().trackedEntityDataValues()
                        .value(uid, dataElement)
                        .blockingSet(value)
                }

                val repository = d2.eventModule().events().uid(uid)
                repository.setEventDate(Date.valueOf(date))
                repository.setStatus(EventStatus.COMPLETED)
            } catch (e: Exception) {
                Timber.tag("SAVE_EVENT").e(e)
            }
        }
    }

    /** The data elements a program stage is configured with. */
    private fun stageDataElements(programStage: String): List<String> =
        d2.programModule().programStageDataElements()
            .byProgramStage().eq(programStage)
            .blockingGet()
            .mapNotNull { it.dataElement()?.uid() }

    override suspend fun saveEvent(
        event: String?,
        orgUnit: String,
        program: String,
        programStage: String,
        tei: SearchTeiModel?,
        data: List<Pair<String, String?>>,
        eventDate: String?
    ) {
        withContext(Dispatchers.IO) {
            val date = eventDate ?: DateHelper.formatDate(System.currentTimeMillis())!!

            try {
                val uid = event ?: eventUid(
                    tei?.uid(),
                    orgUnit,
                    program,
                    programStage,
                    date
                ) ?: createEventProjection(
                    tei?.selectedEnrollment?.uid(),
                    orgUnit,
                    program,
                    programStage,
                )

                for (item in data) {
                    d2.trackedEntityModule().trackedEntityDataValues()
                        .value(uid, item.first)
                        .blockingSet(item.second)
                }

                val repository = d2.eventModule().events().uid(uid)
                repository.setEventDate(Date.valueOf(date))
                repository.setStatus(EventStatus.COMPLETED)
            } catch (e: Exception) {
                Timber.tag("SAVE_EVENT").e(e)
            }
        }
    }

    override suspend fun getEvents(
        teiUids: List<String>,
        program: String,
        programStage: String,
        eventDate: String?
    ) = withContext(Dispatchers.IO) {
        val date = if (eventDate != null) {
            Date.valueOf(eventDate)
        } else {
            DateUtils.getInstance().today
        }

        d2.eventModule().events()
            .byTrackedEntityInstanceUids(teiUids)
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .byDeleted().isFalse
            .byEventDate().eq(date)
            .withTrackedEntityDataValues()
            .blockingGet()
    }

    override suspend fun getEvents(
        ou: String,
        program: String,
        programStage: String,
        dataElement: String,
        teis: List<String>
    ) = withContext(Dispatchers.IO) {
        d2.eventModule().events()
            .byTrackedEntityInstanceUids(teis)
            .byOrganisationUnitUid().eq(ou)
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .withTrackedEntityDataValues()
            .blockingGet()
    }

    override suspend fun deleteEvent(event: String) = withContext(Dispatchers.IO) {
        d2.eventModule().events().uid(event).delete().blockingAwait()
    }
}
