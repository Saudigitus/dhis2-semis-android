package org.saudigitus.semis.core.data.model.app_config

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceStatus(
    val allowAttendanceStatus: Boolean?,
    val program: String?,
    val programStage: String?,
    val status: String?,
)
