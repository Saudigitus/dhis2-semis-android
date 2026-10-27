package org.saudigitus.campaign.core.data.models.datastore.appconfig

import kotlinx.serialization.Serializable

@Serializable
data class Constraints(
    val registration: String?,
    val accessRules: List<AccessRule>?,
)
