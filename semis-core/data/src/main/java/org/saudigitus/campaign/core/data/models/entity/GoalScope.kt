package org.saudigitus.campaign.core.data.models.entity

enum class GoalScope {
    ALL,
    GROUPS;

    companion object {
        fun from(value: String?): GoalScope =
            entries.firstOrNull { it.name == value?.uppercase() }
                ?: ALL
    }
}