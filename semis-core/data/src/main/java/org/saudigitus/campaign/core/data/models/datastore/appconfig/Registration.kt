package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Registration(
    @SerialName("id")
    val id: String?,
    @SerialName("programStageName")
    val programStageName: String?
)