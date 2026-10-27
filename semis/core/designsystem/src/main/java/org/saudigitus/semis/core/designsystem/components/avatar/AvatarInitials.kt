package org.saudigitus.semis.core.designsystem.components.avatar

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Derives the initials and the container color rendered by [InitialsAvatar].
 */
object AvatarInitials {

    private val palette = listOf(
        Color(0xFF1E3A8A),
        Color(0xFF0E7490),
        Color(0xFF7C3AED),
        Color(0xFFD97706),
        Color(0xFF0891B2),
        Color(0xFFBE185D),
        Color(0xFF15803D),
        Color(0xFF9A3412),
    )

    /**
     * First letter of the first and last meaningful word of [name], upper-cased.
     */
    fun of(name: String, maxLength: Int = 2): String {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

        return when {
            words.isEmpty() -> ""
            words.size == 1 -> words.first().take(maxLength)
            else -> "${words.first().first()}${words.last().first()}"
        }.uppercase()
    }

    /**
     * Stable color for [key], so the same record always keeps the same avatar color.
     */
    fun colorFor(key: String): Color {
        if (key.isEmpty()) return palette.first()

        return palette[abs(key.hashCode()) % palette.size]
    }
}
