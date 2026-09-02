package org.saudigitus.campaign.core.navigation


import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {

    val orgUnitUid: String? get() = null
    val programUid: String? get() = null


    @Serializable
    data class TrackerListingRoute(
        override val programUid: String,
        val entityType: String? = null,
    ) : AppRoute

    @Serializable
    data class TrackerDetailRoute(
        override val programUid: String,
        val trackedEntityUid: String,
        val enrollmentUid: String
    ) : AppRoute

    @Serializable
    data class FormRoute(
        val formType: String = FormType.NEW_ENROLLMENT,
        override val programUid: String? = null,
        override val orgUnitUid: String? = null,
        val orgUnitName: String? = null,
        val enrollmentUid: String? = null,
        val trackedEntityUid: String? = null,
        val programStageUid: String? = null,
    ) : AppRoute

    @Serializable
    data class EventRoute(
        override val programUid: String?,
        val programName: String? = null,
    ): AppRoute

    @Serializable
    data object LoggingRoute : AppRoute

}
