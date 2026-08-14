package org.saudigitus.semis.core.data.model.app_config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AttendanceStatus(
    val allowAttendanceStatus: Boolean? = null,
    val program: String? = null,
    val programStage: String? = null,
    @JsonNames("totalAbsencesDataElement")
    val totalAbsences: String? = null,
    @JsonNames("totalRecordsDataElement")
    val totalRecords: String? = null,
)

fun AttendanceStatus?.isEnabledAndConfigured(): Boolean {
    val config = this ?: return false

    return config.allowAttendanceStatus == true &&
        !config.program.isNullOrBlank() &&
        !config.programStage.isNullOrBlank() &&
        !config.totalAbsences.isNullOrBlank() &&
        !config.totalRecords.isNullOrBlank()
}
