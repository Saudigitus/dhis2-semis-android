package org.saudigitus.semis.core.data.repository

import org.hisp.dhis.android.core.event.Event
import org.saudigitus.semis.core.data.model.SearchTeiModel

interface EventRepository {

    /**
     * Saves an event to the database.
     *
     * This function is responsible for creating or updating an event in the database
     * based on the provided parameters. It supports flexible data input through the [data] map,
     * which defines the elements and corresponding values to store for the event.
     *
     * @param event The ID of the event to update. If `null`, a new event will be created.
     * @param orgUnit The organisation unit where the event belongs.
     * @param program The program associated with the event.
     * @param programStage The program stage linked to the event.
     * @param tei The Tracked Entity Instance (TEI) associated with this event.
     * @param data A map of data elements and their values for the event.
     * Each entry uses the following structure:
     * ```
     * data["dataElement"] = Pair("dataElementId", "value")
     * data["reasonDataElement"] = Pair("dataElementId", "value")
     * ```
     * - The **`dataElement`** key is **mandatory** and represents the primary data value to be saved.
     * - The **`reasonDataElement`** key is **optional** and is typically used in attendance-related events
     *   to store an additional reason or comment.
     * For default scenarios, only the `dataElement` key is required.
     *
     * @param eventDate The date when the event occurred. If `null`, the current date will be used.
     */
    suspend fun saveEvent(
        event: String?,
        orgUnit: String,
        program: String,
        programStage: String,
        tei: SearchTeiModel,
        data: Map<String, Pair<String, String>>,
        eventDate: String?,
    )
    suspend fun getEvents(
        teiUids: List<String>,
        program: String,
        programStage: String,
        eventDate: String?
    ): List<Event>
}