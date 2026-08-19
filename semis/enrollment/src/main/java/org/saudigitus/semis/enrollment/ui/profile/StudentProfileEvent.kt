package org.saudigitus.semis.enrollment.ui.profile

sealed interface StudentProfileEvent {
    data object OnBack : StudentProfileEvent
    data class SelectTab(val tab: StudentProfileTab) : StudentProfileEvent
}
