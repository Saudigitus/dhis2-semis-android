package org.saudigitus.semis.enrollment.ui.profile

sealed interface StudentProfileEvent {
    data object OnBack : StudentProfileEvent
    data class SelectTab(val tab: StudentProfileTab) : StudentProfileEvent

    /** A tab of a configured page, identified by what the deployment named it. */
    data class SelectConfiguredTab(val tabId: String) : StudentProfileEvent
}
