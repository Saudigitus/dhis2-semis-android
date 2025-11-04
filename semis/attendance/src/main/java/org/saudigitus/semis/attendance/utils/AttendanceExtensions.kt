package org.saudigitus.semis.attendance.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonDecorator
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import java.security.acl.Owner

fun AttendanceEventWithDecorator.withIconProps(
    icon: ImageVector,
    iconName: String,
    iconColor: Color,
) = copy(
    icon = icon,
    iconName = iconName,
    iconColor = iconColor
)

fun AttendanceEventWithDecorator.withDecoratorAndIconProps(
    decorator: AttendanceButtonDecorator,
    icon: ImageVector,
    iconName: String,
    iconColor: Color,
) = copy(
    decorator = decorator,
    icon = icon,
    iconName = iconName,
    iconColor = iconColor
)

fun AttendanceEventWithDecorator.withDecorator(
    decorator: AttendanceButtonDecorator,
) = copy(
    decorator = decorator,
)

fun AttendanceButtonModel.withProps(
    owner: String? = null,
    icon: ImageVector?,
    iconName: String?,
    iconColor: Color?,
) = AttendanceButtonModel(
    key = this.key,
    owner = owner,
    code = this.code,
    name = this.name,
    dataElement = this.dataElement,
    order = this.order,
    enabled = this.enabled,
    icon = icon,
    iconName = iconName,
    color = iconColor
)