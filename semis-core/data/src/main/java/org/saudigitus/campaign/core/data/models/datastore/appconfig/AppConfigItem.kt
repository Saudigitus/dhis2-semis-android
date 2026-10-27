package org.saudigitus.campaign.core.data.models.datastore.appconfig


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfigItem(
    @SerialName("default")
    val default: Default?,
    @SerialName("formValidations")
    val formValidations: List<FormValidation>?,
    @SerialName("fields")
    val fields: Fields?,
    @SerialName("navigateTo")
    val navigateTo: String? = null,
    @SerialName("program")
    val program: String?,
    @SerialName("registrations")
    val registrations: List<Registration>?,
    @SerialName("filters")
    val filters: List<Filter>?,
)
