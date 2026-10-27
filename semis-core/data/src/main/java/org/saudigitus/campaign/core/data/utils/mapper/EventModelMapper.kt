package org.saudigitus.campaign.core.data.utils.mapper

import org.dhis2.commons.data.EventModel

fun EventModel.map(
    eventCardMapper: EventCardMapper,
    onCardClick: (event: String, programStage: String) -> Unit = { _, _ -> },
    onSyncIconClick: (String) -> Unit,
) = eventCardMapper.map(
    event = this,
    onCardClick = {
        onCardClick(
            this.event?.uid().orEmpty(),
            this.event?.programStage().orEmpty()
        )
    },
    onSyncIconClick = onSyncIconClick,
)