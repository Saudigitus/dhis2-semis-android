package org.saudigitus.campaign.core.data.rules

import com.formrules.FormRules
import com.formrules.engine.DataContext
import com.formrules.model.RuleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.saudigitus.campaign.core.data.repository.DatastoreRepository
import org.saudigitus.campaign.core.data.utils.toJson

class CustomRulesRepository(
    private val d2: D2,
    private val datastoreRepository: DatastoreRepository
) {
    suspend fun evaluateDashboard(
        program: String,
        tei: String
    ) = withContext(Dispatchers.IO) {
        val formValidationJson = datastoreRepository.getFormValidations(program)
            .toJson() ?: return@withContext null

        try {
            val formValidationRules = FormRules.fromJson(formValidationJson)
            val context = DataContext(
                attributes = d2.trackedEntityModule().trackedEntityAttributeValues()
                    .byTrackedEntityInstance().eq(tei)
                    .blockingGet()
                    .associate { it.trackedEntityAttribute().orEmpty() to it.value() }
            )

            formValidationRules.evaluate(context, RuleType.TEI_DASHBOARD)
        } catch (_: Exception) {
            null
        }
    }
}