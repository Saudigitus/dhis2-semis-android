package org.saudigitus.campaign.core.data.repository

import org.saudigitus.campaign.core.data.models.FormFieldEntity

interface EnrollmentRepository {
    /**
     * Creates a new enrollment and its associated Tracked Entity Instance (TEI).
     *
     * This function is responsible for:
     * - Creating a Tracked Entity Instance (TEI)
     * - Enrolling the TEI into the specified program
     * - Assigning the enrollment to the given organisation unit
     * - Setting the enrollment date
     * - Persisting the provided form field values
     *
     * @param orgUnit The organisation unit UID where the enrollment will be created.
     * @param program The program UID in which the TEI will be enrolled.
     * @param date The enrollment date in ISO-8601 format (e.g., "yyyy-MM-dd").
     * @param fields A list of [FormFieldEntity] containing the field values
     *               to be saved during the enrollment creation.
     *
     * @return A [Pair] where:
     * - first: Enrollment UID
     * - second: Tracked Entity Instance (TEI) UID
     *
     * @throws Exception If the TEI or enrollment creation fails.
     */
    suspend fun create(
        orgUnit: String,
        program: String,
        date: String,
        fields: List<FormFieldEntity>,
    ): Pair<String, String>

    suspend fun update(
        enrollment: String,
        tei: String,
        date: String,
        fields: List<FormFieldEntity>,
    ): Pair<String, String>

    suspend fun getEnrollmentDate(tei: String?): Long?
}