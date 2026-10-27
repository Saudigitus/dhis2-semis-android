package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvaluateProcessor(
    @SerialName("formula")
    val formula: String? = null,
    @SerialName("evaluationType")
    val evaluationType: String? = null,
    @SerialName("operands")
    val operands: List<Operand>? = null,
)