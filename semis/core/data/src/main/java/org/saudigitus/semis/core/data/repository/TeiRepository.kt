package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.flow.Flow
import org.saudigitus.semis.core.data.model.SearchTeiModel

interface TeiRepository {
    suspend fun getTrackerEntities(
        ou: String,
        program: String,
        stage: String,
        dataElementIds: List<String>,
        dataValues: List<String>,
    ): Flow<List<SearchTeiModel>>
}