package org.saudigitus.campaign.core.data.repository

interface SyncHelperRepository {
    suspend fun downloadDatastore()
}