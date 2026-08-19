package org.saudigitus.semis.enrollment.ui.profile

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.data.model.profile.TeiProfile
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState

@Immutable
data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val filterDetailsState: FilterDetailsState = FilterDetailsState(
        enable = false,
        enableCounter = false,
    ),
    val profile: TeiProfile? = null,
    val selectedTab: StudentProfileTab = StudentProfileTab.DETAILS,
    val errorMessage: String? = null,
) {
    /** Subjects that hold at least one mark, which are the ones worth a counter. */
    val scoredSubjects get() = profile?.performance?.filter { it.marks.isNotEmpty() }.orEmpty()

    val hasAttendance get() = profile?.attendance?.records?.isNotEmpty() == true

    val hasPerformance get() = scoredSubjects.isNotEmpty()

    val hasSocioEconomics get() = profile?.socioEconomics?.isNotEmpty() == true
}
