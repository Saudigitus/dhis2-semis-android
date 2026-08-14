package org.saudigitus.campaign.core.data.models.entity.convertes

import androidx.room.TypeConverter
import org.saudigitus.campaign.core.data.models.entity.GoalScope
import org.saudigitus.campaign.core.data.models.entity.GoalType

class GoalConverters {

    @TypeConverter
    fun fromType(type: GoalType): String = type.name

    @TypeConverter
    fun toType(value: String): GoalType = GoalType.valueOf(value)

    @TypeConverter
    fun fromScope(scope: GoalScope): String = scope.name

    @TypeConverter
    fun toScope(value: String): GoalScope = GoalScope.valueOf(value)
}