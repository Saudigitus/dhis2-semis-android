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
import org.saudigitus.semis.core.data.model.profile.ProfileTabKind
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.avatar.AvatarInitials
import org.saudigitus.semis.core.designsystem.components.cards.LearnerCard
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.components.notice.InlineNotice
import org.saudigitus.semis.core.designsystem.components.tabs.SegmentedTabItem
import org.saudigitus.semis.core.designsystem.components.tabs.SegmentedTabRow
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.dark_warning
import org.saudigitus.semis.core.designsystem.theme.semisScreenBackground
import org.saudigitus.semis.enrollment.R
import org.saudigitus.semis.enrollment.ui.profile.components.ProfileSummary
import org.saudigitus.semis.enrollment.ui.profile.components.ProfileTabSection

/**
 * The page of one person for the academic year chosen in the home filters.
 *
 * It is built from the program metadata by the rules the web shares: who the person is at the
 * top, the figures of the year, then the tabs of the modules the program enables. The toolbar is
 * the one every other screen of the app uses.
 */
@Composable
fun StudentProfileScreen(
    state: StudentProfileUiState,
    onEvent: (StudentProfileEvent) -> Unit,
) {
    val profile = state.profile

    TopAppBarScaffold(
        toolbarHeaders = ToolbarHeaders(
            title = stringResource(R.string.profile_title),
            subtitle = profile?.academicYear?.let { stringResource(R.string.profile_academic_year, it) },
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
                    item {
                        // The same card the lists use, so the person reads the same here as on
                        // every list: photo or initials, name, identifier and tags.
                        LearnerCard(
                            name = profile.identity.name,
                            supportingText = profile.identity.identifier,
                            photoPath = profile.identity.photoPath,
                            tags = profile.identity.tags,
                            avatarColor = AvatarInitials.colorFor(state.teiUid),
                            containerColor = SemisPalette.CardSurface,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }

                    if (profile.summary.isNotEmpty()) {
                        item {
                            ProfileSummary(
                                figures = profile.summary,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                        }
                    }

                    item {
                        SegmentedTabRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            tabs = profile.tabs.map { tab ->
                                SegmentedTabItem(id = tab.kind.name, label = tabTitle(tab.kind))
                            },
                            selectedId = state.currentTab?.kind?.name.orEmpty(),
                            onSelect = { tab ->
                                onEvent(StudentProfileEvent.SelectTab(ProfileTabKind.valueOf(tab.id)))
                            },
                        )
                    }

                    item {
                        state.currentTab?.let { tab ->
                            ProfileTabSection(tab = tab, academicYear = profile.academicYear)
                        }
                    }
                }
            }
        }
    }
}

/** The tab names, the same names the menu and the web use, in the user's language. */
@Composable
private fun tabTitle(kind: ProfileTabKind): String = stringResource(
    when (kind) {
        ProfileTabKind.PERSONAL_DETAILS -> R.string.profile_tab_personal_details
        ProfileTabKind.ATTENDANCE -> R.string.profile_tab_attendance
        ProfileTabKind.PERFORMANCE -> R.string.profile_tab_performance
        ProfileTabKind.FINAL_RESULT -> R.string.profile_tab_final_result
        ProfileTabKind.TRANSFER -> R.string.profile_tab_transfer
    },
)
