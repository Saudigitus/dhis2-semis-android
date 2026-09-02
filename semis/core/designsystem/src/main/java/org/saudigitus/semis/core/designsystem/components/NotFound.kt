package org.saudigitus.semis.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.theme.dark_warning
import org.saudigitus.semis.core.designsystem.theme.warning_notice_content
import org.saudigitus.semis.core.designsystem.theme.warning_notice_outline
import org.saudigitus.semis.core.designsystem.theme.warning_notice_surface

@Composable
fun ConfigNotFound(
    modifier: Modifier = Modifier,
    iconSize: Dp = 74.dp,
    imageVector: ImageVector = ImageVector.vectorResource(R.drawable.settings_alert),
    message: String = stringResource(R.string.config_not_found)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(.25f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = message,
                modifier = Modifier.size(iconSize),
                tint = dark_warning
            )
            Text(
                text = message,
                color = dark_warning,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Banner telling the user what is missing before the screen can be of any use.
 *
 * It warns rather than blocks, so it is drawn as a notice and not as an error: a warm surface, a
 * quiet outline and text small enough to read as a remark beside the content rather than as an
 * interruption of it. The icon sits after the text because the message is what has to be read
 * first, and the icon only says how to read it.
 */
@Composable
fun NoRecordsFound(
    modifier: Modifier = Modifier,
    imageVector: ImageVector = Icons.Rounded.WarningAmber,
    message: String = stringResource(R.string.no_records_found)
) {
    Row(
        modifier = modifier
            .background(color = warning_notice_surface, shape = NoticeShape)
            .border(width = 1.dp, color = warning_notice_outline, shape = NoticeShape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = message,
            color = warning_notice_content,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
        )
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = warning_notice_content
        )
    }
}

private val NoticeShape = RoundedCornerShape(12.dp)


@Composable
fun NoResults(
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_empty_folder),
            contentDescription = message,
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = message,
            fontSize = 17.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}