package org.saudigitus.semis.core.designsystem.attendance

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
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
import org.saudigitus.semis.core.designsystem.utils.UiDefaults
import org.saudigitus.semis.core.designsystem.utils.UiDefaults.GRAY
import org.saudigitus.semis.core.designsystem.utils.UiDefaults.getIconByName
import org.saudigitus.semis.core.designsystem.theme.white

@Composable
fun AttendanceButton(
    key: String,
    modifier: Modifier = Modifier,
    state: AttendanceButtonState,
    onClick: (AttendanceButtonModel) -> Unit
) {

    if (state.buttons.isEmpty()) {
        Icon(
            modifier = modifier.then(Modifier.size(45.dp)),
            imageVector = Icons.AutoMirrored.Filled.Help,
            contentDescription = null,
            tint = Color.LightGray
        )
    } else if (state.isEditing) {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(state.buttons) { _, item ->
                val event = state.attendanceEvents.find { it.event?.tei == key }
                val isSelected = if (item.code == null) {
                    event == null
                } else {
                    event?.event?.value == item.code
                }

                IconButton(
                    onClick = {
                        onClick.invoke(item)
                    },
                    enabled = item.enabled,
                    modifier = Modifier
                        .border(
                            width = (0.15).dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(100.dp)
                        )
                        .size(45.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isSelected) {
                            event?.decorator?.containerColor
                                ?: item.color
                                ?: UiDefaults.getAttendanceStatusColor(item.key)
                        } else Color.White,
                        contentColor = if (isSelected) {
                            Color(event?.decorator?.contentColor ?: white)
                        } else {
                            item.color
                                ?: UiDefaults.getAttendanceStatusColor(item.code.orEmpty())
                        },
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
    } else {
        val event = state.attendanceEvents.find { it.event?.tei == key }

        if (event == null) {
            val present = state.buttons.firstOrNull { it.code == null }

            if (present == null) {
                Icon(
                    modifier = modifier.then(Modifier.size(45.dp)),
                    imageVector = Icons.AutoMirrored.Filled.Help,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            } else {
                IconButton(
                    onClick = {},
                    modifier = modifier.then(
                        Modifier
                            .border(
                                width = (0.15).dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(100.dp)
                            )
                            .size(45.dp)
                    ),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = present.color
                            ?: UiDefaults.getAttendanceStatusColor(present.key),
                        contentColor = Color.White,
                    )
                ) {
                    Icon(
                        imageVector = present.icon
                            ?: ImageVector.vectorResource(
                                getIconByName(present.iconName.orEmpty())
                            ),
                        contentDescription = present.name,
                    )
                }
            }
        } else {
            IconButton(
                onClick = {},
                modifier = modifier.then(
                    Modifier
                        .border(
                            width = (0.15).dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(100.dp)
                        )
                        .size(45.dp)
                ),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = event.decorator?.containerColor ?: Color.White,
                    contentColor = Color(event.decorator?.contentColor ?: GRAY),
                )
            ) {
                Icon(
                    imageVector = event.icon
                        ?: ImageVector.vectorResource(getIconByName(event.iconName.orEmpty())),
                    contentDescription = event.iconName,
                )
            }
        }
    }
}
