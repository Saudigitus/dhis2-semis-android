package org.saudigitus.semis.core.data.model.transfer

import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem

/**
 * The program stages whose events follow the learner when a transfer is approved.
 *
 * A learner's record is spread over several stages, and the ones listed in the
 * configuration describe the learner rather than the school: enrollment details, marks,
 * final result and socio-economics. Leaving them behind would make reports by
 * organisation unit disagree with where the learner actually is.
 *
 * The transfer stage is deliberately absent: the request is the record of the school
 * that raised it and stays there.
 *
 * Attendance is equally absent, and by omission rather than by rule: it is not part of
 * the configured set. Attendance states that a learner was present at a school on a
 * given day, which stays true after they leave.
 */
fun transferableProgramStages(config: SEMISConfigItem?): List<String> {
    val item = config ?: return emptyList()
    val performanceStages = item.performance
        ?.programStages
        ?.mapNotNull { it?.programStage }
        .orEmpty()

    return buildList {
        add(item.registration?.programStage)
        addAll(performanceStages)
        add(item.finalResult?.programStage)
        add(item.socioEconomics?.programStage)
    }
        .filterNotNull()
        .filter { it.isNotBlank() }
        .filterNot { it == item.transfer?.programStage }
        .distinct()
}
