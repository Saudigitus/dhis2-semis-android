package org.saudigitus.campaign.core.data.models.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "goal_group_access",
    primaryKeys = ["uid", "groupId"],
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["uid"],
            childColumns = ["uid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("groupId")
    ]
)
data class GoalGroupAccessEntity(
    val uid: String,
    val groupId: String
)