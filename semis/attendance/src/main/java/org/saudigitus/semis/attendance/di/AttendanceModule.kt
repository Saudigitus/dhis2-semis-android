package org.saudigitus.semis.attendance.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.attendance.ui.repository.AttendanceRepository
import org.saudigitus.semis.attendance.ui.repository.AttendanceRepositoryImpl
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.form.data.repository.FormRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AttendanceModule {
    @Provides
    @Singleton
    fun provideAttendanceRepository(
        d2: D2,
        appConfigRepository: AppConfigRepository,
        formRepository: FormRepository
    ): AttendanceRepository = AttendanceRepositoryImpl(d2, appConfigRepository, formRepository)
}
