package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.saudigitus.campaign.core.data.dao.DatastoreDao
import org.saudigitus.campaign.core.data.models.entity.DatastoreEntity
import org.saudigitus.campaign.core.data.repository.ApiDatastore

class ApiDatastoreImpl(
    private val datastoreDao: DatastoreDao
): ApiDatastore {

    override suspend fun create(namespace: String, datastoreValue: String) = withContext(Dispatchers.IO) {
        datastoreDao.upsert(DatastoreEntity(key = namespace, value = datastoreValue))
    }

    override suspend fun create(datastore: DatastoreEntity) = withContext(Dispatchers.IO) {
        datastoreDao.upsert(datastore)
    }

    override suspend fun create(vararg datastore: DatastoreEntity) {
        datastoreDao.upsert(*datastore)
    }

    override suspend fun getDatastoreByKey(key: String) = withContext(Dispatchers.IO) {
        datastoreDao.getDatastoreByKey(key)
    }

    override suspend fun delete(datastore: DatastoreEntity) = withContext(Dispatchers.IO) {
        datastoreDao.delete(datastore)
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        datastoreDao.deleteAll()
    }
}