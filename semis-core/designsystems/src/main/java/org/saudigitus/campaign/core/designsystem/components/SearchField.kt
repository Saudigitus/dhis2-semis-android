package org.saudigitus.campaign.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.designsystem.utils.Utils
import org.saudigitus.campaign.core.utils.R

@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    label: String? = stringResource(R.string.search),
    value: String,
    filterAction: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.dp, color = Color.Unspecified, shape = RoundedCornerShape(30.dp))
            .then(modifier),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label.orEmpty()) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = label) },
        trailingIcon = filterAction,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(30.dp),
        singleLine = true,
        colors = Utils.inputColors()
    )
}