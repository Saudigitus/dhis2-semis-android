package org.saudigitus.campaign.core.form.di

import org.dhis2.commons.resources.ResourceManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.saudigitus.campaign.core.form.data.repository.FormRepository
import org.saudigitus.campaign.core.form.data.repository.SemisEnrollmentFormRepository
import org.saudigitus.campaign.core.form.ui.FormViewModel

val campaignFormModule = module {
    single<FormRepository> {
        SemisEnrollmentFormRepository(
            d2 = get(),
            optionRepository = get(),
            programRepository = get(),
            enrollmentRepository = get(),
        )
    }
    viewModel {
        FormViewModel(
            formRepository = get(),
            resourceManager = ResourceManager(androidContext(), get()),
        )
    }
}
