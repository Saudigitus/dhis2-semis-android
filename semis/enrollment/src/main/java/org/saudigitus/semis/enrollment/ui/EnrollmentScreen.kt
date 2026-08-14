package org.saudigitus.semis.enrollment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.FilterDetails
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import org.saudigitus.semis.core.designsystem.utils.mapper.searchTeiMapper

@Composable
fun EnrollmentScreen(
    programName: String,
    tei: List<SearchTeiModel>,
    filterState: FilterComponentState,
    teiCardMapper: TEICardMapper,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onDisplayImage: (String) -> Unit,
    onNewEnrollment: () -> Unit,
) {
    TopAppBarScaffold(
        toolbarHeaders = ToolbarHeaders(title = programName),
        navigationAction = onBack,
        syncAction = onSync,
        bottomBar = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                enabled = filterState.orgUnit != null,
                onClick = onNewEnrollment,
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.semis_new_enrollment),
                )
            }
        },
    ) {
        FilterDetails(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .dropShadow(RoundedCornerShape(Radius.S))
                .background(
                    color = SurfaceColor.SurfaceBright,
                    shape = RoundedCornerShape(Radius.S),
                ),
            state = filterState.filterDetailsState,
        )

        if (tei.isEmpty()) {
            NoResults(message = stringResource(R.string.no_records_found))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(tei, key = { it.tei.uid() }) { student ->
                    val card = searchTeiMapper(
                        tei = student,
                        teiCardMapper = teiCardMapper,
                        onImageClick = onDisplayImage,
                        onCardClick = { _, _ -> },
                    )

                    ListCard(
                        modifier = Modifier.fillParentMaxWidth().testTag("ENROLLMENT_TEI_ITEM"),
                        listCardState = rememberListCardState(
                            title = ListCardTitleModel(text = card.first.title, allowOverflow = false),
                            description = card.first.description?.let { ListCardDescriptionModel(text = it) },
                            lastUpdated = card.first.lastUpdated,
                            additionalInfoColumnState = rememberAdditionalInfoColumnState(
                                additionalInfoList = card.first.additionalInfo,
                                syncProgressItem = AdditionalInfoItem(
                                    key = stringResource(R.string.syncing),
                                    value = "",
                                ),
                                expandLabelText = stringResource(R.string.show_more),
                                shrinkLabelText = stringResource(R.string.show_less),
                                scrollableContent = true,
                            ),
                        ),
                        onCardClick = card.first.onCardCLick,
                        listAvatar = card.first.avatar,
                    )
                }
            }
        }
    }
}
