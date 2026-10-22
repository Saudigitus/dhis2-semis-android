package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.profile.PersonProfile

interface TeiProfileRepository {

    /**
     * Builds the page of one person for [academicYear], the year chosen in the home filters.
     *
     * The records are those of the enrollment of that year. Returns null when the person is not
     * on the device.
     */
    suspend fun getProfile(
        teiUid: String,
        program: String,
        academicYear: String?,
    ): PersonProfile?
}
