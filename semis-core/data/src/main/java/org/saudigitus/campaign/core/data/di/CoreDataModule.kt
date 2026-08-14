package org.saudigitus.campaign.core.data.di

import org.koin.dsl.module
import org.saudigitus.campaign.core.data.repository.EnrollmentRepository
import org.saudigitus.campaign.core.data.repository.OptionRepository
import org.saudigitus.campaign.core.data.repository.ProgramRepository
import org.saudigitus.campaign.core.data.repository.TeiRepository
import org.saudigitus.campaign.core.data.repository.impl.EnrollmentRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.ProgramRepositoryImpl
import org.saudigitus.campaign.core.data.repository.impl.SemisEnrollmentOptionRepository
import org.saudigitus.campaign.core.data.repository.impl.SemisEnrollmentTeiRepository

val campaignDataModule = module {
    single<ProgramRepository> { ProgramRepositoryImpl(get(), get()) }
    single<OptionRepository> { SemisEnrollmentOptionRepository(get()) }
    single<TeiRepository> { SemisEnrollmentTeiRepository(get(), get()) }
    single<EnrollmentRepository> { EnrollmentRepositoryImpl(get(), get(), get()) }
}
