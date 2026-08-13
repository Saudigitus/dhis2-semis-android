package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attendance(
    @SerialName("absenceReason")
    val absenceReason: String?,
    @SerialName("attendanceStatus")
    val attendanceStatus: AttendanceStatus?,
    @SerialName("enabled")
    val enabled: Boolean?,
    @SerialName("lastUpdate")
    val lastUpdate: String?,
    @SerialName("programStage")
    val programStage: String?,
    @SerialName("status")
    val status: String?,
    @SerialName("statusOptions")
    val statusOptions: List<StatusOption?>?
)