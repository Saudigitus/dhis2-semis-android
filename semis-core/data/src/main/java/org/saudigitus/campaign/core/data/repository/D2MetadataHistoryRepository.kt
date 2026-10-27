package org.saudigitus.campaign.core.data.repository

import org.saudigitus.campaign.core.data.models.entity.EventHistoryEntity
import org.saudigitus.campaign.core.data.models.entity.TrackedEntityInstanceHistoryEntity

interface D2MetadataHistoryRepository {
    suspend fun createTEIHistory(program: String, tei: String)
    suspend fun createEventHistory(program: String, event: String)

    suspend fun getTEIHistory(program: String): List<TrackedEntityInstanceHistoryEntity>
    suspend fun getTEIHistoryUids(todayOnly: Boolean = false): List<String>
    suspend fun getTEIHistoryUids(program: String): List<String>
    suspend fun getEventHistory(program: String): List<EventHistoryEntity>
    suspend fun getEventHistoryUids(program: String): List<String>
    suspend fun getEventHistoryUids(todayOnly: Boolean = false): List<String>

    suspend fun cleanTable()
}