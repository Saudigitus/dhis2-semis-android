package org.saudigitus.campaign.core.form.di

import com.formrules.dhis2.di.dhis2RuleEngineModule
import com.formrules.di.ruleEngineModule
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.saudigitus.campaign.core.form.data.repository.FormRepository
import org.saudigitus.campaign.core.form.data.repository.FormRepositoryImpl
import org.saudigitus.campaign.core.form.rules.FormValidationRulesHelper
import org.saudigitus.campaign.core.form.rules.FormValidationRulesRepository
import org.saudigitus.campaign.core.form.ui.FormViewModel

internal val internalCampaignFormModule = module {
    singleOf(::FormRepositoryImpl) { bind<FormRepository>() }
    singleOf(::FormValidationRulesHelper) { bind<FormValidationRulesRepository>() }
    viewModelOf(::FormViewModel)
}


val campaignFormModule =
    module {
        includes(
            internalCampaignFormModule,
            ruleEngineModule,
            dhis2RuleEngineModule
        )
    }