package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * How a deployment wants the record of one person presented.
 *
 * The page is not a fixed set of sections written in Kotlin: a deployment states which tabs
 * exist, what each one holds and in what order, and the same page then serves a learner, a staff
 * member and whatever a future program registers. Everything is nullable because a deployment
 * that has not configured the page must degrade to showing nothing rather than failing to parse.
 */
@Serializable
data class Profile(
    @SerialName("identityCard")
    val identityCard: IdentityCard?,
    @SerialName("program")
    val program: String?,
    @SerialName("tabs")
    val tabs: List<ProfileTab?>?
)

/** What the header shows: who this is, and the marks that qualify them at a glance. */
@Serializable
data class IdentityCard(
    @SerialName("badges")
    val badges: List<IdentityBadge?>?,
    @SerialName("photo")
    val photo: IdentityPhoto?,
    @SerialName("subtitle")
    val subtitle: IdentityText?,
    @SerialName("title")
    val title: IdentityText?
)

/**
 * A mark shown beside the name.
 *
 * [source] says where the value is read from and is deliberately kept as text: deployments are
 * already using sources the published schema does not list, and a badge nobody can resolve is
 * dropped rather than allowed to fail the whole page.
 */
@Serializable
data class IdentityBadge(
    @SerialName("order")
    val order: Int?,
    @SerialName("source")
    val source: String?,
    @SerialName("styled")
    val styled: Boolean?,
    @SerialName("variable")
    val variable: String?
)

/** The attribute holding the person's photo. */
@Serializable
data class IdentityPhoto(
    @SerialName("attribute")
    val attribute: String?
)

/** Text built from attribute values joined by [separator], such as a full name. */
@Serializable
data class IdentityText(
    @SerialName("attributes")
    val attributes: List<String?>?,
    @SerialName("separator")
    val separator: String?
)

/** One tab of the page, holding the panels a deployment grouped together. */
@Serializable
data class ProfileTab(
    @SerialName("color")
    val color: String?,
    @SerialName("components")
    val components: List<ProfileComponent?>?,
    @SerialName("createdAt")
    val createdAt: Long?,
    @SerialName("displayName")
    val displayName: String?,
    @SerialName("id")
    val id: String?,
    @SerialName("order")
    val order: Int?
)

/**
 * One panel of a tab.
 *
 * [type] names what the panel shows and, like a badge source, is kept as text so that a type this
 * version does not draw is skipped instead of breaking the tab around it. [editable] is what
 * decides, later, whether the panel offers the form for writing rather than only reading.
 */
@Serializable
data class ProfileComponent(
    @SerialName("details")
    val details: ProfileComponentDetails?,
    @SerialName("displayName")
    val displayName: String?,
    @SerialName("editable")
    val editable: Boolean?,
    @SerialName("order")
    val order: Int?,
    @SerialName("size")
    val size: String?,
    @SerialName("type")
    val type: String?
)

/** Which records a panel reads, and how many of them it shows. */
@Serializable
data class ProfileComponentDetails(
    @SerialName("pageSize")
    val pageSize: Int?,
    @SerialName("programStage")
    val programStage: String?
)
