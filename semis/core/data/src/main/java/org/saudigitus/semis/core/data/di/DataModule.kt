package org.saudigitus.semis.core.data.di

import org.dhis2.commons.network.NetworkUtils
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.AppConfigRepositoryImpl
import org.saudigitus.semis.core.data.repository.AppModulesRepository
import org.saudigitus.semis.core.data.repository.AppModulesRepositoryImpl
import org.saudigitus.semis.core.data.repository.EventRepository
import org.saudigitus.semis.core.data.repository.EventRepositoryImpl
import org.saudigitus.semis.core.data.repository.FilterRepository
import org.saudigitus.semis.core.data.repository.FilterRepositoryImpl
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.core.data.repository.OptionRepositoryImpl
import org.saudigitus.semis.core.data.repository.ProgramStageRepository
import org.saudigitus.semis.core.data.repository.ProgramStageRepositoryImpl
import org.saudigitus.semis.core.data.repository.TeiDownloaderRepository
import org.saudigitus.semis.core.data.repository.TeiDownloaderRepositoryImpl
import org.saudigitus.semis.core.data.repository.TeiRepository
import org.saudigitus.semis.core.data.repository.TeiRepositoryImpl
import org.saudigitus.semis.core.data.repository.TeiTransferRepository
import org.saudigitus.semis.core.data.repository.TeiTransferRepositoryImpl

val semisDataModule = module {

    single<AppConfigRepository> {
        AppConfigRepositoryImpl(
            d2 = get(),
        )
    }

    single<OptionRepository> {
        OptionRepositoryImpl(
            d2 = get(),
            ruleEngineRepository = get(),
        )
    }

    single<FilterRepository> {
        FilterRepositoryImpl(
            configRepository = get(),
            optionRepository = get(),
        )
    }

    single<AppModulesRepository> {
        AppModulesRepositoryImpl(
            configRepository = get(),
            resourceManager = get(),
        )
    }

    single<NetworkUtils> {
        NetworkUtils(
            context = androidContext(),
        )
    }

    single<TeiDownloaderRepository> {
        TeiDownloaderRepositoryImpl(
            d2 = get(),
            networkUtils = get(),
            resourceManager = get(),
        )
    }

    single<TeiRepository> {
        TeiRepositoryImpl(
            d2 = get(),
            transformations = get(),
        )
    }

    single<TeiTransferRepository> {
        TeiTransferRepositoryImpl(
            d2 = get(),
            appConfigRepository = get(),
            eventRepository = get(),
            transformations = get(),
            resourceManager = get(),
        )
    }

    single<EventRepository> {
        EventRepositoryImpl(
            d2 = get(),
        )
    }

    single<ProgramStageRepository> {
        ProgramStageRepositoryImpl(
            d2 = get(),
        )
    }
}