package org.saudigitus.campaign.core.data.repository

import org.saudigitus.campaign.core.data.models.entity.DatastoreEntity

interface ApiDatastore {
    suspend fun create(namespace: String, datastoreValue: String)
    suspend fun create(datastore: DatastoreEntity)
    suspend fun create(vararg datastore: DatastoreEntity)

    suspend fun getDatastoreByKey(key: String): DatastoreEntity?

    suspend fun delete(datastore: DatastoreEntity)

    suspend fun deleteAll()
}