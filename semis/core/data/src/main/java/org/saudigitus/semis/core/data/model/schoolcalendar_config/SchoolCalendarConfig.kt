package org.saudigitus.semis.core.data.model.schoolcalendar_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SchoolCalendarConfig(
    @SerialName("academicYear")
    val academicYear: String?,
    @SerialName("defaults")
    val defaults: Defaults?,
    @SerialName("schoolCalendar")
    val schoolCalendar: List<SchoolCalendar?>?
)