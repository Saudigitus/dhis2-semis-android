package org.saudigitus.semis.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.AppConfigRepositoryImpl
import org.saudigitus.semis.core.data.repository.FilterRepository
import org.saudigitus.semis.core.data.repository.FilterRepositoryImpl
import org.saudigitus.semis.core.data.rules.RuleEngineRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppConfigRepository(d2: D2): AppConfigRepository =
        AppConfigRepositoryImpl(d2)

    @Provides
    @Singleton
    fun provideFilterRepository(
        d2: D2,
        configRepository: AppConfigRepository,
        ruleEngineRepository: RuleEngineRepository
    ): FilterRepository = FilterRepositoryImpl(d2, configRepository, ruleEngineRepository)
}
