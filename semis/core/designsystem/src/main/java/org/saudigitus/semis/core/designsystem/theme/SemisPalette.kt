package org.saudigitus.semis.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette tokens shared by the module screens that follow the mobile mock-up design.
 */
object SemisPalette {

    val HeaderBlue = Color(0xFF1B3F94)
    val HeaderBlueAccent = Color(0xFF2B55AD)
    val ActionBlue = Color(0xFF1B4BA8)

    val ScreenBackground = Color(0xFFEEF2F7)
    val CardSurface = Color(0xFFFFFFFF)
    val CardBorder = Color(0xFFE2E8F0)

    val TextPrimary = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)
    val TextMuted = Color(0xFF94A3B8)

    val OnHeaderPrimary = Color(0xFFFFFFFF)
    val OnHeaderSecondary = Color(0xFFA9C1E8)

    /** Translucent surface shared by the actions and cards laid over [HeaderBlue]. */
    val HeaderSurface = Color(0xFFFFFFFF).copy(alpha = .16f)

    /** Deep tones used by the summary tiles rendered on top of [HeaderBlue]. */
    val TileTeal = Color(0xFF115E59)
    val TilePurple = Color(0xFF5B3A94)
    val TileSlate = Color(0xFF475569)

    /** Ordered tones assigned to the configured statuses of a summary tile row. */
    val TileTones = listOf(TileTeal, TilePurple, TileSlate, HeaderBlueAccent)
}
