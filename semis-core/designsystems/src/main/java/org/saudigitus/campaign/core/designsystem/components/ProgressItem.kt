package org.saudigitus.campaign.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import org.saudigitus.campaign.core.data.models.dto.UserGoalWithProgress
import org.saudigitus.campaign.core.designsystem.theme.light_error
import org.saudigitus.campaign.core.designsystem.theme.light_success
import org.saudigitus.campaign.core.designsystem.theme.light_warning
import org.saudigitus.campaign.core.designsystem.utils.Utils.RED_THRESHOLD
import org.saudigitus.campaign.core.designsystem.utils.Utils.YELLOW_THRESHOLD
import org.saudigitus.campaign.core.designsystem.utils.Utils.getColorByProgress

@Composable
fun ProgressItem(userProgress: UserGoalWithProgress) {

    val progress =
        (
            if (userProgress.achieved == 0) 0f
            else userProgress.achieved.toFloat() / userProgress.goal.toFloat()
            )
            .fastCoerceIn(0f, 1f)


    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = userProgress.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "${userProgress.achieved}/${userProgress.goal}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .height(6.dp)
                .fillMaxWidth()
        ) {
            Row(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .weight(RED_THRESHOLD)
                        .fillMaxHeight()
                        .background(
                            color = light_error.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                bottomStart = 16.dp
                            )
                        )
                )
                Box(
                    Modifier
                        .weight(YELLOW_THRESHOLD - RED_THRESHOLD)
                        .fillMaxHeight()
                        .background(color = light_warning.copy(alpha = 0.3f))
                )
                Box(
                    Modifier
                        .weight(1f - YELLOW_THRESHOLD)
                        .fillMaxHeight()
                        .background(
                            color = light_success.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(
                                topEnd = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                )
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                trackColor = Color.Transparent,
                color = getColorByProgress(progress),
                progress = { animatedProgress }
            )
        }
    }
}
