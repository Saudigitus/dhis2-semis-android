package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SEMISConfigItem(
    @SerialName("absenteeism")
    val absenteeism: Absenteeism?,
    @SerialName("attendance")
    val attendance: Attendance?,
    @SerialName("defaults")
    val defaults: Defaults?,
    @SerialName("filters")
    val filters: Filters?,
    @SerialName("final-result")
    val finalResult: FinalResult?,
    @SerialName("key")
    val key: String?,
    @SerialName("lastUpdate")
    val lastUpdate: String?,
    @SerialName("performance")
    val performance: Performance?,
    @SerialName("program")
    val program: String?,
    @SerialName("reenroll")
    val reenroll: Reenroll?,
    @SerialName("registration")
    val registration: Registration?,
    @SerialName("socio-economics")
    val socioEconomics: SocioEconomics?,
    @SerialName("trackedEntityType")
    val trackedEntityType: String?,
    @SerialName("transfer")
    val transfer: Transfer?
)