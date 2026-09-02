package org.saudigitus.semis.core.data.model.schoolcalendar_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicYear(
    @SerialName("code")
    val code: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("endDate")
    val endDate: String?,
    @SerialName("label")
    val label: String?,
    @SerialName("startDate")
    val startDate: String?
)