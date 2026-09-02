package org.saudigitus.semis.core.data.model.app_config

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ProgramStages(
    @SerializedName("programStage")
    val programStage: String?
)