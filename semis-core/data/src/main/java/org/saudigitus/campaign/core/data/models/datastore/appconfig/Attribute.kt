package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attribute(
    @SerialName("attribute")
    val attribute: String?,
    @SerialName("autoGenerate")
    val autoGenerate: Boolean?,
    @SerialName("enabled")
    val enabled: Boolean?,
    @SerialName("enableOnAssign")
    val enabledOnAssign: Boolean? = null,
    @SerialName("initialValue")
    val initialValue: Int?,
    @SerialName("minValue")
    val minValue: Int? = 0,
    @SerialName("maxValue")
    val maxValue: Int? = Int.MAX_VALUE,
    @SerialName("valueType")
    val valueType: String?,
    @SerialName("mandatory")
    val mandatory: Boolean? = false,
    @SerialName("ouHideStrategy")
    val ouHideStrategy: String?,
    @SerialName("canSelectOUParent")
    val canSelectOUParent: Boolean = true,
    @SerialName("isOUTreeOpen")
    val isOUTreeOpen: Boolean? = true,
)