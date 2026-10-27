package org.saudigitus.semis.transfer.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.model.TransferStep

/**
 * One action at a time: starting a request from the outgoing list, then moving through
 * the steps until it is confirmed.
 */
@Composable
internal fun TransferBottomBar(
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    isBusy: Boolean = false,
    leadingIcon: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .height(54.dp),
            enabled = enabled,
            shape = RoundedCornerShape(17.dp),
            onClick = onClick,
        ) {
            when {
                isBusy -> CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )

                else -> {
                    if (leadingIcon) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 2.dp),
                        )
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/** Wording of the action that moves a request forward. */
internal fun TransferStep.continueLabel(): Int = when (this) {
    TransferStep.ENTITIES, TransferStep.DESTINATION -> R.string.continue_label
    TransferStep.REVIEW -> R.string.confirm_transfer
}
