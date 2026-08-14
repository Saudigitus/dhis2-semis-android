package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Rule(
    @SerialName("field")
    val fieldUid: String? = null,
    @SerialName("operator")
    val operand: String? = null,
    @SerialName("value")
    val value: JsonElement? = null,
    @SerialName("valueType")
    val valueType: String? = null
)