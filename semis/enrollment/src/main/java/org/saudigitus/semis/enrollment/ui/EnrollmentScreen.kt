package org.saudigitus.semis.enrollment.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.core.designsystem.components.SemisFilterDetails
import org.saudigitus.semis.core.designsystem.components.buttons.SemisActionBar
import org.saudigitus.semis.core.designsystem.components.cards.TeiLearnerCard
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.theme.semisScreenBackground

@Composable
fun EnrollmentScreen(
    programName: String,
    tei: List<SearchTeiModel>,
    filterState: FilterComponentState,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onNewEnrollment: () -> Unit,
    onTeiClick: (teiUid: String) -> Unit = {},
) {
    TopAppBarScaffold(
        toolbarHeaders = ToolbarHeaders(title = programName),
        navigationAction = onBack,
        syncAction = onSync,
        bottomBar = {
            SemisActionBar(
                label = stringResource(R.string.semis_new_enrollment),
                icon = Icons.Rounded.Add,
                enabled = filterState.orgUnit != null,
                onClick = onNewEnrollment,
            )
        },
    ) {
        Column(
            modifier = Modifier.semisScreenBackground(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        ) {
            SemisFilterDetails(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                state = filterState.filterDetailsState,
                showChevron = false,
            )

            if (tei.isEmpty()) {
                NoResults(message = stringResource(R.string.no_records_found))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 4.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                ) {
                    items(tei, key = { it.tei.uid() }) { student ->
                        TeiLearnerCard(
                            tei = student,
                            modifier = Modifier.testTag("ENROLLMENT_TEI_ITEM"),
                            maxAdditionalInfo = 0,
                            onClick = { onTeiClick(student.tei.uid()) },
                        )
                    }
                }
            }
        }
    }
}
