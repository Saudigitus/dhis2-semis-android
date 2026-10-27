package org.saudigitus.campaign.core.data.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DatastoreEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val lastUpdate: Long = System.currentTimeMillis(),
)
