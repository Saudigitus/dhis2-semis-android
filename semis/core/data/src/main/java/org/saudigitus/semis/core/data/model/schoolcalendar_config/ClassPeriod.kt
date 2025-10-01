package org.saudigitus.semis.core.data.model.schoolcalendar_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClassPeriod(
    @SerialName("description")
    val description: String?,
    @SerialName("endDate")
    val endDate: String?,
    @SerialName("key")
    val key: String?,
    @SerialName("startDate")
    val startDate: String?
)