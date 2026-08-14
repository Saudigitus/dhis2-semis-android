package org.saudigitus.campaign.core.data.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val type: GoalType,
    val goal: Int,
    val scope: GoalScope
)