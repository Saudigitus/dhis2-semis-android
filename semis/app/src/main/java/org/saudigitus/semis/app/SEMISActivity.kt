package org.saudigitus.semis.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import org.saudigitus.semis.app.presentation.home.HomeScreen
import org.saudigitus.semis.core.designsystem.theme.SEMISTheme

@AndroidEntryPoint
class SEMISActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SEMISTheme(
                dynamicColor = false,
                darkTheme = false
            ) {
                HomeScreen()
            }
        }
    }
}