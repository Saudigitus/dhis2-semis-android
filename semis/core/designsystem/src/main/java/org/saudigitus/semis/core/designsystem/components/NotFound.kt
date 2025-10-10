package org.saudigitus.semis.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.theme.dark_warning

@Composable
fun ConfigNotFoud(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(.25f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.settings_alert),
                contentDescription = stringResource(R.string.config_not_found),
                modifier = Modifier.size(74.dp),
                tint = dark_warning
            )
            Text(
                text = stringResource(R.string.config_not_found),
                color = dark_warning,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NoRecordsFound(
    modifier: Modifier = Modifier,
    imageVector: ImageVector = Icons.Default.HideSource,
    message: String = stringResource(R.string.no_records_found)
) {
    Row(
        modifier = modifier.then(
            Modifier.background(
                color = Color.LightGray.copy(alpha = .25f),
                shape = RoundedCornerShape(16.dp)
            ).padding(16.dp)
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(R.string.no_records_found),
            tint = dark_warning
        )
        Text(
            text = message,
            color = dark_warning
        )
    }
}