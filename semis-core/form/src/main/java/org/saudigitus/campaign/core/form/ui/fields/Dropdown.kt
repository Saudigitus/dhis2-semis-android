package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.designsystem.theme.seed
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.utils.Utils

@Composable
fun <T> DropdownField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean,
    data: List<T>,
    selectedItem: T? = null,
    enabled: Boolean = true,
    colors: TextFieldColors = Utils.inputColors(),
    extraTrailingIcon: @Composable (() -> Unit)? = null,
    onClick: (T) -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var selectedText by rememberSaveable { mutableStateOf("") }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val interactionSource = remember { MutableInteractionSource() }

    if (interactionSource.collectIsPressedAsState().value) {
        isExpanded = !isExpanded
    }

    if (data.indexOfFirst { it == selectedItem } != -1) {
        selectedIndex = data.indexOfFirst { it == selectedItem }
        selectedText = "$selectedItem"
    }

    val paddingValue = if (selectedIndex >= 0) {
        4.dp
    } else {
        0.dp
    }

    Column {
        TextField(
            value = selectedText,
            onValueChange = {},
            label = { Text(text = label) },
            placeholder = { Text(text = placeholder) },
            readOnly = true,
            singleLine = true,
            enabled = enabled,
            trailingIcon = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = enabled,
                        onClick = {
                            isExpanded = !isExpanded
                        },
                    ) {
                        Icon(
                            if (isExpanded) {
                                Icons.Default.ArrowDropUp
                            } else {
                                Icons.Default.ArrowDropDown
                            },
                            contentDescription = null,
                            tint = seed,
                        )
                    }

                    if (extraTrailingIcon != null) {
                        Text(
                            stringResource(R.string.separator),
                            color = SurfaceColor.Primary,
                        )

                        extraTrailingIcon()
                    }
                }
            },
            interactionSource = interactionSource,
            colors = colors,
            supportingText = supportingText,
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(Spacing.Spacing0, 300.dp)
                .then(modifier)
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                },
        )

        DropdownMenu(
            expanded = isExpanded && data.isNotEmpty(),
            onDismissRequest = { isExpanded = false },
            modifier =
                Modifier
                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
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
            data.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(text = "$item") },
                    onClick = {
                        selectedIndex = index
                        isExpanded = false
                        selectedText = "$item"

                        onClick.invoke(item)
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
