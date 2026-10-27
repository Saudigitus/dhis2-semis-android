package org.saudigitus.campaign.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.data.EventModel
import org.hisp.dhis.android.core.event.Event
import org.saudigitus.campaign.core.data.models.FormSectionEntity

interface EventRepository {
    suspend fun saveEvent(
        orgUnit: String,
        program: String,
        tei: String? = null,
        enrollment: String? = null,
        eventDate: String?,
        formSections: List<FormSectionEntity>,
    ): Boolean

    suspend fun getOrGenerateEvent(
        orgUnit: String,
        program: String,
        programStage: String,
        tei: String? = null,
        enrollment: String? = null,
    ): Event?

    suspend fun getEvents(
        teiUids: List<String>,
        program: String,
        programStage: String,
        eventDate: String?
    ): List<Event>

    suspend fun getEvents(
        ou: String,
        program: String,
        programStage: String,
        dataElement: String,
        teis: List<String>,
    ): List<Event>

    fun getEvents(program: String, programStage: String? = null): Flow<PagingData<EventModel>>

    suspend fun deleteEvent(eventUid: String)
}