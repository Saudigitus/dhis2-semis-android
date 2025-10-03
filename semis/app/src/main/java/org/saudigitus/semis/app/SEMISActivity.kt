package org.saudigitus.semis.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.dhis2.commons.Constants
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.sync.SyncDialog
import org.saudigitus.semis.app.presentation.AppScreen
import org.saudigitus.semis.app.presentation.home.HomeViewModel
import org.saudigitus.semis.core.designsystem.theme.SEMISTheme

@AndroidEntryPoint
class SEMISActivity : FragmentActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var program: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SEMISTheme(
                dynamicColor = false,
                darkTheme = false
            ) {
                program = intent?.extras?.getString(Constants.PROGRAM_UID).orEmpty()

                LaunchedEffect(Unit) {
                    viewModel.initialize(program)
                }

                AppScreen(
                    viewModel = viewModel,
                    navBack = { finish() },
                    syncData = ::syncProgram
                )
            }
        }
    }

    private fun syncProgram() {
        SyncDialog(
            activity = this@SEMISActivity,
            recordUid = program,
            syncContext = SyncContext.TrackerProgram(program),
            onNoConnectionListener = {
                Snackbar.make(
                    this.window.decorView.rootView,
                    getString(R.string.sync_offline_check_connection),
                    Snackbar.LENGTH_SHORT,
                ).show()
            },
        ).show()
    }
}