package org.saudigitus.semis.core.designsystem.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.fields.DropDown
import org.saudigitus.semis.core.designsystem.components.fields.OuField
import org.saudigitus.semis.core.designsystem.components.model.FilterType
import org.saudigitus.semis.core.designsystem.theme.SemisFieldShape

@Composable
fun FilterContainer(
    modifier: Modifier = Modifier,
    program: String,
    state: FilterComponentState,
    onEvent: (FilterComponentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        state.academicYear?.let {
            DropDown(
                dropdownState = it,
                defaultSelection = state.selectedFilters
                    .getOrElse(FilterType.ACADEMIC_YEAR) { null },
                onItemClick = { item ->
                    onEvent(
                        FilterComponentEvent.FilterValueChange(
                            FilterType.ACADEMIC_YEAR,
                            item
                        )
                    )
                }
            )
        }
        OuField(
            placeholder = stringResource(R.string.school),
            leadingIcon = ImageVector.vectorResource(R.drawable.ic_location_on),
            selectedOrgUnit = state.orgUnit,
            program = program,
            onItemClick = {
                onEvent(FilterComponentEvent.FilterValueChange(FilterType.SCHOOL, it))
            }
        )
        AnimatedVisibility(visible = state.filters.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
            ) {
                items(state.filters) { filter ->
                    DropDown(
                        dropdownState = filter,
                        defaultSelection = state.selectedFilters
                            .getOrElse(filter.filterType) { null },
                        onItemClick = { item ->
                            onEvent(
                                FilterComponentEvent.FilterValueChange(
                                    filter.filterType,
                                    item
                                )
                            )
                        }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterActionButton(
                modifier = Modifier.weight(DOWNLOAD_WEIGHT),
                label = stringResource(R.string.filters_download_records),
                icon = Icons.Default.Download,
                filled = true,
                onClick = { onEvent(FilterComponentEvent.Sync) },
            )
            FilterActionButton(
                modifier = Modifier.weight(RESET_WEIGHT),
                label = stringResource(R.string.filters_reset),
                icon = Icons.Rounded.FilterAltOff,
                filled = false,
                onClick = { onEvent(FilterComponentEvent.ResetFilters) },
            )
        }
    }
}

/**
 * One of the two actions that close the filters.
 *
 * Both are drawn by the same composable so that they cannot drift apart in height, shape or type:
 * side by side, any difference between them reads as meaning rather than as an accident. What does
 * separate them is weight, filled for the action the screen is there for and outlined for the way
 * back.
 *
 * The corner is the one every field in SEMIS is drawn with, so that a filter, a form field and
 * the action that follows them read as parts of the same app.
 */
@Composable
private fun FilterActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    filled: Boolean,
    onClick: () -> Unit,
) {
    val content: @Composable RowScope.() -> Unit = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = icon,
                contentDescription = null,
            )
            Text(
                text = label,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily(Font(R.font.rubik_medium)),
            )
        }
    }

    if (filled) {
        Button(
            modifier = modifier.height(ACTION_HEIGHT),
            onClick = onClick,
            shape = SemisFieldShape,
            contentPadding = ACTION_PADDING,
            content = content,
        )
    } else {
        OutlinedButton(
            modifier = modifier.height(ACTION_HEIGHT),
            onClick = onClick,
            shape = SemisFieldShape,
            contentPadding = ACTION_PADDING,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
            content = content,
        )
    }
}

private val ACTION_HEIGHT = 52.dp
private val ACTION_PADDING = PaddingValues(horizontal = 10.dp)

/**
 * How the line is divided between the two actions.
 *
 * The download comes first and takes the larger share, being what the screen is there for, and the
 * way back follows it with enough room to name what it clears.
 */
private const val DOWNLOAD_WEIGHT = 3f
private const val RESET_WEIGHT = 2f
