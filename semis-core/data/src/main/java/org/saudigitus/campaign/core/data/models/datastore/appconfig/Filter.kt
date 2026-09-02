package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    @SerialName("uid")
    val uid: String?,
    @SerialName("key")
    val key: String?,
    @SerialName("displayName")
    val displayName: String?,
    @SerialName("type")
    val type: FilterType? = FilterType.ATTRIBUTE,
    @SerialName("programStage")
    val programStage: String? = null,
    @SerialName("filterProcessor")
    val filterProcessor: FilterProcessor? = null,
    @SerialName("filteraProcessor")
    val legacyFilterProcessor: FilterProcessor? = null,
    @SerialName("range")
    val range: Int? = 5,
    @SerialName("renderType")
    val renderType: RenderType? = RenderType.SEARCH_FIELD,
) {
    val resolvedFilterProcessor: FilterProcessor?
        get() = filterProcessor ?: legacyFilterProcessor
}
