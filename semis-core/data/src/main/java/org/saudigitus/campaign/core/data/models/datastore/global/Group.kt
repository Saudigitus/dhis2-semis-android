package org.saudigitus.campaign.core.data.models.datastore.global

import kotlinx.serialization.Serializable

@Serializable
data class Group (
    val groupId: String?,
    val key: String?
)
