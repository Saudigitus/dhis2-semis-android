package org.saudigitus.semis.core.designsystem.utils.mapper

import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.saudigitus.semis.core.data.model.SearchTeiModel

fun SearchTeiModel.map(
    teiCardMapper: TEICardMapper,
    onSyncIconClick: ((uid: String) -> Unit)? = null,
    showSync: Boolean = false,
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
    onImageClick: (String) -> Unit,
    onCardClick: (tei: String, enrollment: String) -> Unit
): Pair<ListCardUiModel, Boolean> {
    val card = tei.map(teiCardMapper, onImageClick = onImageClick, onCardClick = onCardClick)
    val isInactive = tei.selectedEnrollment.status() == EnrollmentStatus.CANCELLED

    return Pair(card, isInactive)
}