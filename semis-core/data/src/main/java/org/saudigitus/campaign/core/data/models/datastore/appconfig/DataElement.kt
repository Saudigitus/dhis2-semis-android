package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataElement(
    @SerialName("autoGenerate")
    val autoGenerate: Boolean?,
    @SerialName("dataElement")
    val dataElement: String?,
    @SerialName("enabled")
    val enabled: Boolean?,
    @SerialName("enableOnAssign")
    val enabledOnAssign: Boolean? = null,
    @SerialName("valueType")
    val valueType: String?,
    @SerialName("initialValue")
    val initialValue: Int? = null,
    @SerialName("minValue")
    val minValue: Int? = null,
    @SerialName("maxValue")
    val maxValue: Int? = Int.MAX_VALUE,
    @SerialName("limit")
    val limit: String?,
    @SerialName("mandatory")
    val mandatory: Boolean? = false,
    @SerialName("ouHideStrategy")
    val ouHideStrategy: String?,
    @SerialName("canSelectOUParent")
    val canSelectOUParent: Boolean = true,
    @SerialName("isOUTreeOpen")
    val isOUTreeOpen: Boolean? = true,
    @SerialName("userLevel")
    val userLevel: Int? = null,
    @SerialName("options")
    val options: List<Option>? = null,
    @SerialName("programStages")
    val programStages: List<String>? = null
)
