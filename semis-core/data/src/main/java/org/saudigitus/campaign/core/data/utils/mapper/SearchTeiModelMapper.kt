package org.saudigitus.campaign.core.data.utils.mapper

import org.dhis2.commons.ui.model.ListCardUiModel
import org.saudigitus.campaign.core.data.models.SearchTeiModel

fun SearchTeiModel.map(
    teiCardMapper: TEICardMapper,
    onSyncIconClick: ((uid: String) -> Unit)? = null,
    showSync: Boolean = true,
    onImageClick: (String) -> Unit,
    onCardClick: (tei: String, enrollment: String) -> Unit = { _, _ -> },
) = teiCardMapper.map(
    searchTEIModel = this,
    onSyncIconClick = {
        if (onSyncIconClick != null) {
            onSyncIconClick(this.uid())
        }
    },
    onCardClick = {
        onCardClick(this.uid(), this.enrollments.getOrNull(0)?.uid() ?: "")
    },
    onImageClick = onImageClick,
    showSync = showSync,
)

fun searchTeiMapper(
    tei: SearchTeiModel,
    teiCardMapper: TEICardMapper,
    onSyncIconClick: (uid: String) -> Unit = {},
    onImageClick: (String) -> Unit,
    onCardClick: (tei: String, enrollment: String) -> Unit
): Pair<ListCardUiModel, Boolean> {
    val card = tei.map(
        teiCardMapper,
        onSyncIconClick = onSyncIconClick,
        onImageClick = onImageClick,
        onCardClick = onCardClick,
    )

    return Pair(card, false)
}
