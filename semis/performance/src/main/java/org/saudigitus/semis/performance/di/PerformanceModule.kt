package org.saudigitus.semis.performance.di

import org.koin.dsl.module
import org.saudigitus.semis.performance.data.repository.PerformanceRepository
import org.saudigitus.semis.performance.data.repository.PerformanceRepositoryImpl


val performanceModule = module {
    single<PerformanceRepository> { PerformanceRepositoryImpl(get(), get(), get()) }
}