package org.saudigitus.semis.attendance.di

import org.koin.dsl.module
import org.saudigitus.semis.attendance.ui.repository.AttendanceRepository
import org.saudigitus.semis.attendance.ui.repository.AttendanceRepositoryImpl

val attendanceModule = module {
    single<AttendanceRepository> { AttendanceRepositoryImpl(get(), get()) }
}