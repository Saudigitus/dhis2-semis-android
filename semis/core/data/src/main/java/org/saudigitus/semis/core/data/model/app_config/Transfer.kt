package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Transfer(
    @SerialName("approvedCode")
    val approvedCode: String?,
    @SerialName("destinySchool")
    val destinySchool: String?,
    @SerialName("enabled")
    val enabled: Boolean?,
    @SerialName("lastUpdate")
    val lastUpdate: String?,
    @SerialName("originSchool")
    val originSchool: String?,
    @SerialName("penddingCode")
    @JsonNames("pendingCode")
    val pendingCode: String?,
    @SerialName("programStage")
    val programStage: String?,
    @SerialName("reprovedCode")
    val reprovedCode: String?,
    @SerialName("status")
    val status: String?,
    @SerialName("statusOptions")
    val statusOptions: List<StatusOption>?
)

fun Transfer.pendingStatusCode(): String? = statusOptions
    ?.firstOrNull { option ->
        option.key?.trim()?.equals("pending", ignoreCase = true) == true
    }
    ?.code
    ?.takeIf(String::isNotBlank)

fun Transfer?.isIncomingEnabledAndConfigured(): Boolean = this?.run {
    enabled == true &&
        !programStage.isNullOrBlank() &&
        !originSchool.isNullOrBlank() &&
        !destinySchool.isNullOrBlank()
} == true
