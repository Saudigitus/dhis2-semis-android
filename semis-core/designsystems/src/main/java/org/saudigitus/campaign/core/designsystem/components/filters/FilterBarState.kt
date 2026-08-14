package org.saudigitus.campaign.core.designsystem.components.filters

import org.saudigitus.campaign.core.data.models.FilterModel
import org.saudigitus.campaign.core.data.models.datastore.appconfig.RenderType
import org.saudigitus.campaign.core.utils.Constants

data class FilterBarState(
    val display: Boolean = true,
    val defaultFilters: List<FilterModel> = emptyList(),
) {
    fun chipFilters() = defaultFilters.filter { it.renderType == RenderType.CHIP }
    fun switchFilters() = defaultFilters.filter { it.renderType == RenderType.SWITCH }

    fun hasFilters() = defaultFilters.isNotEmpty()

    fun hasToCaptureCoordinates(): Boolean {
        val filter = defaultFilters.find { it.key == Constants.COORDINATES }

        return filter != null && filter.value.toBoolean()
    }

    fun getCoordinateField() = defaultFilters.find { it.key == Constants.COORDINATES }
}
