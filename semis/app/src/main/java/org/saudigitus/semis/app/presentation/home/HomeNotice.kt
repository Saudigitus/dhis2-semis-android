package org.saudigitus.semis.app.presentation.home

/**
 * What the home screen has to tell the user about the class they picked.
 *
 * An incomplete selection and a class without learners both leave the listing empty, but they ask
 * different things of the user: one is missing a choice, the other is missing data. Keeping them
 * apart is what lets the screen say which of the two it is.
 */
enum class HomeNotice {
    /** The class is chosen and holds learners, so there is nothing to report. */
    NONE,

    /** The user has still to pick the school, the academic year or one of the configured filters. */
    SELECT_FILTERS,

    /** The selection is complete but nothing has been downloaded or enrolled under it yet. */
    NO_DATA,
}
