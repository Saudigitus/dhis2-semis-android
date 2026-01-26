package org.saudigitus.semis.performance.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.repository.EventRepository
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.performance.data.repository.PerformanceRepository
import org.saudigitus.semis.performance.data.repository.PerformanceRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PerformanceModule {

    @Provides
    @Singleton
    fun providePerformanceRepository(
        d2: D2,
        eventRepository: EventRepository,
        optionRepository: OptionRepository
    ) : PerformanceRepository {
        return PerformanceRepositoryImpl(d2, eventRepository, optionRepository)
    }
}