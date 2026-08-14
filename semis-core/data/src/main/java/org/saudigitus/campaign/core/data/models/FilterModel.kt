package org.saudigitus.campaign.core.data.models

import org.saudigitus.campaign.core.data.models.datastore.appconfig.FilterType
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FilterProcessor
import org.saudigitus.campaign.core.data.models.datastore.appconfig.RenderType

data class FilterModel(
    val uid: String,
    val key: String? = null,
    val displayName: String,
    val optionModel: List<OptionModel> = emptyList(),
    val type: FilterType,
    val renderType: RenderType,
    val programStage: String? = null,
    val filterProcessor: FilterProcessor? = null,
    val range: Int? = 5,
    val value: String? = null,
    val selectedOption: OptionModel? = null,
)
