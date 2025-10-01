package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Filters(
    @SerialName("dataElements")
    val dataElements: List<DataElement?>?
)