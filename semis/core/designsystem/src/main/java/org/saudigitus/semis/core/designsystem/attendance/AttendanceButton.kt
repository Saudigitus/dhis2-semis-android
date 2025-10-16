package org.saudigitus.semis.core.designsystem.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.utils.UiDefaults.GRAY
import org.saudigitus.semis.core.designsystem.utils.UiDefaults.getIconByName

@Composable
fun AttendanceButton(
    modifier: Modifier = Modifier,
    state: AttendanceButtonState,
    onClick: (AttendanceButtonModel) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(
            state.buttons.toList(),
            key = { index, item -> item.key }
        ) { index, item ->
            IconButton(
                onClick = {
                    onClick.invoke(item)
                },
                enabled = item.enabled,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = state.buttonDecorators.getOrNull(index)?.containerColor ?: Color.LightGray,
                    contentColor = Color(state.buttonDecorators.getOrNull(index)?.contentColor ?: GRAY),
                )
            ) {
                Icon(
                    imageVector = item.icon
                        ?: ImageVector.vectorResource(getIconByName(item.iconName.orEmpty())),
                    contentDescription = item.name,
                )
            }
        }
    }
}
