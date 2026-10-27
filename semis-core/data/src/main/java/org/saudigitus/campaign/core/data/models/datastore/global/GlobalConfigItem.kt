package org.saudigitus.campaign.core.data.models.datastore.global

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GlobalConfigItem (
    @SerialName("goals")
    val goals: List<Goal>?,
    @SerialName("users")
    val users: List<User>?,
    @SerialName("services")
    val services: List<Service>?,
    @SerialName("stats")
    val sync: Stats?,
    @SerialName("menu")
    val menuDrawer: MenuDrawer?,
)