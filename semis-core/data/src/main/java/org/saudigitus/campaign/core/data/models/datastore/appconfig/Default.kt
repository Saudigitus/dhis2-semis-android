package org.saudigitus.campaign.core.data.models.datastore.appconfig

import kotlinx.serialization.Serializable

@Serializable
data class Default(
    val teiDashboard: TeiDashboard?,
    val forms: List<Form>?,
    val customNavigation: List<CustomNavigation>? = null,
    val displayMode: DisplayMode? = null,
)
