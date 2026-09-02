package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.model.app_config.SyncMode
import org.saudigitus.semis.core.data.model.schoolcalendar_config.SchoolCalendarConfig

interface AppConfigRepository {
    suspend fun getAppConfig(program: String): SEMISConfigItem?
    suspend fun getSchoolCalendar(): SchoolCalendarConfig?

    /**
     * What the deployment wants the app to do once a record is written.
     *
     * Answers DEFAULT whenever the deployment has not said otherwise, including when the setting
     * cannot be read at all: how records are sent must never be what stops them being captured.
     */
    suspend fun getSyncMode(): SyncMode
    /**
     * Whether a day may carry attendance, judged by the calendar of [academicYearCode].
     *
     * The year is asked for rather than assumed, because a user working in an earlier year has to
     * be judged by that year's calendar. Where nothing is selected the configured default stands.
     */
    fun allowedCalenderYearDates(
        dateLong: Long,
        schoolCalendar: SchoolCalendarConfig?,
        academicYearCode: String? = null,
    ): Boolean
}

