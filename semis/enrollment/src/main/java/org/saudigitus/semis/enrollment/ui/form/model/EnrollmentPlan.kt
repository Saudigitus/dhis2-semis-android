package org.saudigitus.semis.enrollment.ui.form.model

import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem

/**
 * One stretch of the enrollment the user fills in before anything is written.
 *
 * The steps are what the wizard walks through. Splitting them out as a type, rather than carrying a
 * list of program stage uids, is what lets the first step stand for the tracked entity attributes,
 * which have no stage of their own yet occupy a step like any other.
 */
sealed interface EnrollmentStep {

    /** The attributes of the learner, always captured first. */
    data object Attributes : EnrollmentStep

    /** A program stage the configuration asks the user to fill in during enrollment. */
    data class Stage(val programStage: String) : EnrollmentStep
}

/**
 * Everything the enrollment has to produce, worked out before the first field is shown.
 *
 * Resolving this upfront is what allows the whole enrollment to be committed in one go at the end:
 * the wizard knows how many steps there are before it starts, so it can collect them all and write
 * once, instead of persisting each form as it is completed and leaving a half made learner behind
 * whenever the user stops midway.
 */
data class EnrollmentPlan(
    val steps: List<EnrollmentStep> = emptyList(),
    val backgroundStages: List<String> = emptyList(),
) {
    /** Number of steps the user walks through, which the progress indicator reports. */
    val stepCount: Int get() = steps.size
}

/**
 * Works out the enrollment plan from [config].
 *
 * The stages the user fills in come from the registration and socio economics sections, in that
 * order, while the stages that only need an empty event come from performance and final result.
 *
 * The `enabled` flag of each section is deliberately not consulted: every mapped stage produces its
 * event whether or not its section is marked as enabled. That is the agreed behaviour and reading
 * the flag here would silently stop events from being created.
 */
internal fun enrollmentPlan(config: SEMISConfigItem?): EnrollmentPlan {
    val interactiveStages = listOfNotNull(
        config?.registration?.programStage.cleanUid(),
        config?.socioEconomics?.programStage.cleanUid(),
    ).distinct()

    val backgroundStages = buildList {
        config?.performance?.programStages.orEmpty().forEach { stage ->
            stage?.programStage.cleanUid()?.let(::add)
        }
        config?.finalResult?.programStage.cleanUid()?.let(::add)
    }.distinct()

    return EnrollmentPlan(
        steps = buildList {
            add(EnrollmentStep.Attributes)
            interactiveStages.forEach { add(EnrollmentStep.Stage(it)) }
        },
        backgroundStages = backgroundStages,
    )
}

/**
 * A uid that is blank, or only whitespace, means the section was left unconfigured rather than
 * pointing at a stage, so it is dropped instead of producing an unusable step.
 */
private fun String?.cleanUid(): String? = this?.trim()?.takeIf(String::isNotEmpty)
