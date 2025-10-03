package org.saudigitus.semis.core.designsystem.components.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import org.dhis2.ui.theme.colorPrimary
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.model.DropdownItem
import org.saudigitus.semis.core.designsystem.components.model.FilterType
import org.saudigitus.semis.core.designsystem.utils.icon
import org.saudigitus.semis.core.designsystem.utils.placeholder
import kotlin.collections.find
import kotlin.collections.forEachIndexed

@Stable
data class DropdownState(
    val filterType: FilterType = FilterType.UNKNOWN,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
    val displayName: String = "",
    val order: Int = -1,
    val data: List<DropdownItem> = emptyList(),
)


@Composable
fun DropDown(
    modifier: Modifier = Modifier,
    dropdownState: DropdownState,
    defaultSelection: DropdownItem? = null,
    elevation: Dp = 2.dp,
    onItemClick: (DropdownItem) -> Unit,
) {
    var selectedItemIndex by remember { mutableIntStateOf(-1) }
    var selectedItem by remember {
        mutableStateOf(defaultSelection?.itemName ?: defaultSelection?.code.orEmpty())
    }
    var expand by remember { mutableStateOf(false) }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    var onClearSelection by remember { mutableStateOf(false) }

    if (selectedItemIndex > 0 && onClearSelection) {
        selectedItemIndex = 0
        selectedItem = "${dropdownState.data[selectedItemIndex]}"
    }

    if (defaultSelection != null) {
        val item = dropdownState.data.find {
            it.itemName == defaultSelection.itemName || "${it.code}" == defaultSelection.code
        }
        selectedItemIndex = dropdownState.data.indexOf(item)
        selectedItem = item.toString().ifEmpty { "" }

        if (item != null) {
            onItemClick.invoke(item)
        }
    }

    val paddingValue = if (selectedItemIndex >= 0) {
        4.dp
    } else {
        0.dp
    }

    val interactionSource = remember { MutableInteractionSource() }
    if (interactionSource.collectIsPressedAsState().value) {
        expand = !expand
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .shadow(
                    elevation = elevation,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(30.dp),
                    clip = false,
                )
                .background(color = Color.White, shape = RoundedCornerShape(30.dp)),
            shape = RoundedCornerShape(30.dp),
            value = selectedItem,
            onValueChange = {
                selectedItem = it
            },
            singleLine = true,
            readOnly = true,
            placeholder = { Text(text = dropdownState.displayName.ifEmpty { stringResource(dropdownState.placeholder()) }) },
            leadingIcon = {
                Icon(
                    imageVector = dropdownState.leadingIcon ?: ImageVector.vectorResource(dropdownState.icon()),
                    contentDescription = null,
                    tint = colorPrimary,
                )
            },
            trailingIcon = {
                IconButton(onClick = { expand = !expand }) {
                    if (dropdownState.trailingIcon != null) {
                        Icon(
                            imageVector = dropdownState.trailingIcon,
                            contentDescription = null,
                            tint = colorPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = if (!expand) {
                                Icons.Default.ArrowDropDown
                            } else {
                                Icons.Default.ArrowDropUp
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
            ),
        )

        DropdownMenu(
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                .background(color = Color.White),
            offset = DpOffset(0.dp, 2.dp),
            expanded = expand,
            onDismissRequest = {
                expand = !expand
            },
        ) {
            dropdownState.data.forEachIndexed { index, item ->
                Row(Modifier.padding(horizontal = 10.dp)) {
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (selectedItemIndex == index) {
                                    Color.LightGray.copy(.5f)
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(paddingValue),
                        text = {
                            Text(
                                text = "$item",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = true,
                                style = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily(Font(R.font.rubik_regular)),
                                ),
                            )
                        },
                        onClick = {
                            onItemClick(item)
                            expand = !expand
                            selectedItem = "$item"
                            selectedItemIndex = index
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = dropdownState.leadingIcon ?: ImageVector.vectorResource(dropdownState.icon()),
                                contentDescription = "$item",
                                tint = colorPrimary,
                            )
                        },
                    )
                }
            }
        }
    }
}