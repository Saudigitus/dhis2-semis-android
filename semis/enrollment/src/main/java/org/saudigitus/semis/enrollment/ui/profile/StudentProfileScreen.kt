package org.saudigitus.semis.enrollment.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.components.notice.InlineNotice
import org.saudigitus.semis.core.designsystem.components.tabs.SegmentedTabItem
import org.saudigitus.semis.core.designsystem.components.tabs.SegmentedTabRow
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.theme.dark_warning
import org.saudigitus.semis.core.designsystem.theme.semisScreenBackground
import org.saudigitus.semis.enrollment.R
import org.saudigitus.semis.enrollment.ui.profile.components.ConfiguredProfileSection
import org.saudigitus.semis.enrollment.ui.profile.components.StudentAttendanceSection
import org.saudigitus.semis.enrollment.ui.profile.components.StudentDetailsSection
import org.saudigitus.semis.enrollment.ui.profile.components.StudentPerformanceSection

/**
 * Dashboard of one learner: who they are, then the socio-economic, attendance and
 * performance data captured for them under the selection made on the home screen.
 *
 * The page is topped by the same toolbar as every other screen of the app. It carried a band of
 * its own before, with the selection repeated and counters that only made sense for a learner
 * being read for attendance, which made the one page that serves any registered person look
 * unlike the rest of the app.
 */
@Composable
fun StudentProfileScreen(
    state: StudentProfileUiState,
    onEvent: (StudentProfileEvent) -> Unit,
) {
    val profile = state.profile

    TopAppBarScaffold(
        toolbarHeaders = ToolbarHeaders(
            // The deployment says which attributes name the person and how they are joined;
            // the derived name is what a deployment that has not configured that still gets.
            title = state.configured?.identity?.title.orEmpty()
                .ifBlank { profile?.name.orEmpty() }
                .ifBlank { stringResource(R.string.profile_unknown_learner) },
            subtitle = state.configured?.identity?.subtitle.orEmpty()
                .ifBlank { profile?.systemId.orEmpty() }
                .takeIf { it.isNotBlank() },
        ),
        // Nothing on this page is filtered, so the toolbar carries sending and nothing else.
        toolbarActionState = ToolbarActionState(
            syncVisibility = true,
            filterVisibility = false,
        ),
        navigationAction = { onEvent(StudentProfileEvent.OnBack) },
        syncAction = { onEvent(StudentProfileEvent.OnSyncClick) },
    ) {
        Column(modifier = Modifier.semisScreenBackground()) {
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
                    // What the deployment configured is what the page shows. The fixed tabs remain
                    // for a deployment that has configured nothing, so upgrading the app does not
                    // empty a page that was working.
                    if (state.isConfigured) {
                        item {
                            SegmentedTabRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                tabs = configuredTabs(state),
                                selectedId = state.currentTab?.id.orEmpty(),
                                onSelect = { tab ->
                                    onEvent(StudentProfileEvent.SelectConfiguredTab(tab.id))
                                },
                            )
                        }

                        item {
                            state.currentTab?.let { tab -> ConfiguredProfileSection(tab = tab) }
                        }
                    } else {
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

/** The tabs the deployment configured, in the order it put them in. */
@Composable
private fun configuredTabs(state: StudentProfileUiState): List<SegmentedTabItem> =
    state.configured?.tabs.orEmpty().map { tab ->
        SegmentedTabItem(id = tab.id, label = tab.title)
    }
