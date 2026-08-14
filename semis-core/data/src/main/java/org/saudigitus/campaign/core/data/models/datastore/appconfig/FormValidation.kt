package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FormValidation(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("evaluateProcessor")
    val evaluateProcessor: EvaluateProcessor? = null,
    @SerialName("field")
    val fieldUid: String? = null,
    @SerialName("operator")
    val operand: String? = null,
    @SerialName("programStage")
    val programStage: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("value")
    val value: String? = null,
    @SerialName("valueType")
    val valueType: String? = null,
    @SerialName("action")
    val action: Action? = null,
    @SerialName("condition")
    val condition: Condition? = null,
)
