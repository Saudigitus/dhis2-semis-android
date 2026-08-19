package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.profile.TeiProfile

interface TeiProfileRepository {

    /**
     * Gathers the dashboard of one learner.
     *
     * [academicYear] is the label or code selected on the home screen and scopes the
     * attendance and performance history to that year; when it cannot be resolved against
     * the school calendar the whole history is kept rather than nothing.
     */
    suspend fun getProfile(
        teiUid: String,
        program: String,
        academicYear: String?,
    ): TeiProfile?
}
