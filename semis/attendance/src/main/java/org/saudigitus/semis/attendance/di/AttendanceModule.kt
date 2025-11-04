package org.saudigitus.semis.attendance.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.saudigitus.semis.attendance.data.AttendanceTransformation
import org.saudigitus.semis.attendance.data.repository.AttendanceEventRepository
import org.saudigitus.semis.attendance.data.repository.AttendanceEventRepositoryImpl
import org.saudigitus.semis.attendance.data.repository.AttendanceOptionRepository
import org.saudigitus.semis.attendance.data.repository.AttendanceOptionRepositoryImpl
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.EventRepository
import org.saudigitus.semis.core.data.repository.OptionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AttendanceModule {

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        optionRepository: OptionRepository,
        appConfigRepository: AppConfigRepository
    ): AttendanceOptionRepository {
        return AttendanceOptionRepositoryImpl(optionRepository, appConfigRepository)
    }

    @Provides
    @Singleton
    fun provideAttendanceEventRepository(
        eventRepository: EventRepository,
        appConfigRepository: AppConfigRepository,
        attendanceTransformation: AttendanceTransformation
    ): AttendanceEventRepository {
        return AttendanceEventRepositoryImpl(
            eventRepository,
            appConfigRepository,
            attendanceTransformation
        )
    }
}