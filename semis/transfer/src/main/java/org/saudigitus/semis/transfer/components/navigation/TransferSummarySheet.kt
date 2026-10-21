package org.saudigitus.semis.transfer.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.transfer.model.TransferTab

/**
 * Which of the two lists is being read, on the sheet below the header. The school itself
 * is named by the bar, so it is not repeated here.
 */
@Composable
internal fun TransferSummarySheet(
    selectedTab: TransferTab,
    modifier: Modifier = Modifier,
    onSelectTab: (TransferTab) -> Unit,
) {
    HeaderSheet(modifier = modifier) {
        TransferTabs(
            selectedTab = selectedTab,
            onSelect = onSelectTab,
        )
    }
}

/** While a request is being raised, the sheet carries the step instead of the lists. */
@Composable
internal fun TransferRequestSheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    HeaderSheet(modifier = modifier, content = content)
}

/**
 * The header colour is painted behind the sheet so that the rounded top corners have
 * something to curve over. Without it the corners fall on the screen background and the
 * seam between the two reads as a straight cut. It has to be the colour the bar itself
 * uses, or the two blues meet and the join is visible as a band.
 */
@Composable
private fun HeaderSheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SemisPalette.HeaderBlueAccent),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = SummarySheetShape,
            color = SemisPalette.CardSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

/** Curves over the header, which is flat where the two meet. */
private val SummarySheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
