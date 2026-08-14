package org.saudigitus.semis.transfer.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.semis.transfer.model.TransferStep

@Composable
internal fun TransferStepContainer(step: TransferStep, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TransferProgress(step)
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}
