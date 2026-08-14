package org.saudigitus.campaign.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.saudigitus.campaign.core.designsystem.R
import org.saudigitus.campaign.core.designsystem.theme.light_error

@Composable
fun DefaultErrorBox(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.config_not_found),
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .then(modifier),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InfoBar(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            text = text,
            textColor = light_error,
            backgroundColor = light_error.copy(alpha = 0.3f),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = text,
                    tint = light_error
                )
            }
        )
        Spacer(modifier = Modifier.height(108.dp))
        Image(
            painter = painterResource(R.drawable.error),
            contentDescription = text,
        )
    }
}