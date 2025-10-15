package org.saudigitus.semis.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.dhis2.commons.Constants
import org.dhis2.commons.dialogs.imagedetail.ImageDetailActivity
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.sync.SyncDialog
import org.saudigitus.semis.app.presentation.home.HomeViewModel
import org.saudigitus.semis.core.designsystem.theme.SEMISTheme
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import javax.inject.Inject

@AndroidEntryPoint
class SEMISActivity : FragmentActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var program: String

    @Inject
    lateinit var teiCardMapper: TEICardMapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SEMISTheme(
                dynamicColor = false,
                darkTheme = false
            ) {
                program = intent?.extras?.getString(Constants.PROGRAM_UID).orEmpty()
                val programName = intent?.extras?.getString(Constants.DATA_SET_NAME).orEmpty()
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    viewModel.initialize(program, programName)
                }

                AppNavGraph(
                    viewModel = viewModel,
                    teiCardMapper = teiCardMapper,
                    navController = navController,
                    navBack = { finish() },
                    syncData = ::syncProgram,
                    displayImageDetail = ::displayImageDetail,
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

    private fun displayImageDetail(imagePath: String) {
        val intent = ImageDetailActivity.intent(
            context = this,
            title = null,
            imagePath = imagePath,
        )

        startActivity(intent)
    }
}