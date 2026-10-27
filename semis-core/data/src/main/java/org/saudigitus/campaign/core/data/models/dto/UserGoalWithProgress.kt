package org.saudigitus.campaign.core.data.models.dto

import org.saudigitus.campaign.core.data.models.entity.GoalScope
import org.saudigitus.campaign.core.data.models.entity.GoalType

data class UserGoalWithProgress(
    val name: String,
    val type: GoalType,
    val uid: String,
    val goal: Int,
    val scope: GoalScope,
    val achieved: Int = 0
)