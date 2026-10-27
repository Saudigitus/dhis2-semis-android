package org.saudigitus.campaign.core.data.models

data class OrgUnit(
    val uid: String,
    val displayName: String?,
) {
    override fun toString(): String {
        return "$uid - $displayName"
    }
}
