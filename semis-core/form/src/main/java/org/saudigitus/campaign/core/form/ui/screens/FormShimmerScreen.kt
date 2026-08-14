package org.saudigitus.campaign.core.form.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        toolbarHeaders = ToolbarHeaders(title = stringResource(R.string.form_title)),
    ) {
        Spacer(Modifier.size(16.dp))
        repeat(15) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect()
            )
        }
    }
}

