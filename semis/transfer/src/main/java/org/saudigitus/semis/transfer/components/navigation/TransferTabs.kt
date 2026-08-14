package org.saudigitus.semis.transfer.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.model.TransferTab

@Composable
internal fun TransferTabs(
    selectedTab: TransferTab,
    incomingCount: Int,
    onSelect: (TransferTab) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            TransferTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val interactionSource = remember { MutableInteractionSource() }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(
                                bounded = false,
                                color = SurfaceColor.Primary,
                                radius = 14.dp
                            ),
                            enabled = !selected,
                            role = Role.Tab,
                            onClick = { onSelect(tab) },
                        ),
                    color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (selected) 2.dp else 0.dp,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                        text = if (tab == TransferTab.TRANSFERS) {
                            stringResource(R.string.transfers_tab)
                        } else {
                            stringResource(R.string.incoming_students_tab_count, incomingCount)
                        },
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
