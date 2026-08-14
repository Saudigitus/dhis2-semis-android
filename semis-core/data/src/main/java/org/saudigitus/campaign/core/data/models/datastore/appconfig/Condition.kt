package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Condition(
    @SerialName("logicalOperator")
    val logicalOperator: String? = null,
    @SerialName("rules")
    val rules: List<Rule>? = null
)