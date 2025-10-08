package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.model.schoolcalendar_config.SchoolCalendarConfig

interface AppConfigRepository {
    suspend fun getAppConfig(program: String): SEMISConfigItem?
    suspend fun getSchoolCalendar(): SchoolCalendarConfig?
}

