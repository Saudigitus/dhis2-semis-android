package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocioEconomics(
    @SerialName("programStage")
    val programStage: String?
)