package org.saudigitus.campaign.core.data.models.datastore.appconfig

import kotlinx.serialization.Serializable

@Serializable
data class Form(
    val formType: String?,
    val constraints: Constraints?
)
