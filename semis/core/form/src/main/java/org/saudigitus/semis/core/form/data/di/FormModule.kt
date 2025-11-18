package org.saudigitus.semis.core.form.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.EventRepository
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.core.data.repository.ProgramStageRepository
import org.saudigitus.semis.core.data.rules.RuleEngineRepository
import org.saudigitus.semis.core.data.utils.Transformations
import org.saudigitus.semis.core.form.data.AttendanceTransformation
import org.saudigitus.semis.core.form.data.repository.AttendanceEventRepository
import org.saudigitus.semis.core.form.data.repository.AttendanceOptionRepository
import org.saudigitus.semis.core.form.data.repository.FormRepository
import org.saudigitus.semis.core.form.data.repository.impl.AttendanceEventRepositoryImpl
import org.saudigitus.semis.core.form.data.repository.impl.AttendanceOptionRepositoryImpl
import org.saudigitus.semis.core.form.data.repository.impl.FormRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FormModule {

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
    fun provideAttendanceTransformation(d2: D2, transformations: Transformations) =
        AttendanceTransformation(d2, transformations)

    @Provides
    @Singleton
    fun provideAttendanceEventRepository(
        eventRepository: EventRepository,
        appConfigRepository: AppConfigRepository,
        transformations: AttendanceTransformation
    ): AttendanceEventRepository {
        return AttendanceEventRepositoryImpl(
            eventRepository,
            appConfigRepository,
            transformations
        )
    }

    @Provides
    @Singleton
    fun provideFormRepository(
        d2: D2,
        appConfigRepository: AppConfigRepository,
        programStageRepository: ProgramStageRepository,
        optionRepository: OptionRepository,
        ruleEngineRepository: RuleEngineRepository,
        attendanceEventRepository: AttendanceEventRepository,
        attendanceOptionRepository: AttendanceOptionRepository
    ): FormRepository = FormRepositoryImpl(
        d2,
        appConfigRepository,
        programStageRepository,
        optionRepository,
        ruleEngineRepository,
        attendanceEventRepository,
        attendanceOptionRepository,
    )
}