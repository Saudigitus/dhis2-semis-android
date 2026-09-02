package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fields(
    @SerialName("attributes")
    val attributes: List<Attribute>?,
    @SerialName("dataElements")
    val dataElements: List<DataElement>?
)