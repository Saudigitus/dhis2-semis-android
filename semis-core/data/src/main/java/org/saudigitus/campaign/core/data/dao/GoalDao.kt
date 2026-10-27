package org.saudigitus.campaign.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.saudigitus.campaign.core.data.models.entity.GoalEntity
import org.saudigitus.campaign.core.data.models.entity.GoalGroupAccessEntity

@Dao
interface GoalDao {

    @Upsert
    suspend fun upsertGoals(goals: List<GoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalAccess(access: List<GoalGroupAccessEntity>)

    @Query(
        """
            SELECT * FROM goals
            WHERE uid = :uid
        """
    )
    suspend fun getGoalsByUid(
        uid: String
    ): GoalEntity?

    @Query(
        """
            SELECT DISTINCT uid FROM goals
        """
    )
    fun getGoalsUids(): List<String>

    @Query(
        """
            SELECT DISTINCT uid FROM goals
        """
    )
    fun observeGoalsUids(): Flow<List<String>>

    @Query("""
        SELECT DISTINCT uid
        FROM goals
        WHERE uid IN (:uids)
    """)
    suspend fun getExistingUis(
        uids: List<String>
    ): List<String>

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun countGoals(): Int

    @Query("DELETE FROM goals WHERE uid = :uid")
    suspend fun deleteGoalByUid(uid: String)

    @Query("DELETE FROM goals WHERE uid IN (:uids)")
    suspend fun deleteGoalByUids(uids: List<String>)

    @Query("DELETE FROM goals")
    suspend fun clearGoals()

    @Query("DELETE FROM goal_group_access")
    suspend fun clearGoalAccess()

    @Query("DELETE FROM goal_group_access WHERE uid IN (:uids)")
    suspend fun deleteGoalAccessByUids(uids: List<String>)

    @Transaction
    suspend fun replaceGoals(
        goals: List<GoalEntity>,
        access: List<GoalGroupAccessEntity>
    ) {
        upsertGoals(goals)
        deleteGoalAccessByUids(goals.map { it.uid })

        if (access.isNotEmpty()) {
            insertGoalAccess(access)
        }
    }
}
