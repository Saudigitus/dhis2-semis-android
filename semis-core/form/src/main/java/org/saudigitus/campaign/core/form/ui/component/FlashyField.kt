package org.saudigitus.campaign.core.form.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MandatoryFieldWrapper(
    showError: Boolean,
    content: @Composable () -> Unit
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(showError) {
        if (showError) {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 10f,
                animationSpec = tween(50)
            )
            shakeOffset.animateTo(-10f, tween(50))
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    Box(
        modifier = Modifier
            .offset(x = shakeOffset.value.dp)
            .background(
                if (showError) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (showError) MaterialTheme.colorScheme.error else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(if (showError) 8.dp else 0.dp)
    ) {
        content()
    }
}
