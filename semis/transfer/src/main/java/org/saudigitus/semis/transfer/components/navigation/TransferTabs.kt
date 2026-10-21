package org.saudigitus.semis.transfer.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.model.TransferTab

/**
 * The two sides of a transfer, as one segmented control: what this school asked for and
 * what it was asked to decide.
 */
@Composable
internal fun TransferTabs(
    selectedTab: TransferTab,
    modifier: Modifier = Modifier,
    onSelect: (TransferTab) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
                                radius = 14.dp,
                            ),
                            enabled = !selected,
                            role = Role.Tab,
                            onClick = { onSelect(tab) },
                        ),
                    color = if (selected) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        Color.Transparent
                    },
                    shadowElevation = if (selected) 2.dp else 0.dp,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            6.dp,
                            Alignment.CenterHorizontally,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val contentColor = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = contentColor,
                        )

                        Text(
                            text = stringResource(tab.label),
                            maxLines = 1,
                            color = contentColor,
                            fontWeight = if (selected) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

private val TransferTab.icon: ImageVector
    get() = when (this) {
        TransferTab.OUTGOING -> Icons.Outlined.Upload
        TransferTab.INCOMING -> Icons.Outlined.Download
    }

private val TransferTab.label: Int
    get() = when (this) {
        TransferTab.OUTGOING -> R.string.outgoing_tab
        TransferTab.INCOMING -> R.string.incoming_tab
    }
