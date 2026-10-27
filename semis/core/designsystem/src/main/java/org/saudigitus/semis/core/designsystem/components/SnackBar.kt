package org.saudigitus.semis.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.theme.light_success

@Composable
fun SnackBar(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    containerColor: Color = light_success,
    contentColor: Color = Color.White,
    painter: Painter = painterResource(R.drawable.success_icon),
) {
    SnackbarHost(hostState = hostState) {
        Snackbar(
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painter,
                    contentDescription = it.visuals.message,
                )

                Text(
                    text = it.visuals.message,
                    style = LocalTextStyle.current.copy(
                        fontFamily = FontFamily(Font(R.font.rubik_regular)),
                    ),
                )
            }
        }
    }
}