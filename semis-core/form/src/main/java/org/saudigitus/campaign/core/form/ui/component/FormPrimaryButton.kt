package org.saudigitus.campaign.core.form.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.campaign.core.designsystem.theme.FormSurfaces
import org.saudigitus.campaign.core.form.R

/**
 * Main action of the form, drawn as the module screens draw theirs.
 *
 * The form keeps its own copy of the shape, colour and type rather than reaching for the app design
 * system, because it is deliberately built to stay portable. The values match the primary action of
 * the listings so that moving from a list into a form does not feel like moving between two apps.
 */
@Composable
internal fun FormPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FormSurfaces.ActionBlue,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            imageVector?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }

            Text(
                text = text,
                fontSize = 15.sp,
                maxLines = 1,
                fontFamily = FontFamily(Font(R.font.rubik_medium)),
            )
        }
    }
}
