package org.saudigitus.campaign.core.data.models.datastore.global

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Goal(
    @SerialName("name")
    val name: String?,
    @SerialName("uid")
    val uid: String?,
    @SerialName("type")
    val type: String?,
    @SerialName("goal")
    val goal: Int?,
    @SerialName("access")
    val access: Access?
)