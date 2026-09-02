package org.saudigitus.semis.enrollment.ui.profile

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.data.model.profile.ConfiguredProfile
import org.saudigitus.semis.core.data.model.profile.TeiProfile

@Immutable
data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val profile: TeiProfile? = null,
    val configured: ConfiguredProfile? = null,
    val selectedTab: StudentProfileTab = StudentProfileTab.DETAILS,
    val selectedTabId: String? = null,
    val errorMessage: String? = null,
) {
    /**
     * The tab being read, which is the one picked or, until something is picked, the first the
     * deployment configured.
     */
    val currentTab get() = configured?.tabs?.firstOrNull { it.id == selectedTabId }
        ?: configured?.tabs?.firstOrNull()

    /** True when the deployment configured this page, which is what decides how it is drawn. */
    val isConfigured get() = !configured?.tabs.isNullOrEmpty()

    /** Subjects that hold at least one mark, which are the ones worth a counter. */
    val scoredSubjects get() = profile?.performance?.filter { it.marks.isNotEmpty() }.orEmpty()

    val hasAttendance get() = profile?.attendance?.records?.isNotEmpty() == true

    val hasPerformance get() = scoredSubjects.isNotEmpty()

    val hasSocioEconomics get() = profile?.socioEconomics?.isNotEmpty() == true
}
