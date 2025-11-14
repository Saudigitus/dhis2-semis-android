package org.saudigitus.semis.attendance.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.dhis2.ui.theme.colorPrimary
import org.saudigitus.semis.core.designsystem.components.FilterDetails
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.ShowCount
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel
import org.saudigitus.semis.core.designsystem.utils.UiDefaults


data class AttendanceSummaryState(
    val filterDetailsState: FilterDetailsState = FilterDetailsState(),
    val bottomSheetModels: List<BottomSheetModel> = emptyList(),
    val enableBulk: Boolean = false,
)


@Composable
fun AttendanceSummary(
    modifier: Modifier = Modifier,
    state: AttendanceSummaryState,
    onBulk: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilterDetails(
            modifier = Modifier.fillMaxWidth(),
            state = state.filterDetailsState,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(state.bottomSheetModels) { item ->
                    ShowCount(
                        imageVector = item.icon ?: ImageVector.vectorResource(UiDefaults.getIconByName(item.iconName.orEmpty())),
                        count = item.value?.toInt() ?: 0,
                        color = item.color ?: Color.LightGray
                    )
                }
            }

            IconButton(
                onClick = onBulk,
                enabled = state.enableBulk,
                modifier = Modifier
                    .border(
                        width = (0.15).dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .size(45.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Rocket,
                    contentDescription = null,
                    tint = colorPrimary
                )
            }
        }
    }
}