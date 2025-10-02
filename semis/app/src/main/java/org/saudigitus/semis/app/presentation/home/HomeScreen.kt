package org.saudigitus.semis.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold

@Composable
fun HomeScreen() {
    TopAppBarScaffold(
        toolbarHeaders = ToolbarHeaders(
            title = "Home",
            subtitle = "Academic year | School"
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Hello Buddy!\nThis is SEMIS App")
        }
    }
}