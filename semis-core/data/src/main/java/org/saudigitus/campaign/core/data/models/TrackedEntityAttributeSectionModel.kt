package org.saudigitus.campaign.core.data.models

data class TrackedEntityAttributeSectionModel(
    val uid: String,
    val code: String? = null,
    val displayName: String?,
    val description: String? = null,
    val attributes: List<TrackedEntityAttributeModel> = emptyList(),
    val sortOrder: Int? = -1,
)
