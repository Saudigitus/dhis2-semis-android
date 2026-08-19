package org.saudigitus.semis.enrollment.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.components.notice.InlineNotice
import org.saudigitus.semis.core.designsystem.components.tabs.SegmentedTabItem
import org.saudigitus.semis.core.designsystem.components.tabs.SegmentedTabRow
import org.saudigitus.semis.core.designsystem.templates.RoundedHeaderScaffold
import org.saudigitus.semis.core.designsystem.theme.dark_warning
import org.saudigitus.semis.enrollment.R
import org.saudigitus.semis.enrollment.ui.profile.components.StudentAttendanceSection
import org.saudigitus.semis.enrollment.ui.profile.components.StudentDetailsSection
import org.saudigitus.semis.enrollment.ui.profile.components.StudentPerformanceSection
import org.saudigitus.semis.enrollment.ui.profile.components.StudentProfileHeader

/**
 * Dashboard of one learner: who they are, then the socio-economic, attendance and
 * performance data captured for them under the selection made on the home screen.
 */
@Composable
fun StudentProfileScreen(
    state: StudentProfileUiState,
    onEvent: (StudentProfileEvent) -> Unit,
) {
    val profile = state.profile

    RoundedHeaderScaffold(
        header = {
            StudentProfileHeader(
                name = profile?.name.orEmpty()
                    .ifBlank { stringResource(R.string.profile_unknown_learner) },
                systemId = profile?.systemId.orEmpty(),
                tiles = studentProfileTiles(
                    recordedDaysLabel = stringResource(R.string.profile_recorded),
                    profile = profile,
                ),
                filterDetailsState = state.filterDetailsState,
                onNavigateBack = { onEvent(StudentProfileEvent.OnBack) },
            )
        },
    ) {
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            profile == null -> InlineNotice(
                text = state.errorMessage
                    ?: stringResource(R.string.profile_not_found),
                imageVector = Icons.Default.Warning,
                tone = dark_warning,
                modifier = Modifier.padding(16.dp),
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = 24.dp,
                ),
            ) {
                item {
                    SegmentedTabRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        tabs = profileTabs(state),
                        selectedId = state.selectedTab.name,
                        onSelect = { tab ->
                            onEvent(
                                StudentProfileEvent.SelectTab(
                                    StudentProfileTab.valueOf(tab.id),
                                ),
                            )
                        },
                    )
                }

                item {
                    when (state.selectedTab) {
                        StudentProfileTab.DETAILS -> StudentDetailsSection(profile = profile)
                        StudentProfileTab.ATTENDANCE -> StudentAttendanceSection(
                            history = profile.attendance,
                        )

                        StudentProfileTab.PERFORMANCE -> StudentPerformanceSection(
                            subjects = state.scoredSubjects,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun profileTabs(state: StudentProfileUiState): List<SegmentedTabItem> = listOf(
    SegmentedTabItem(
        id = StudentProfileTab.DETAILS.name,
        label = stringResource(R.string.profile_tab_details),
    ),
    SegmentedTabItem(
        id = StudentProfileTab.ATTENDANCE.name,
        label = stringResource(R.string.profile_tab_attendance),
        badge = state.profile?.attendance?.recordedDays
            ?.takeIf { it > 0 }
            ?.toString(),
    ),
    SegmentedTabItem(
        id = StudentProfileTab.PERFORMANCE.name,
        label = stringResource(R.string.profile_tab_performance),
        badge = state.scoredSubjects.size.takeIf { it > 0 }?.toString(),
    ),
)
