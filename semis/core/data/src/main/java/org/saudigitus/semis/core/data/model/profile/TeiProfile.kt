package org.saudigitus.semis.core.data.model.profile

/**
 * Everything the learner dashboard shows, gathered for the filters the home screen is on.
 */
data class TeiProfile(
    val teiUid: String,
    val name: String,
    val systemId: String,
    val identity: List<ProfileAttribute> = emptyList(),
    val socioEconomics: List<SocioEconomicRecord> = emptyList(),
    val attendance: AttendanceHistory = AttendanceHistory(),
    val performance: List<SubjectPerformance> = emptyList(),
)
