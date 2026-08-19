package org.saudigitus.semis.core.data.model.profile

/**
 * A labelled value of a learner profile, be it a tracked entity attribute or a data value.
 */
data class ProfileAttribute(
    val label: String,
    val value: String?,
)
