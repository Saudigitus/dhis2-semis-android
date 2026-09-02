package org.saudigitus.semis.core.data.model.profile

/**
 * Marks a learner collected for one subject, with the average they add up to.
 */
data class SubjectPerformance(
    val programStage: String,
    val subject: String,
    val marks: List<ProfileMark> = emptyList(),
) {
    val average: Double? = marks
        .mapNotNull { it.value }
        .takeIf { it.isNotEmpty() }
        ?.average()
}
