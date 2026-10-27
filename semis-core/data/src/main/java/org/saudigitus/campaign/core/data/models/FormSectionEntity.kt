package org.saudigitus.campaign.core.data.models

data class FormSectionEntity(
    val uid: String,
    val programStage: String? = null,
    val eventUid: String? = null,
    val code: String?,
    val name: String?,
    val description: String? = null,
    val formFields: List<FormFieldEntity> = emptyList(),
    val sortOrder: Int? = -1
)