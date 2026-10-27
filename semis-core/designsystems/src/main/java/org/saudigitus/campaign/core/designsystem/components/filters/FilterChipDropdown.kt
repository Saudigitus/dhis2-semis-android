package org.saudigitus.campaign.core.designsystem.components.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.data.models.FilterModel
import org.saudigitus.campaign.core.data.models.OptionModel

@Composable
fun FilterChipDropdown(
    modifier: Modifier = Modifier,
    filter: FilterModel,
    onClick: (FilterModel, OptionModel) -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var selectedText by rememberSaveable { mutableStateOf("") }

    var chipSize by remember { mutableStateOf(Size.Zero) }

    if (filter.optionModel.indexOfFirst { it == filter.selectedOption } != -1) {
        selectedIndex = filter.optionModel.indexOfFirst { it == filter.selectedOption }
        selectedText = filter.selectedOption?.displayName.orEmpty()
    } else {
        selectedIndex = -1
        selectedText = ""
    }

    val paddingValue = if (selectedIndex >= 0) {
        4.dp
    } else {
        0.dp
    }

    Column(modifier = modifier) {
        FilterChip(
            onClick = { isExpanded = !isExpanded },
            label = { Text(selectedText.ifEmpty { filter.displayName }) },
            selected = filter.value.toBoolean(),
            trailingIcon = {
                if (filter.optionModel.isNotEmpty()) {
                    Icon(
                        if (isExpanded) {
                            Icons.Default.ArrowDropUp
                        } else {
                            Icons.Default.ArrowDropDown
                        },
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier
                .width(180.dp)
                .onGloballyPositioned { coordinates ->
                    chipSize = coordinates.size.toSize()
                },
        )

        AnimatedVisibility(filter.optionModel.isNotEmpty()) {
            DropdownMenu(
                expanded = isExpanded && filter.optionModel.isNotEmpty(),
                onDismissRequest = { isExpanded = false },
                modifier =
                    Modifier
                        .width(with(LocalDensity.current) { chipSize.width.toDp() })
                        .background(SurfaceColor.SurfaceBright),
                offset = DpOffset(x = 0.dp, y = Spacing.Spacing4),
                shape = RoundedCornerShape(16.dp),
                properties =
                    PopupProperties(
                        focusable = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        clippingEnabled = true,
                    ),
            ) {
                filter.optionModel.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(text = item.displayName.orEmpty()) },
                        onClick = {
                            selectedIndex = index
                            isExpanded = false
                            selectedText = item.displayName.orEmpty()

                            onClick.invoke(filter, item)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = if (selectedIndex == index) {
                                    Color.LightGray.copy(.35f)
                                } else {
                                    Color.White
                                },
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(
                                start = 8.dp,
                                top = paddingValue,
                                end = 8.dp,
                                bottom = paddingValue,
                            ),
                    )
                }
            }
        }
    }
}