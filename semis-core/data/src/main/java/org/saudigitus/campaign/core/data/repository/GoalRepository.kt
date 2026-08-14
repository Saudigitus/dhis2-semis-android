package org.saudigitus.campaign.core.data.repository

import kotlinx.coroutines.flow.Flow
import org.saudigitus.campaign.core.data.models.dto.UserGoalWithProgress

interface GoalRepository {
    suspend fun persistGoals()
    suspend fun incrementProgress(
        dataElement: String,
        value: Int,
    )

    fun getUserDailyGoals(): Flow<List<UserGoalWithProgress>>

    suspend fun deleteIfRemovedOnDatastore()

    suspend fun deleteAll()
}