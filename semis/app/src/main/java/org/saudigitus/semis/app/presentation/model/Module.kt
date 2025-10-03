package org.saudigitus.semis.app.presentation.model

import androidx.annotation.DrawableRes

data class Module(
    val title: String,
    val description: String,
    @DrawableRes val icon: Int,
    val enabled: Boolean = true,
    val route: String
)
