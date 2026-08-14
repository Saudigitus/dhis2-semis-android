package org.saudigitus.campaign.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.saudigitus.campaign.core.data.models.FilterModel
import org.saudigitus.campaign.core.data.models.FormFieldEntity
import org.saudigitus.campaign.core.data.models.SearchTeiModel
import org.saudigitus.campaign.core.utils.location.state.CoordinateState

interface TeiRepository {
    suspend fun getTeiByUid(teiUid: String, program: String): SearchTeiModel?

    suspend fun create(orgUnit: String, program: String, fields: List<FormFieldEntity>): String?
    suspend fun update (tei: String, fields: List<FormFieldEntity>): String?
    suspend fun getTrackerEntities(
        ou: String,
        program: String,
        stage: String,
        dataElementIds: List<String>,
        dataValues: List<String>,
    ): Flow<List<SearchTeiModel>>

    fun getTrackerEntitiesByProgram(program: String): Flow<PagingData<SearchTeiModel>>

    fun searchTrackerEntities(
        program: String,
        query: String,
        filters: List<FilterModel> = emptyList(),
        coordinates: Pair<FilterModel, CoordinateState>? = null,
    ): Flow<PagingData<SearchTeiModel>>

    suspend fun getFilters(program: String): List<FilterModel>

    suspend fun getTrackedEntityType(uid: String): String

    suspend fun getTrackedEntityAttributeValues(tei: String): List<TrackedEntityAttributeValue>

}
