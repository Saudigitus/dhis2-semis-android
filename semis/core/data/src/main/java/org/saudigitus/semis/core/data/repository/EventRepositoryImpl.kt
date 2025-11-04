package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.D2
import java.sql.Date
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    val d2: D2
): EventRepository {
    override suspend fun saveEvent(
        orgUnit: String,
        program: String,
        enrollment: String,
        programStage: String
    ) {
        TODO("Not yet implemented")
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