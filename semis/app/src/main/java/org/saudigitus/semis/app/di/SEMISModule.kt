package org.saudigitus.semis.app.di

import org.koin.dsl.module
import org.saudigitus.semis.attendance.di.attendanceModule
import org.saudigitus.semis.core.data.di.semisDataModule
import org.saudigitus.semis.core.designsystem.di.semisUiModule
import org.saudigitus.semis.performance.di.performanceModule

val semisModule = module {
    includes(
        semisDataModule,
        semisUiModule,
        performanceModule,
        attendanceModule,

    )
}