package org.saudigitus.campaign.core.data.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventHistoryEntity(
    @PrimaryKey val uid: String,
    val program: String,
    val userId: String,
    val date: String,
)
