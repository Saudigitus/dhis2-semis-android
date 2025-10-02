package org.saudigitus.semis.core.designsystem.components.model

import kotlinx.serialization.Serializable

@Serializable
data class DropdownItem(
    val id: String,
    val itemName: String,
    val code: String? = null,
    val sortOrder: Int? = -1,
) {
    override fun toString() = itemName
}