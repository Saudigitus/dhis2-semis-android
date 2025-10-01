package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatusOption(
    @SerialName("code")
    val code: String?,
    @SerialName("color")
    val color: String?,
    @SerialName("ConfigKey")
    val configKey: String?,
    @SerialName("icon")
    val icon: String?,
    @SerialName("key")
    val key: String?
)