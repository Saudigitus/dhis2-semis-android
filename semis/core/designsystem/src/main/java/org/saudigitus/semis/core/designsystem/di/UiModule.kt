package org.saudigitus.semis.core.designsystem.di

import org.koin.dsl.module
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper

val semisUiModule = module {
    single<TEICardMapper> {
        TEICardMapper(
            context = get(),
            resourceManager = get(),
        )
    }
}