package org.saudigitus.campaign.core.data.models.datastore.appconfig

import kotlinx.serialization.Serializable

@Serializable
data class Option(
    val uid: String?,
    val code: String?,
    val name: String?,
)
