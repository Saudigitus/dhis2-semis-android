package org.saudigitus.semis.core.designsystem.components.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

/**
 * Colored screen header with rounded bottom corners, hosting the title bar and any
 * contextual content such as summary tiles.
 */
@Composable
fun RoundedBottomHeader(
    modifier: Modifier = Modifier,
    containerColor: Color = SemisPalette.HeaderBlue,
    cornerRadius: Dp = 28.dp,
    contentPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        top = 8.dp,
        end = 16.dp,
        bottom = 18.dp,
    ),
    verticalSpacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = containerColor,
                shape = RoundedCornerShape(
                    bottomStart = cornerRadius,
                    bottomEnd = cornerRadius,
                ),
            )
            .statusBarsPadding()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content,
    )
}
