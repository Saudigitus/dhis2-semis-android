package org.saudigitus.semis.core.data.repository

import org.hisp.dhis.android.core.event.Event

interface EventRepository {
    suspend fun saveEvent(
        orgUnit: String,
        program: String,
        enrollment: String,
        programStage: String
    )
    suspend fun getEvents(
        teiUids: List<String>,
        program: String,
        programStage: String,
        eventDate: String?
    ): List<Event>
}