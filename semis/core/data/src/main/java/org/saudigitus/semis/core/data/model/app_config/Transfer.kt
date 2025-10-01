package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val penddingCode: String?,
    @SerialName("programStage")
    val programStage: String?,
    @SerialName("reprovedCode")
    val reprovedCode: String?,
    @SerialName("status")
    val status: String?,
    @SerialName("statusOptions")
    val statusOptions: List<StatusOption>?
)