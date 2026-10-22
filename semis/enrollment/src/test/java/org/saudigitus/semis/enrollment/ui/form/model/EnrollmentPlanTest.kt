package org.saudigitus.semis.enrollment.ui.form.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.data.model.app_config.FinalResult
import org.saudigitus.semis.core.data.model.app_config.Performance
import org.saudigitus.semis.core.data.model.app_config.ProgramStages
import org.saudigitus.semis.core.data.model.app_config.Registration
import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.model.app_config.SocioEconomics

class EnrollmentPlanTest {

    private fun config(
        registrationStage: String? = null,
        socioEconomicsStage: String? = null,
        performanceStages: List<String?> = emptyList(),
        finalResultStage: String? = null,
        enabled: Boolean? = true,
    ) = SEMISConfigItem(
        absenteeism = null,
        attendance = null,
        defaults = null,
        filters = null,
        finalResult = FinalResult(
            enabled = enabled,
            lastUpdate = null,
            programStage = finalResultStage,
            status = null,
        ),
        key = null,
        lastUpdate = null,
        performance = Performance(
            enabled = enabled,
            lastUpdate = null,
            programStages = performanceStages.map { ProgramStages(it) },
        ),
        program = null,
        reenroll = null,
        registration = Registration(
            enabled = enabled,
            grade = null,
            lastUpdate = null,
            programStage = registrationStage,
            section = null,
        ),
        socioEconomics = SocioEconomics(programStage = socioEconomicsStage),
        trackedEntityType = null,
        transfer = null,
    )

    @Test
    fun `the attributes are always the first step`() {
        val plan = enrollmentPlan(config())

        assertEquals(listOf(EnrollmentStep.Attributes), plan.steps)
    }

    @Test
    fun `a configuration without any section still asks for the attributes`() {
        val plan = enrollmentPlan(null)

        assertEquals(listOf(EnrollmentStep.Attributes), plan.steps)
        assertTrue(plan.backgroundStages.isEmpty())
    }

    @Test
    fun `the configured stages follow the attributes in registration then socio economics order`() {
        val plan = enrollmentPlan(
            config(registrationStage = "regStage", socioEconomicsStage = "socioStage"),
        )

        assertEquals(
            listOf(
                EnrollmentStep.Attributes,
                EnrollmentStep.Stage("regStage"),
                EnrollmentStep.Stage("socioStage"),
            ),
            plan.steps,
        )
        assertEquals(3, plan.stepCount)
    }

    @Test
    fun `an unconfigured section adds no step`() {
        val plan = enrollmentPlan(config(socioEconomicsStage = "socioStage"))

        assertEquals(
            listOf(EnrollmentStep.Attributes, EnrollmentStep.Stage("socioStage")),
            plan.steps,
        )
    }

    @Test
    fun `a blank stage uid is treated as unconfigured`() {
        val plan = enrollmentPlan(config(registrationStage = "   ", socioEconomicsStage = ""))

        assertEquals(listOf(EnrollmentStep.Attributes), plan.steps)
    }

    @Test
    fun `a stage uid keeps working when the configuration pads it with whitespace`() {
        val plan = enrollmentPlan(config(registrationStage = "  regStage  "))

        assertEquals(
            listOf(EnrollmentStep.Attributes, EnrollmentStep.Stage("regStage")),
            plan.steps,
        )
    }

    @Test
    fun `the same stage configured twice produces a single step`() {
        val plan = enrollmentPlan(
            config(registrationStage = "sameStage", socioEconomicsStage = "sameStage"),
        )

        assertEquals(
            listOf(EnrollmentStep.Attributes, EnrollmentStep.Stage("sameStage")),
            plan.steps,
        )
    }

    @Test
    fun `performance and final result stages are created without asking the user`() {
        val plan = enrollmentPlan(
            config(
                performanceStages = listOf("term1", "term2"),
                finalResultStage = "finalStage",
            ),
        )

        assertEquals(listOf(EnrollmentStep.Attributes), plan.steps)
        assertEquals(listOf("term1", "term2", "finalStage"), plan.backgroundStages)
    }

    @Test
    fun `a background stage listed twice is created once`() {
        val plan = enrollmentPlan(
            config(performanceStages = listOf("term1", "term1"), finalResultStage = "term1"),
        )

        assertEquals(listOf("term1"), plan.backgroundStages)
    }

    @Test
    fun `every mapped stage is planned even when its section is disabled`() {
        val plan = enrollmentPlan(
            config(
                registrationStage = "regStage",
                performanceStages = listOf("term1"),
                finalResultStage = "finalStage",
                enabled = false,
            ),
        )

        assertEquals(
            listOf(EnrollmentStep.Attributes, EnrollmentStep.Stage("regStage")),
            plan.steps,
        )
        assertEquals(listOf("term1", "finalStage"), plan.backgroundStages)
    }
}
