package org.saudigitus.semis.core.data.repository

import androidx.compose.ui.util.fastFilterNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.model.app_config.SEMISConfig
import org.saudigitus.semis.core.data.model.schoolcalendar_config.Holiday
import org.saudigitus.semis.core.data.model.schoolcalendar_config.SchoolCalendarConfig
import org.saudigitus.semis.core.utils.Constants.CALENDAR_KEY
import org.saudigitus.semis.core.utils.Constants.DATASTORE_KEY
import org.saudigitus.semis.core.utils.Constants.DATASTORE_NAMESPACE
import org.saudigitus.semis.core.utils.DateHelper
import org.saudigitus.semis.core.utils.DateHelper.formatDate
import org.saudigitus.semis.core.utils.DateHelper.stringToLocalDate
import org.saudigitus.semis.core.utils.JsonMapper
import org.saudigitus.semis.core.utils.decodeJson
import java.time.ZoneId
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

        return@withContext calendarConfig
    }

    override fun allowedCalenderYearDates(dateLong: Long, schoolCalendar: SchoolCalendarConfig?): Boolean {
        val default = schoolCalendar?.defaults
        val currentSchoolCalendar = schoolCalendar?.schoolCalendar?.find {
            it?.academicYear?.code == default?.academicYear
        }

        val date = stringToLocalDate(formatDate(dateLong)!!)
        val today = System.currentTimeMillis()

        return if (schoolCalendar != null && currentSchoolCalendar != null) {
            val startDate = currentSchoolCalendar.academicYear?.startDate
            val endDate = currentSchoolCalendar.academicYear?.endDate

            val startMillis = stringToLocalDate(startDate!!)
                .atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()?.toEpochMilli()!!
            val endMillis = stringToLocalDate(endDate!!)
                .atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()?.toEpochMilli()!!

            (
                !DateHelper.isWeekend(date) && currentSchoolCalendar.weekDays?.saturday == false &&
                    currentSchoolCalendar.weekDays.sunday == false
                ) &&
                currentSchoolCalendar.holidays?.let { holiday ->
                    isHoliday(holiday.fastFilterNotNull(), dateLong)
                } == true && (dateLong in startMillis..endMillis) && dateLong <= today
        } else {
            dateLong <= today
        }
    }

    private fun isHoliday(holidays: List<Holiday>, date: Long): Boolean {
        return holidays.none { holiday ->
            holiday.date == formatDate(date)
        }
    }
}