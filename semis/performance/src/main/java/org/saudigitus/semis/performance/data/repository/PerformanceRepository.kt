package org.saudigitus.semis.performance.data.repository

import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.form.data.model.FormFieldData

interface PerformanceRepository {

    suspend fun savePerformance(
        orgUnit: String,
        program: String,
        programStage: String,
        teiList: List<SearchTeiModel>,
        formFieldsData: List<FormFieldData>
    )

    suspend fun getEvents(
        ou: String,
        program: String,
        programStage: String,
        dataElement: String,
        teis: List<String>,
    ) : List<FormFieldData>
}