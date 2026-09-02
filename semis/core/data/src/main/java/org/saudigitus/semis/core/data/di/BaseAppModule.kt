package org.saudigitus.semis.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager
import javax.inject.Singleton

/**
 * The bindings SEMIS takes from the base app.
 *
 * Every SEMIS repository and ViewModel is injected with the SDK instance and with the base app's
 * [ResourceManager]. Neither is a SEMIS type, so neither can be produced by a SEMIS constructor,
 * and the base app declares them in trees SEMIS cannot reach: Koin, and Dagger components tied to
 * an activity. Until release 3.4.2 they reached the Hilt graph through the stock use case's own
 * Hilt module, which that release replaced with Koin, taking these bindings with it. Declaring
 * them here makes the SEMIS graph stand on its own instead of on another feature's wiring.
 */
@Module
@InstallIn(SingletonComponent::class)
object BaseAppModule {

    /**
     * The SDK instance the base app already created. [D2Manager.getD2] is the only supported way
     * to reach it: instantiating a second one would open a second database connection.
     */
    @Provides
    @Singleton
    fun provideD2(): D2 = D2Manager.getD2()

    @Provides
    @Singleton
    fun provideColorUtils(): ColorUtils = ColorUtils()

    /**
     * Bound to the application context on purpose. A [ResourceManager] holding an activity would
     * outlive it, and SEMIS resolves strings from singletons and ViewModels, never from a view.
     */
    @Provides
    @Singleton
    fun provideResourceManager(
        @ApplicationContext context: Context,
        colorUtils: ColorUtils,
    ): ResourceManager = ResourceManager(context, colorUtils)
}
