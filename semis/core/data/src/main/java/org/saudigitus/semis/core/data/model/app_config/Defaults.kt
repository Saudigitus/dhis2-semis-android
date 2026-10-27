package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Defaults(
    @SerialName("allowSearching")
    val allowSearching: Boolean?,
    @SerialName("defaultOrder")
    val defaultOrder: String?
)