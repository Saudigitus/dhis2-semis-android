package org.saudigitus.semis.core.data.model.profile

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.data.model.person.PersonIdentity

/**
 * The record of one person for the selected academic year, built from the program metadata and
 * the modules the deployment enables, with no profile configuration.
 *
 * The same rules build it on the web, so a learner or a staff member reads the same on both apps.
 * Nothing here names a learner, a term or a subject: a student, a staff member and whatever a
 * future program registers all come out of the same structure.
 */
@Immutable
data class PersonProfile(
    val identity: PersonIdentity,
    val academicYear: String? = null,
    val summary: List<ProfileFigure> = emptyList(),
    val tabs: List<ProfileTabContent> = emptyList(),
)

/** One figure of the summary at the top of the page, counted from the records of the year. */
@Immutable
data class ProfileFigure(
    val kind: ProfileFigureKind,
    val value: String,
    /** What the figure is about when the kind alone does not say it: the stage of an average. */
    val subject: String? = null,
)

enum class ProfileFigureKind {
    ABSENCES,
    AVERAGE,
}

/**
 * The tabs of the page, always in this order and the same on the web.
 *
 * Personal details is always there; every other tab appears only when its module is enabled for
 * the program.
 */
enum class ProfileTabKind {
    PERSONAL_DETAILS,
    ATTENDANCE,
    PERFORMANCE,
    FINAL_RESULT,
    TRANSFER,
}

/** One tab, with the panels it holds already read. */
@Immutable
data class ProfileTabContent(
    val kind: ProfileTabKind,
    val panels: List<ProfilePanel> = emptyList(),
)

/**
 * One card of a tab and the records it holds.
 *
 * [title] comes from the metadata, as DHIS2 translates it: the tracked entity type for the
 * attributes, the program stage for its events. [target] is the program stage of a card of events
 * and null for the card of attributes.
 */
@Immutable
data class ProfilePanel(
    val title: String,
    val target: String?,
    val records: List<ProfileRecord> = emptyList(),
)

/**
 * One record inside a card: an event, or the attributes of the person.
 *
 * The values are read in the groups the program or the stage puts them in, which is the grouping
 * whoever configured the program chose.
 */
@Immutable
data class ProfileRecord(
    val id: String,
    val heading: String? = null,
    val sections: List<ProfileSection> = emptyList(),
)

/**
 * One section of a record. [title] is null for the values outside every section, and for a stage
 * or program that groups nothing at all.
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
