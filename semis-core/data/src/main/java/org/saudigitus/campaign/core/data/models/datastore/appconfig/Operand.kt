package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Operand(
    @SerialName("aggregation")
    val aggregation: String? = null,
    @SerialName("field")
    val `field`: String? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("programStage")
    val programStage: String? = null,
    @SerialName("scope")
    val scope: String? = null,
)