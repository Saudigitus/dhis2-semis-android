package org.saudigitus.campaign.core.data.models

import org.hisp.dhis.android.core.common.ValueType

data class FormFieldEntity(val eventUid: String? = null, val uid: String, val value: String? = null)

data class FormSectionEntity(
    val uid: String,
    val programStage: String? = null,
    val eventUid: String? = null,
    val code: String? = null,
    val name: String? = null,
    val description: String? = null,
    val formFields: List<FormFieldEntity> = emptyList(),
    val sortOrder: Int? = -1,
)

data class OptionModel(val uid: String, val code: String?, val displayName: String?, val sortOrder: Int?) {
    override fun toString() = displayName.orEmpty()
}

data class OrgUnit(val uid: String, val displayName: String?)

enum class OuHideStrategy { PARENT_AND_REMAINING, PARENT_AND_CHILDREN }

data class QrResult(val uid: String? = null, val displayName: String? = null) {
    companion object { fun fromJson(json: String?): QrResult? = null }
}

data class TrackedEntityAttributeModel(
    val uid: String,
    val displayFormName: String?,
    val code: String? = null,
    val optionSetUid: String? = null,
    val valueType: ValueType? = ValueType.TEXT,
    val mandatory: Boolean? = false,
    val sortOrder: Int? = -1,
)

data class TrackedEntityAttributeSectionModel(
    val uid: String,
    val code: String? = null,
    val displayName: String?,
    val description: String? = null,
    val attributes: List<TrackedEntityAttributeModel> = emptyList(),
    val sortOrder: Int? = -1,
)
