package org.saudigitus.semis.core.data.model

/**
 * A program whose records a screen has just changed and that therefore has something to send.
 *
 * A screen states what it touched rather than asking for a sync of everything: capturing
 * attendance writes into two programs at once, the learner records and the class summary, and a
 * teacher who only ever hears about the first is left with a summary the server never sees.
 */
sealed interface SyncTarget {

    val program: String

    /** A program that registers people, where the records hang off an enrollment. */
    data class Tracker(override val program: String) : SyncTarget

    /** A program of standalone events, such as the one the class summary is written to. */
    data class Events(override val program: String) : SyncTarget
}
