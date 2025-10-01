package org.saudigitus.semis.core.data.model.schoolcalendar_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Defaults(
    @SerialName("academicYear")
    val academicYear: String?
)