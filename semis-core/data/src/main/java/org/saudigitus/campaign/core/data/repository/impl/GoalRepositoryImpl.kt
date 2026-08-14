package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.saudigitus.campaign.core.data.dao.GoalDao
import org.saudigitus.campaign.core.data.dao.UserProgressDao
import org.saudigitus.campaign.core.data.models.datastore.global.Goal
import org.saudigitus.campaign.core.data.models.dto.UserGoalWithProgress
import org.saudigitus.campaign.core.data.models.entity.GoalEntity
import org.saudigitus.campaign.core.data.models.entity.GoalGroupAccessEntity
import org.saudigitus.campaign.core.data.models.entity.GoalScope
import org.saudigitus.campaign.core.data.models.entity.GoalType
import org.saudigitus.campaign.core.data.repository.DatastoreRepository
import org.saudigitus.campaign.core.data.repository.GoalRepository
import org.saudigitus.campaign.core.utils.Constants.DS_GLOBAL_KEY
import org.saudigitus.campaign.core.utils.DateHelper
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class GoalRepositoryImpl(
    private val d2: D2,
    private val goalDao: GoalDao,
    private val userProgressDao: UserProgressDao,
    private val datastoreRepository: DatastoreRepository,
) : GoalRepository {

    override suspend fun persistGoals() = withContext(Dispatchers.IO) {
        val globalConfig = datastoreRepository.getGlobalConfig(DS_GLOBAL_KEY)
            ?: return@withContext

        val dsGoals = globalConfig.goals.orEmpty()
            .filter { !it.uid.isNullOrBlank() }

        deleteRemovedGoals(dsGoals)

        if (dsGoals.isEmpty()) return@withContext

        val goalEntities = dsGoals.map { goal ->
            GoalEntity(
                name = goal.name.orEmpty(),
                type = GoalType.from(goal.type),
                uid = goal.uid.orEmpty(),
                goal = goal.goal ?: 0,
                scope = GoalScope.from(goal.access?.scope)
            )
        }

        val accessEntities = dsGoals.flatMap { goal ->
            if (GoalScope.from(goal.access?.scope) == GoalScope.GROUPS) {
                goal.access?.groupIds
                    ?.distinct()
                    .orEmpty()
                    .map { groupId ->
                        GoalGroupAccessEntity(
                            uid = goal.uid.orEmpty(),
                            groupId = groupId
                        )
                    }
            } else emptyList()
        }

        goalDao.replaceGoals(goalEntities, accessEntities)
    }

    override suspend fun incrementProgress(dataElement: String, value: Int) =
        withContext(Dispatchers.IO) {
            val user = d2.userModule().user().blockingGet()
            val goal = goalDao.getGoalsByUid(dataElement) ?: return@withContext

            if (goal.scope == GoalScope.GROUPS) {
                val goals = datastoreRepository.getGlobalConfig(DS_GLOBAL_KEY)?.goals.orEmpty()
                val userGroups = d2.userModule()
                    .userGroups()
                    .blockingGet()
                    .map { it.uid() }

                val goalDL = goals.find {
                    it.uid == goal.uid
                        && it.access?.groupIds.orEmpty().any { groupId -> groupId in userGroups }
                }

                if (goalDL == null) return@withContext
            }

            userProgressDao.incrementProgress(
                uid = goal.uid,
                userId = user?.uid().orEmpty(),
                date = DateHelper.formatDate(System.currentTimeMillis()).orEmpty(),
                increment = value
            )
        }

    override fun getUserDailyGoals(): Flow<List<UserGoalWithProgress>> {
        val user = d2.userModule().user().blockingGet()
        val groupIds = d2.userModule().userGroups().blockingGet().mapNotNull { it.uid() }
        val goalPeriods = GoalPeriods.current()

        return userProgressDao.getUserDailyGoals(
            userId = user?.uid().orEmpty(),
            userGroupIds = groupIds,
            today = goalPeriods.today,
            weekStart = goalPeriods.weekStart,
            weekEnd = goalPeriods.weekEnd,
            monthStart = goalPeriods.monthStart,
            monthEnd = goalPeriods.monthEnd,
            yearStart = goalPeriods.yearStart,
            yearEnd = goalPeriods.yearEnd,
        )
    }

    override suspend fun deleteIfRemovedOnDatastore() = withContext(Dispatchers.IO) {
        try {
            val globalConfig = datastoreRepository.getGlobalConfig(DS_GLOBAL_KEY)
                ?: return@withContext

            val validUids = globalConfig.goals
                ?.mapNotNull { it.uid }
                ?.toSet() ?: emptySet()

            val user = d2.userModule().user().blockingGet()
            val userId = user?.uid() ?: return@withContext

            val localUids = goalDao.getGoalsUids()
            val localSet = localUids.toSet()

            val toDelete = localSet.subtract(validUids)

            if (toDelete.isNotEmpty()) {
                goalDao.deleteGoalByUids(toDelete.toList())
                userProgressDao.deleteByUids(
                    uids = toDelete.toList(),
                    userId = userId
                )
                Timber.d("Deleted removed goals: $toDelete")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting removed goals from datastore")
        }
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        try {
            goalDao.clearGoals()
            goalDao.clearGoalAccess()
            userProgressDao.deleteAll()
            Timber.d("All goal data cleared successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing goal data")
        }
    }

    private suspend fun deleteRemovedGoals(dsGoals: List<Goal>) {
        val user = d2.userModule().user().blockingGet()
        val userId = user?.uid() ?: return

        val dsDataElements = dsGoals
            .mapNotNull { it.uid }
            .toSet()

        val localDataElements = goalDao
            .getGoalsUids()
            .toSet()

        val toDelete = localDataElements - dsDataElements

        if (toDelete.isNotEmpty()) {
            goalDao.deleteGoalByUids(toDelete.toList())
            userProgressDao.deleteByUids(toDelete.toList(), userId)
        }
    }

    private data class GoalPeriods(
        val today: String,
        val weekStart: String,
        val weekEnd: String,
        val monthStart: String,
        val monthEnd: String,
        val yearStart: String,
        val yearEnd: String,
    ) {
        companion object {
            fun current(): GoalPeriods {
                val today = LocalDate.now()

                return GoalPeriods(
                    today = today.toString(),
                    weekStart = today
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .toString(),
                    weekEnd = today
                        .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                        .toString(),
                    monthStart = today.withDayOfMonth(1).toString(),
                    monthEnd = today.withDayOfMonth(today.lengthOfMonth()).toString(),
                    yearStart = today.withDayOfYear(1).toString(),
                    yearEnd = today.withDayOfYear(today.lengthOfYear()).toString(),
                )
            }
        }
    }
}
