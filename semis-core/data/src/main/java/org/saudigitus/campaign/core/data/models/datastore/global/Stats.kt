package org.saudigitus.campaign.core.data.models.datastore.global

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stats(
    @SerialName("createdByUser")
    val createdByUser: Boolean? = false,
    @SerialName("todayOnly")
    val todayOnly: Boolean? = false,
)
