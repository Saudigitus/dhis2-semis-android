package org.saudigitus.semis.core.data.model.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.data.model.app_config.FinalResult
import org.saudigitus.semis.core.data.model.app_config.Performance
import org.saudigitus.semis.core.data.model.app_config.ProgramStages
import org.saudigitus.semis.core.data.model.app_config.Registration
import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.model.app_config.SocioEconomics
import org.saudigitus.semis.core.data.model.app_config.Transfer

class TransferableStagesTest {

    @Test
    fun `the learner history follows the stages the configuration lists`() {
        assertEquals(
            listOf("registration", "term1", "term2", "finalResult", "socioEconomics"),
            transferableProgramStages(config()),
        )
    }

    @Test
    fun `the transfer stage stays with the school that raised the request`() {
        val stages = transferableProgramStages(config())

        assertTrue("transfer" !in stages)
    }

    @Test
    fun `a stage listed twice is only moved once`() {
        val duplicated = config(
            performance = Performance(
                enabled = true,
                lastUpdate = null,
                programStages = listOf(
                    ProgramStages(programStage = "registration"),
                    ProgramStages(programStage = "term1"),
                ),
            ),
        )

        assertEquals(
            listOf("registration", "term1", "finalResult", "socioEconomics"),
            transferableProgramStages(duplicated),
        )
    }

    @Test
    fun `sections that are absent or blank simply contribute nothing`() {
        val partial = config(
            registration = null,
            performance = Performance(
                enabled = true,
                lastUpdate = null,
                programStages = listOf(ProgramStages(programStage = " "), null),
            ),
            finalResult = FinalResult(
                enabled = null,
                lastUpdate = null,
                programStage = null,
                status = null,
            ),
        )

        assertEquals(listOf("socioEconomics"), transferableProgramStages(partial))
    }

    @Test
    fun `no configuration moves nothing rather than failing`() {
        assertEquals(emptyList<String>(), transferableProgramStages(null))
    }

    private fun config(
        registration: Registration? = Registration(
            enabled = true,
            grade = null,
            lastUpdate = null,
            programStage = "registration",
            section = null,
        ),
        performance: Performance? = Performance(
            enabled = true,
            lastUpdate = null,
            programStages = listOf(
                ProgramStages(programStage = "term1"),
                ProgramStages(programStage = "term2"),
            ),
        ),
        finalResult: FinalResult? = FinalResult(
            enabled = true,
            lastUpdate = null,
            programStage = "finalResult",
            status = null,
        ),
    ) = SEMISConfigItem(
        absenteeism = null,
        attendance = null,
        defaults = null,
        filters = null,
        finalResult = finalResult,
        key = "student",
        lastUpdate = null,
        performance = performance,
        profile = null,
        program = "program",
        reenroll = null,
        registration = registration,
        socioEconomics = SocioEconomics(programStage = "socioEconomics"),
        trackedEntityType = null,
        transfer = Transfer(
            approvedCode = null,
            destinySchool = "destination",
            enabled = true,
            lastUpdate = null,
            originSchool = null,
            pendingCode = null,
            programStage = "transfer",
            reprovedCode = null,
            status = "status",
            statusOptions = null,
        ),
    )
}
