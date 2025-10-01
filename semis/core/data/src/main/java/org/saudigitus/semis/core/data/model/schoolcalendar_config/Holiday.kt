package org.saudigitus.semis.core.data.model.schoolcalendar_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Holiday(
    @SerialName("date")
    val date: String?,
    @SerialName("event")
    val event: String?,
    @SerialName("type")
    val type: String?
)