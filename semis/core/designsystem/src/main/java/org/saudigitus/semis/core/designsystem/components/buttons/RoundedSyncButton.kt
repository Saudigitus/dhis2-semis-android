package org.saudigitus.semis.core.designsystem.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun RoundedSyncButton(
    modifier: Modifier = Modifier,
    label: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.then(Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(54.dp)
            .shadow(
                elevation = 2.dp,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                shape = RoundedCornerShape(30.dp),
                clip = false,
            )),
        onClick = onClick,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                16.dp,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = label,
                )
            }
            Text(label)
            trailingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = label,
                )
            }
        }
    }
}