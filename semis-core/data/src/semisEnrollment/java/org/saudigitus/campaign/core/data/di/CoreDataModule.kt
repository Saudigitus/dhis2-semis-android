package org.saudigitus.campaign.core.data.di

import org.koin.dsl.module
import org.saudigitus.campaign.core.data.repository.EnrollmentRepository
import org.saudigitus.campaign.core.data.repository.OptionRepository
import org.saudigitus.campaign.core.data.repository.ProgramRepository
import org.saudigitus.campaign.core.data.repository.TeiRepository
import org.saudigitus.campaign.core.data.repository.impl.SemisEnrollmentOptionRepository
import org.saudigitus.campaign.core.data.repository.impl.SemisEnrollmentProgramRepository
import org.saudigitus.campaign.core.data.repository.impl.SemisEnrollmentRepository
import org.saudigitus.campaign.core.data.repository.impl.SemisEnrollmentTeiRepository

val campaignDataModule = module {
    single<ProgramRepository> { SemisEnrollmentProgramRepository(get()) }
    single<OptionRepository> { SemisEnrollmentOptionRepository(get()) }
    single<TeiRepository> { SemisEnrollmentTeiRepository(get()) }
    single<EnrollmentRepository> { SemisEnrollmentRepository(get(), get()) }
}
