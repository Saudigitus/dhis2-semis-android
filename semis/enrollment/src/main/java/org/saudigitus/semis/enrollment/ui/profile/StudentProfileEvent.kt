package org.saudigitus.semis.enrollment.ui.profile

sealed interface StudentProfileEvent {
    data object OnBack : StudentProfileEvent

    /** The reader asked for what the device is holding to be sent, from the toolbar. */
    data object OnSyncClick : StudentProfileEvent
    data class SelectTab(val tab: StudentProfileTab) : StudentProfileEvent

    /** A tab of a configured page, identified by what the deployment named it. */
    data class SelectConfiguredTab(val tabId: String) : StudentProfileEvent
}
