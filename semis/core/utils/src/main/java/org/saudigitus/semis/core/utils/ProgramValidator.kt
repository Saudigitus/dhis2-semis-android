package org.saudigitus.semis.core.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.datastore.DataStoreEntry
import javax.inject.Inject

class ProgramValidator @Inject constructor(
    private val d2: D2,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun isSEMIS(
        namespace: String,
        key: String,
        validator: (dataStore: DataStoreEntry?) -> Boolean
    ): Boolean = withContext(ioDispatcher) {
        val dataStore = d2.dataStoreModule()
            .dataStore()
            .byNamespace().eq(namespace)
            .byKey().eq(key)
            .one().blockingGet()

        return@withContext validator(dataStore)
    }
}