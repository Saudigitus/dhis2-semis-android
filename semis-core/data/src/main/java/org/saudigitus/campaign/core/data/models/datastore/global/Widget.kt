package org.saudigitus.campaign.core.data.models.datastore.global

import kotlinx.serialization.Serializable

@Serializable
data class Widget (
    val key: String,
    val name: String
)