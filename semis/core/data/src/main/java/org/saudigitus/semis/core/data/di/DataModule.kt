package org.saudigitus.semis.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.AppConfigRepositoryImpl
import org.saudigitus.semis.core.data.repository.AppModulesRepository
import org.saudigitus.semis.core.data.repository.AppModulesRepositoryImpl
import org.saudigitus.semis.core.data.repository.FilterRepository
import org.saudigitus.semis.core.data.repository.FilterRepositoryImpl
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.core.data.repository.OptionRepositoryImpl
import org.saudigitus.semis.core.data.repository.TeiDownloaderRepository
import org.saudigitus.semis.core.data.repository.TeiDownloaderRepositoryImpl
import org.saudigitus.semis.core.data.repository.TeiRepository
import org.saudigitus.semis.core.data.repository.TeiRepositoryImpl
import org.saudigitus.semis.core.data.rules.RuleEngineRepository
import org.saudigitus.semis.core.data.utils.Transformations
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
    fun provideOptionRepository(
        d2: D2,
        ruleEngineRepository: RuleEngineRepository
    ): OptionRepository = OptionRepositoryImpl(d2, ruleEngineRepository)

    @Provides
    @Singleton
    fun provideFilterRepository(
        configRepository: AppConfigRepository,
        optionRepository: OptionRepository
    ): FilterRepository = FilterRepositoryImpl(configRepository, optionRepository)

    @Provides
    @Singleton
    fun provideAppModulesRepository(
        configRepository: AppConfigRepository,
        resourceManager: ResourceManager
    ): AppModulesRepository = AppModulesRepositoryImpl(configRepository, resourceManager)

    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils =
        NetworkUtils(context)


    @Provides
    @Singleton
    fun provideTeiDownloaderRepository(
        d2: D2,
        networkUtils: NetworkUtils,
        resourceManager: ResourceManager
    ): TeiDownloaderRepository = TeiDownloaderRepositoryImpl(d2, networkUtils, resourceManager)

    @Provides
    @Singleton
    fun provideTeiRepository(
        d2: D2,
        transformations: Transformations
    ): TeiRepository = TeiRepositoryImpl(d2, transformations)
}
