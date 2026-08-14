package org.saudigitus.campaign.core.data.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.saudigitus.campaign.core.data.AppDatabase
import org.saudigitus.campaign.core.data.repository.D2MetadataHistoryRepository
import org.saudigitus.campaign.core.data.repository.DatastoreRepository
import org.saudigitus.campaign.core.data.repository.EnrollmentRepository
import org.saudigitus.campaign.core.data.repository.EventRepository
import org.saudigitus.campaign.core.data.repository.GoalRepository
import org.saudigitus.campaign.core.data.repository.LoggingRepository
import org.saudigitus.campaign.core.data.repository.OptionRepository
import org.saudigitus.campaign.core.data.repository.ProgramRepository
import org.saudigitus.campaign.core.data.repository.ProgramStageRepository
import org.saudigitus.campaign.core.data.repository.TeiRepository
import org.saudigitus.campaign.core.data.repository.impl.D2MetadataHistoryRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.DatastoreRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.EnrollmentRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.EventRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.GoalRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.LoggingRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.OptionRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.ProgramRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.ProgramStageRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.TeiRepositoryImpl
import org.saudigitus.campaign.core.data.rules.CustomRulesRepository
import org.saudigitus.campaign.core.data.rules.RuleEngineRepository
import org.saudigitus.campaign.core.data.utils.EventTransformation
import org.saudigitus.campaign.core.data.utils.Transformations
import org.saudigitus.campaign.core.data.utils.mapper.EventCardMapper
import org.saudigitus.campaign.core.data.utils.mapper.TEICardMapper

val campaignDataModule = module {
    singleOf(::RuleEngineRepository)
    singleOf(::CustomRulesRepository)
    singleOf(::EventRepositoryImpl) { bind<EventRepository>() }
    singleOf(::ProgramStageRepositoryImpl) { bind<ProgramStageRepository>() }
    singleOf(::ProgramRepositoryImpl) { bind<ProgramRepository>() }
    singleOf(::OptionRepositoryImpl) { bind<OptionRepository>() }
    singleOf(::TeiRepositoryImpl) { bind<TeiRepository>() }
    singleOf(::LoggingRepositoryImpl) { bind<LoggingRepository>() }
    singleOf(::EnrollmentRepositoryImpl) { bind<EnrollmentRepository>() }
    singleOf(::DatastoreRepositoryImpl) { bind<DatastoreRepository>() }
    singleOf(::Transformations)
    singleOf(::EventTransformation)
    singleOf(::TEICardMapper)
    singleOf(::EventCardMapper)
    singleOf(::GoalRepositoryImpl) { bind<GoalRepository>() }
    singleOf(::D2MetadataHistoryRepositoryImpl) { bind<D2MetadataHistoryRepository>() }

    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().progressDao() }
    single { get<AppDatabase>().d2MetadataHistoryDao() }
}