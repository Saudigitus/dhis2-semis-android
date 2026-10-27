package org.saudigitus.campaign.core.data.models.datastore.global

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Access(
    @SerialName("scope")
    val scope: String?,
    @SerialName("groupIds")
    val groupIds: List<String>?
)
