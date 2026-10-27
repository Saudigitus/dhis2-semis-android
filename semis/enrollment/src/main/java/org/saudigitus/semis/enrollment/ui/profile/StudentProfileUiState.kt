package org.saudigitus.semis.enrollment.ui.profile

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.data.model.profile.PersonProfile
import org.saudigitus.semis.core.data.model.profile.ProfileTabKind

@Immutable
data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val teiUid: String = "",
    val profile: PersonProfile? = null,
    val selectedTab: ProfileTabKind = ProfileTabKind.PERSONAL_DETAILS,
    val errorMessage: String? = null,
) {
    /** The tab being read: the one picked, or Personal details until something is picked. */
    val currentTab get() = profile?.tabs?.firstOrNull { it.kind == selectedTab }
        ?: profile?.tabs?.firstOrNull()
}
