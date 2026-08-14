package org.saudigitus.campaign.core.form.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.campaign.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.campaign.core.designsystem.utils.shimmerEffect
import org.saudigitus.campaign.core.form.R


@Composable
fun FormShimmerScreen() {
    TopAppBarScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        toolbarHeaders = ToolbarHeaders(title = stringResource(R.string.form_title)),
    ) {
        Spacer(Modifier.size(16.dp))
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(32.dp),
            strokeWidth = 3.dp,
        )
        Spacer(Modifier.size(16.dp))
        repeat(10) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect()
            )
        }
    }
}

