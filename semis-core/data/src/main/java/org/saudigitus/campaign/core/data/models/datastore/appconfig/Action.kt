package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Action(
    @SerialName("message")
    val message: String? = null,
    @SerialName("target")
    val target: String? = null,
    @SerialName("type")
    val type: String? = null
)