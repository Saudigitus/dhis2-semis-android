package org.saudigitus.campaign.core.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import org.saudigitus.campaign.core.data.dao.D2MetadataHistoryDao
import org.saudigitus.campaign.core.data.dao.DatastoreDao
import org.saudigitus.campaign.core.data.dao.GoalDao
import org.saudigitus.campaign.core.data.dao.UserProgressDao
import org.saudigitus.campaign.core.data.models.entity.DatastoreEntity
import org.saudigitus.campaign.core.data.models.entity.EventHistoryEntity
import org.saudigitus.campaign.core.data.models.entity.GoalEntity
import org.saudigitus.campaign.core.data.models.entity.GoalGroupAccessEntity
import org.saudigitus.campaign.core.data.models.entity.TrackedEntityInstanceHistoryEntity
import org.saudigitus.campaign.core.data.models.entity.UserGoalProgressEntity
import org.saudigitus.campaign.core.data.models.entity.convertes.GoalConverters

@Database(
    entities = [
        GoalEntity::class,
        GoalGroupAccessEntity::class,
        UserGoalProgressEntity::class,
        TrackedEntityInstanceHistoryEntity::class,
        EventHistoryEntity::class,
        DatastoreEntity::class,
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = AppDatabase.Migration2To3::class),
        AutoMigration(from = 3, to = 4),
    ]
)
@TypeConverters(GoalConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun progressDao(): UserProgressDao

    abstract fun d2MetadataHistoryDao(): D2MetadataHistoryDao

    abstract fun datastoreDao(): DatastoreDao

    @RenameColumn(tableName = "goals", fromColumnName = "dataElement", toColumnName = "uid")
    @RenameColumn(tableName = "goal_group_access", fromColumnName = "dataElement", toColumnName = "uid")
    @RenameColumn(tableName = "user_goal_progress", fromColumnName = "dataElement", toColumnName = "uid")
    class Migration2To3 : AutoMigrationSpec

    companion object {
        const val DB_NAME = "campaign"
    }
}