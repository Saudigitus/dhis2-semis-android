package org.saudigitus.semis.core.data.model.schoolcalendar_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SchoolCalendar(
    @SerialName("academicYear")
    val academicYear: AcademicYear?,
    @SerialName("classPeriods")
    val classPeriods: List<ClassPeriod?>?,
    @SerialName("holidays")
    val holidays: List<Holiday?>?,
    @SerialName("id")
    val id: String?,
    @SerialName("weekDays")
    val weekDays: WeekDays?
)