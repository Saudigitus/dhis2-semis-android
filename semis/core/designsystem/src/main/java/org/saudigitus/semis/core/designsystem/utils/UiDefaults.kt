package org.saudigitus.semis.core.designsystem.utils

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.dhis2.commons.resources.ColorUtils
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.model.FilterType
import org.saudigitus.semis.core.utils.Constants

object UiDefaults {

    const val GRAY = 0xFF888888

    val filterTypeMap = mapOf(
        Constants.GRADE to FilterType.GRADE,
        Constants.SECTION to FilterType.SECTION,
        Constants.SECTION_CLASS to FilterType.SECTION,
        Constants.CLASS to FilterType.SECTION
    )

    fun getIconByName(name: String) = when (name) {
        "correct_blue_fill" -> R.drawable.present
        "wrong_red_fill" -> R.drawable.absent
        "clock_orange_fill" -> R.drawable.late
        else -> R.drawable.ic_empty
    }

    fun dynamicIcons(name: String) = try {
        val cl = Class.forName("androidx.compose.material.icons.filled.${name}Kt")
        val method = cl.declaredMethods.first()
        method.invoke(null, Icons.Filled) as ImageVector
    } catch (_: Throwable) {
        null
    }

    fun getAttendanceStatusColor(key: String, statusColor: String): Color {
        return try {
            val colorUtils = ColorUtils()
            Color(colorUtils.parseColor(statusColor))
        } catch (_: Exception) {
            getAttendanceStatusColor(key)
        }
    }

    private fun getAttendanceStatusColor(key: String): Color {
        return when (key) {
            "present" -> Color(0xFF81C784)
            "absent" -> Color(0xFFE57373)
            "late" -> Color(0xFFFACC95)
            else -> Color.LightGray
        }
    }

}