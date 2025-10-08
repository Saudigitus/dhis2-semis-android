package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.model.app_config.SEMISConfig
import org.saudigitus.semis.core.data.model.schoolcalendar_config.SchoolCalendarConfig
import org.saudigitus.semis.core.utils.Constants.CALENDAR_KEY
import org.saudigitus.semis.core.utils.Constants.DATASTORE_KEY
import org.saudigitus.semis.core.utils.Constants.DATASTORE_NAMESPACE
import org.saudigitus.semis.core.utils.JsonMapper
import org.saudigitus.semis.core.utils.decodeJson
import javax.inject.Inject

class AppConfigRepositoryImpl
@Inject constructor(
    val d2: D2,
) : AppConfigRepository {
    override suspend fun getAppConfig(program: String) = withContext(Dispatchers.IO) {
        val dataStore = d2.dataStoreModule()
            .dataStore()
            .byNamespace().eq(DATASTORE_NAMESPACE)
            .byKey().eq(DATASTORE_KEY)
            .one().blockingGet()

        val decodedJson = decodeJson(dataStore?.value())
        val semisConfig = SEMISConfig.fromJson(decodedJson)

        return@withContext semisConfig?.firstOrNull { it.program == program }
    }

    override suspend fun getSchoolCalendar() = withContext(Dispatchers.IO) {
        val dataStore = d2.dataStoreModule()
            .dataStore()
            .byNamespace().eq(DATASTORE_NAMESPACE)
            .byKey().eq(CALENDAR_KEY)
            .one().blockingGet()

        val decodedJson = decodeJson(dataStore?.value())
        val calendarConfig = JsonMapper.json.decodeFromString<SchoolCalendarConfig?>(decodedJson)

        return@withContext  calendarConfig
    }
}