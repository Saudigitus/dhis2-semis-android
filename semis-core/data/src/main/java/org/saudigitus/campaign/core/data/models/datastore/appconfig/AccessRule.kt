package org.saudigitus.campaign.core.data.models.datastore.appconfig

import kotlinx.serialization.Serializable

@Serializable
data class AccessRule(
    val programStage: String?,
    val userGroups: List<String>?
)
