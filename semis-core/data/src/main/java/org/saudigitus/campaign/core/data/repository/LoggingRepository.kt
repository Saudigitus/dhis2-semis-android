package org.saudigitus.campaign.core.data.repository

interface LoggingRepository {
    suspend fun exportAllTeis(): String?
    suspend fun exportSyncedTeis(): String?
    suspend fun exportNotSyncedTeis(): String?
    suspend fun forceSyncAllTeis(): String?
    suspend fun forceSyncSyncedTeis(): String?
    suspend fun forceSyncNotSyncedTeis(): String?
}
