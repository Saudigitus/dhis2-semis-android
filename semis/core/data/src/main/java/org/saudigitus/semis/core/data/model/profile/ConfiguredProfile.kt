package org.saudigitus.semis.core.data.model.profile

import androidx.compose.runtime.Immutable

/**
 * The record of one person, in the shape the deployment configured it.
 *
 * Nothing here names a learner, a term or a subject: the page is built from what the datastore
 * states, so the same structure carries a student, a staff member and whatever a future program
 * registers.
 */
@Immutable
data class ConfiguredProfile(
    val identity: ProfileIdentity = ProfileIdentity(),
    val tabs: List<ProfileTabContent> = emptyList(),
)

/** Who the page is about, as the identity card configuration describes them. */
@Immutable
data class ProfileIdentity(
    val title: String = "",
    val subtitle: String = "",
    val photo: String? = null,
    val badges: List<String> = emptyList(),
)

/** One tab, with the panels it holds already read. */
@Immutable
data class ProfileTabContent(
    val id: String,
    val title: String,
    val panels: List<ProfilePanel> = emptyList(),
)

/**
 * One panel of a tab, and the records it holds.
 *
 * [editable] carries what the deployment asked for so that a later version can offer the form for
 * writing on exactly the panels that allow it. [target] is what such a form would be pointed at:
 * the program stage for a panel of events, and null for the panel of attributes.
 */
@Immutable
data class ProfilePanel(
    val title: String,
    val kind: ProfilePanelKind,
    val editable: Boolean,
    val target: String?,
    val records: List<ProfileRecord> = emptyList(),
)

/** How a panel is drawn: one block per record, or a compact row per record. */
enum class ProfilePanelKind {
    CARDS,
    TABLE,
}

/**
 * One record inside a panel: an event, or the attributes of the person.
 *
 * The values are not a flat list. A program stage groups its data elements into sections, and a
 * program groups its attributes the same way, so the record is read in those groups and shown in
 * them: that is the grouping whoever configured the program chose, and reading it back in any
 * other shape would be reading back something they did not describe.
 */
@Immutable
data class ProfileRecord(
    val id: String,
    val heading: String? = null,
    val sections: List<ProfileSection> = emptyList(),
)

/**
 * One section of a record.
 *
 * [title] is null for the values the configuration leaves outside every section, and for a stage
 * or program that groups nothing at all, where a heading would name a grouping that does not
 * exist.
 */
@Immutable
data class ProfileSection(
    val title: String?,
    val values: List<ProfileValue> = emptyList(),
)

/** One value of a record, already resolved to what the reader should see. */
@Immutable
data class ProfileValue(
    val label: String,
    val value: String,
)
