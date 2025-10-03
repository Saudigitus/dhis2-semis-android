package org.saudigitus.semis.app.utils

import org.saudigitus.semis.app.R
import org.saudigitus.semis.app.presentation.model.Module

object DataFaker {
    val homeModules = listOf(
        Module(
            title = "Attendance",
            description = TODO(),
            icon = R.drawable.s_calendar,
            route = "attendance",
        ),
        Module(
            title = "Performance",
            description = TODO(),
            icon = R.drawable.performance,
            route = "performance",
        ),
    )
}