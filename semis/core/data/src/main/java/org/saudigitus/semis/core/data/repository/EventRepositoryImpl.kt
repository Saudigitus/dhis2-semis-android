package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.utils.Constants
import org.saudigitus.semis.core.utils.DateHelper
import timber.log.Timber
import java.sql.Date
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    val d2: D2
): EventRepository {

    private fun getAttributeOptionCombo() =
        d2.categoryModule().categoryOptionCombos()
            .byDisplayName().eq(Constants.DEFAULT).one().blockingGet()?.uid()

    private fun createEventProjection(
        enrollment: String,
        ou: String,
        program: String,
        programStage: String,
    ): String {
        return d2.eventModule().events()
            .blockingAdd(
                EventCreateProjection.builder()
                    .organisationUnit(ou)
                    .program(program).programStage(programStage)
                    .attributeOptionCombo(getAttributeOptionCombo())
                    .enrollment(enrollment).build(),
            )
    }

    private fun eventUid(
        tei: String,
        program: String,
        programStage: String,
        date: String?,
    ): String? {
        return d2.eventModule().events()
            .byTrackedEntityInstanceUids(listOf(tei))
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .byEventDate().eq(Date.valueOf(date))
            .one().blockingGet()?.uid()
    }

    override suspend fun saveEvent(
        event: String?,
        orgUnit: String,
        program: String,
        programStage: String,
        tei: SearchTeiModel,
        data: Map<String, Pair<String, String>>,
        eventDate: String?
    ) {
        withContext(Dispatchers.IO) {
            val date = eventDate ?: DateHelper.formatDate(System.currentTimeMillis())!!

            try {
                val uid = event ?: eventUid(
                    tei.uid(),
                    program,
                    programStage,
                    date
                ) ?: createEventProjection(
                    tei.selectedEnrollment?.uid().orEmpty(),
                    orgUnit,
                    program,
                    programStage,
                )

                val primaryDataValue = data["dataElement"]!!
                val secondaryDataValue = data.getOrElse("reasonDataElement") { null }

                d2.trackedEntityModule().trackedEntityDataValues()
                    .value(uid, primaryDataValue.first)
                    .blockingSet(primaryDataValue.second)

                if (secondaryDataValue != null && secondaryDataValue.second.isNotEmpty()) {
                    d2.trackedEntityModule().trackedEntityDataValues()
                        .value(uid, secondaryDataValue.first)
                        .blockingSet(secondaryDataValue.second)
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
}