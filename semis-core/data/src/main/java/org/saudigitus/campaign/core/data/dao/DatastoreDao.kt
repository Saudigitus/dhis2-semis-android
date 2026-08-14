package org.saudigitus.campaign.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import org.saudigitus.campaign.core.data.models.entity.DatastoreEntity

@Dao
interface DatastoreDao {
    @Upsert
    suspend fun upsert(vararg datastore: DatastoreEntity)

      @Query("SELECT * FROM datastoreentity WHERE datastoreentity.`key` = :key")
    suspend fun getDatastoreByKey(key: String): DatastoreEntity?

    @Delete
    suspend fun delete(datastore: DatastoreEntity)

    @Query("DELETE FROM datastoreentity")
    suspend fun deleteAll()
}