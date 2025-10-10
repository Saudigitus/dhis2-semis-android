package org.saudigitus.semis.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Module(
    val key: String,
    val title: String,
    val icon: String,
    val enabled: Boolean = true,
    val route: String
)