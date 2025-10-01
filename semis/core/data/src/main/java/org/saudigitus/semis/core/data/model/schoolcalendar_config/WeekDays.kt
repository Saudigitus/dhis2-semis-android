package org.saudigitus.semis.core.data.model.schoolcalendar_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeekDays(
    @SerialName("friday")
    val friday: Boolean?,
    @SerialName("monday")
    val monday: Boolean?,
    @SerialName("saturday")
    val saturday: Boolean?,
    @SerialName("sunday")
    val sunday: Boolean?,
    @SerialName("thursday")
    val thursday: Boolean?,
    @SerialName("tuesday")
    val tuesday: Boolean?,
    @SerialName("wednesday")
    val wednesday: Boolean?
)