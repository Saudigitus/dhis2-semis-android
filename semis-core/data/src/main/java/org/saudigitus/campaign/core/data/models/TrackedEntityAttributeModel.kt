package org.saudigitus.campaign.core.data.models

import org.hisp.dhis.android.core.common.ValueType

data class TrackedEntityAttributeModel(
    val uid: String,
    val displayFormName: String?,
    val code: String? = null,
    val optionSetUid: String? = null,
    val valueType: ValueType? = ValueType.TEXT,
    val mandatory: Boolean? = false,
    val sortOrder: Int? = -1,
)
