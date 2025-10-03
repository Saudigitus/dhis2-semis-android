package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataElement(
    @SerialName("code")
    val code: String?,
    @SerialName("dataElement")
    val dataElement: String?,
    @SerialName("label")
    val label: String?,
    @SerialName("order")
    val order: Int?,
    @SerialName("ulrParam")
    val ulrParam: String?
) {
    override fun toString(): String {
        return "$code, $dataElement, $label, $order, $ulrParam"
    }
}