package org.saudigitus.campaign.core.data.models.datastore.global

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val programId: String?,
    val displayName: String?,
    val sortOrder: Int?,
)