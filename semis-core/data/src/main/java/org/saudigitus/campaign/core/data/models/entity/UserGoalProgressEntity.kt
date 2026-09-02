package org.saudigitus.campaign.core.data.models.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "user_goal_progress",
    primaryKeys = ["uid", "userId", "date"],
    indices = [
        Index(value = ["userId", "date"])
    ]
)
data class UserGoalProgressEntity(
    val uid: String,
    val userId: String,
    val date: String,
    val achieved: Int
)