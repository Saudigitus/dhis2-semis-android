package org.saudigitus.semis.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R

private val rubikFontFamily = FontFamily(
    Font(resId = R.font.rubik_regular),
    Font(resId = R.font.rubik_light),
    Font(resId = R.font.rubik_medium),
    Font(resId = R.font.rubik_bold),
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = rubikFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
)
