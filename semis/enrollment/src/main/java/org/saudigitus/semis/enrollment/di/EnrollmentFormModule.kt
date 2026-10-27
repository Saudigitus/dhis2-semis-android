package org.saudigitus.semis.enrollment.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.koin.core.context.GlobalContext
import org.saudigitus.campaign.core.form.data.repository.FormRepository
import org.saudigitus.semis.enrollment.ui.form.initializeSemisCoreForm

/**
 * Reaches the campaign form repository from the SEMIS side of the app.
 *
 * The enrollment form is built from the campaign modules, which are wired with Koin, while the
 * screen that drives the enrollment is wired with Hilt. This is the one seam between the two, and
 * it resolves the instance Koin already owns rather than building a second one, so both sides act
 * on the same repository.
 */
@Module
@InstallIn(SingletonComponent::class)
object EnrollmentFormModule {

    /**
     * The campaign modules are loaded on demand, so loading them here as well is what keeps this
     * working when the screen is reached without passing through the action that normally loads
     * them, such as the system restoring the app onto the enrollment form after it was killed.
     */
    @Provides
    fun provideCampaignFormRepository(): FormRepository {
        initializeSemisCoreForm()
        return GlobalContext.get().get()
    }
}
