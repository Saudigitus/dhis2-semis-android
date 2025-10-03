package org.saudigitus.semis.core.designsystem.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.dhis2.ui.theme.colorPrimary

@Composable
fun Backdrop(
    modifier: Modifier,
    backLayerContainerColor: Color = colorPrimary,
    frontLayerContainerColor: Color = Color.White,
    frontLayerShape: Shape = MaterialTheme.shapes.medium
        .copy(
            topStart = CornerSize(16.dp),
            topEnd = CornerSize(16.dp),
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        ),
    backLayer: @Composable ColumnScope.() -> Unit,
    frontLayer: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.then(
            Modifier.background(color = backLayerContainerColor)
        ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        backLayer()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = frontLayerContainerColor,
                    shape = frontLayerShape,
                ),
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
        ) {
            frontLayer()
        }
    }
}