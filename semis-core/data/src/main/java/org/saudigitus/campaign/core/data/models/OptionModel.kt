package org.saudigitus.campaign.core.data.models

data class OptionModel(
    val uid: String,
    val code: String?,
    val displayName: String?,
    val sortOrder: Int?
) {
    override fun toString() = displayName.orEmpty()
}
