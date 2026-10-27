package org.saudigitus.campaign.core.data.models

data class EventWithDataValues(
    val eventUid: String,
    val eventDate: String?,
    val programStageUid: String?,
    val programStageName: String? = null,
    val status: String?,
    val orgUnitName: String?,
    val dataValues: List<DataValueItem>,
)

data class DataValueItem(
    val dataElementUid: String,
    val label: String,
    val value: String,
)
