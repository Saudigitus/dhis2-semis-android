package org.saudigitus.semis.core.form.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.core.data.repository.ProgramStageRepository
import org.saudigitus.semis.core.data.rules.RuleEngineRepository
import org.saudigitus.semis.core.form.data.repository.FormRepository
import org.saudigitus.semis.core.form.data.repository.FormRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FormModule {

    @Provides
    @Singleton
    fun provideFormRepository(
        d2: D2,
        appConfigRepository: AppConfigRepository,
        programStageRepository: ProgramStageRepository,
        optionRepository: OptionRepository,
        ruleEngineRepository: RuleEngineRepository
    ): FormRepository = FormRepositoryImpl(
        d2, appConfigRepository, programStageRepository, optionRepository, ruleEngineRepository)
}