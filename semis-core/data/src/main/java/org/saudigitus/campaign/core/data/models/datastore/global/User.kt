package org.saudigitus.campaign.core.data.models.datastore.global

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val groups: List<Group>?,
    val widgets: List<Widget>?
)