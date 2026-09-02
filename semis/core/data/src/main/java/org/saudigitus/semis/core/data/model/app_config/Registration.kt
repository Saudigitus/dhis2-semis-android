package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Registration(
    @SerialName("enabled")
    val enabled: Boolean?,
    @SerialName("grade")
    val grade: String?,
    @SerialName("lastUpdate")
    val lastUpdate: String?,
    @SerialName("programStage")
    val programStage: String?,
    @SerialName("section")
    val section: String?
)