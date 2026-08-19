package org.saudigitus.semis.core.data.model.profile

import java.util.Date

/**
 * One socio-economic record captured for a learner, with the data values it holds.
 */
data class SocioEconomicRecord(
    val eventUid: String,
    val occurredAt: Date?,
    val orgUnitName: String,
    val isActive: Boolean,
    val details: List<ProfileAttribute>,
)
