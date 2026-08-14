package org.saudigitus.campaign.core.data.repository

import org.saudigitus.campaign.core.data.models.FormFieldEntity

interface TeiRepository {
    suspend fun create(orgUnit: String, program: String, fields: List<FormFieldEntity>): String?
    suspend fun update (tei: String, fields: List<FormFieldEntity>): String?
}
