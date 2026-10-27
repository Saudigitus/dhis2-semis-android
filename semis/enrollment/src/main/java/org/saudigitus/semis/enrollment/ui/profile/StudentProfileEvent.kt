package org.saudigitus.semis.enrollment.ui.profile

import org.saudigitus.semis.core.data.model.profile.ProfileTabKind

sealed interface StudentProfileEvent {
    data object OnBack : StudentProfileEvent

    /** The reader asked for what the device is holding to be sent, from the toolbar. */
    data object OnSyncClick : StudentProfileEvent

    data class SelectTab(val tab: ProfileTabKind) : StudentProfileEvent
}
