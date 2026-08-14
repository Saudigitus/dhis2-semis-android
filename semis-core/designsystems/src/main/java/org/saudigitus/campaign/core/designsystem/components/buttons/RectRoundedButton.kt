package org.saudigitus.campaign.core.designsystem.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.designsystem.R

@Composable
fun RectRoundedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: String = stringResource(id = R.string.new_enrollment),
    icon: (@Composable () -> Unit)? = null
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .padding(16.dp)
            .then(modifier),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                8.dp,
                Alignment.CenterHorizontally
            )
        ) {
            icon?.invoke()
            Text(label)
        }
    }
}