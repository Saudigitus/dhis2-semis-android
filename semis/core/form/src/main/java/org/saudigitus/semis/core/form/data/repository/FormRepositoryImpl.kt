package org.saudigitus.semis.core.form.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.saudigitus.semis.core.data.model.OptionModel
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.core.data.repository.ProgramStageRepository
import org.saudigitus.semis.core.data.rules.RuleEngineRepository
import org.saudigitus.semis.core.form.data.model.FormFieldState
import javax.inject.Inject

class FormRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val appConfigRepository: AppConfigRepository,
    private val repository: ProgramStageRepository,
    private val optionRepository: OptionRepository,
    private val ruleEngineRepository: RuleEngineRepository
) : FormRepository {

    private suspend fun getOptionModels(
        program: String,
        dl: String,
    ): List<OptionModel> {
        return optionRepository.getOptions(program = program, dataElement = dl)
            .map {
                OptionModel(
                    uid = it.uid(),
                    code = it.code(),
                    displayName = it.displayName(),
                    sortOrder = it.sortOrder(),
                )
            }
    }

    override suspend fun getFormFields(
        program: String,
        stage: String,
        dl: String?
    ) = withContext(Dispatchers.IO) {
        val attendance = appConfigRepository.getAppConfig(program)?.attendance

        repository.getProgramStageDataElements(program, stage, dl)
            .map {
                val options = getOptionModels(program, it.dataElement()?.uid().orEmpty())
                val dataElementId = it.dataElement()?.uid().orEmpty()

                val dataElement = if (dl == null) {
                    d2.dataElementModule().dataElements()
                        .uid(dataElementId)
                        .blockingGet()
                } else null

                FormFieldState(
                    dataElementUid = dataElementId,
                    label = it.dataElement()?.displayFormName() ?: dataElement?.displayFormName().orEmpty(),
                    valueType = it.dataElement()?.valueType() ?: ValueType.TEXT,
                    optionSet = options,
                    mandatory = it.compulsory() == true,
                    isAttendanceType = attendance?.status == dataElementId
                )
            }

    }

    override suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String,
        fields: List<FormFieldState>,
    ) = withContext(Dispatchers.IO) {
        val currentFields = fields.toMutableList()
        currentFields.clear()

        for (field in fields) {
            var currentField = field

            val ruleEffects = ruleEngineRepository.evaluateDataEntry(
                ou = orgUnit,
                program = program,
                dataElement = field.dataElementUid,
                event = field.eventUid.orEmpty(),
                value = field.value.orEmpty(),
            )

            for (ruleEffect in ruleEffects) {
                when (ruleEffect.ruleAction.type) {
                    ProgramRuleActionType.HIDEFIELD.name -> {
                        currentField = currentField.copy(rendered = false)
                    }

                    ProgramRuleActionType.SHOWERROR.name -> {
                        currentField = currentField.copy(
                            hasError = true,
                            errorMessage = ruleEffect.ruleAction.content().orEmpty()
                        )
                    }

                    ProgramRuleActionType.ASSIGN.name -> {
                        currentField = currentField.copy(
                            value = ruleEffect.ruleAction.content().orEmpty()
                        )
                    }

                    ProgramRuleActionType.SETMANDATORYFIELD.name -> {
                        currentField = currentField.copy(mandatory = true)
                    }
                }
            }

            currentFields.add(currentField)
        }

        return@withContext currentFields
    }
}