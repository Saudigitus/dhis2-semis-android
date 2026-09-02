package org.saudigitus.semis.core.data.model

data class OrgUnit(
    val uid: String,
    val displayName: String?,
) {
    override fun toString(): String {
        return "$uid - $displayName"
    }
}
