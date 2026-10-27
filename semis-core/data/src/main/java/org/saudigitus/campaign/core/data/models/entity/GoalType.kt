package org.saudigitus.campaign.core.data.models.entity

enum class GoalType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    companion object {
        fun from(value: String?): GoalType =
            entries.firstOrNull { it.name == value?.uppercase() }
                ?: DAILY
    }
}