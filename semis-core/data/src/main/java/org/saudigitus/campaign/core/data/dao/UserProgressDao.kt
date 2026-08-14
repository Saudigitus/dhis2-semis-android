package org.saudigitus.campaign.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.saudigitus.campaign.core.data.models.dto.UserGoalWithProgress
import org.saudigitus.campaign.core.data.models.entity.UserGoalProgressEntity

@Dao
interface UserProgressDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProgress(progress: UserGoalProgressEntity): Long

    @Query("""
            UPDATE user_goal_progress
            SET achieved = achieved + :increment
            WHERE uid = :uid
            AND userId = :userId
            AND date = :date
        """
    )
    suspend fun incrementIfExists(
        uid: String,
        userId: String,
        date: String,
        increment: Int
    ): Int

    @Transaction
    suspend fun incrementProgress(
        uid: String,
        userId: String,
        date: String,
        increment: Int
    ) {
        val updatedRows = incrementIfExists(
            uid,
            userId,
            date,
            increment
        )

        if (updatedRows == 0) {
            insertProgress(
                UserGoalProgressEntity(
                    uid = uid,
                    userId = userId,
                    date = date,
                    achieved = increment
                )
            )
        }
    }

    @Query("""
            SELECT g.*,

                   CAST(COALESCE(SUM(ugp.achieved), 0) AS INTEGER) AS achieved
            FROM goals g
            LEFT JOIN user_goal_progress ugp
                ON g.uid = ugp.uid
                AND ugp.userId = :userId
                AND (
                    (g.type = 'DAILY' AND ugp.date = :today)
                    OR (g.type = 'WEEKLY' AND ugp.date BETWEEN :weekStart AND :weekEnd)
                    OR (g.type = 'MONTHLY' AND ugp.date BETWEEN :monthStart AND :monthEnd)
                    OR (g.type = 'YEARLY' AND ugp.date BETWEEN :yearStart AND :yearEnd)
                )
            WHERE g.scope = 'ALL'
               OR (
                    g.scope = 'GROUPS'
                    AND EXISTS (
                        SELECT 1
                        FROM goal_group_access ga
                        WHERE ga.uid = g.uid
                          AND ga.groupId IN (:userGroupIds)
                    )
               )
            GROUP BY g.uid, g.name, g.type, g.goal, g.scope
        """
    )
    fun getUserDailyGoals(
        userId: String,
        userGroupIds: List<String>,
        today: String,
        weekStart: String,
        weekEnd: String,
        monthStart: String,
        monthEnd: String,
        yearStart: String,
        yearEnd: String
    ): Flow<List<UserGoalWithProgress>>

    @Query("""
        SELECT DISTINCT uid
        FROM goals
    """
    )
    fun getGoalUids(): Flow<List<String>>

    @Query("""
        DELETE FROM user_goal_progress
        WHERE uid = :uid
    """)
    suspend fun deleteByUid(uid: String)

    @Query("""
        DELETE FROM user_goal_progress
        WHERE uid IN (:uids) AND userId = :userId
    """)
    suspend fun deleteByUids(uids: List<String>, userId: String)


    @Query("""
        DELETE FROM user_goal_progress
    """)
    suspend fun deleteAll()
}
