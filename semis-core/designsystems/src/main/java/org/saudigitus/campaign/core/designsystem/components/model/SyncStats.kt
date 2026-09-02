package org.saudigitus.campaign.core.designsystem.components.model

data class SyncStats(
    val pending: Int = 0,
    val synced: Int = 0,
    val total: Int = 0,
)