package org.saudigitus.semis.core.designsystem.utils

import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.utils.Constants.ABSENTEEISM
import org.saudigitus.semis.core.utils.Constants.ATTENDANCE
import org.saudigitus.semis.core.utils.Constants.PERFORMANCE
import org.saudigitus.semis.core.utils.Constants.TERMS
import org.saudigitus.semis.core.utils.Constants.TRANSFER

object ModuleIcons {
    fun getModuleIconByName(name: String) = when (name) {
        ATTENDANCE -> R.drawable.s_calendar
        ABSENTEEISM -> R.drawable.s_calendar
        PERFORMANCE -> R.drawable.performance
        TRANSFER -> R.drawable.transfer
        TERMS -> R.drawable.education
        else -> R.drawable.s_calendar
    }
}
