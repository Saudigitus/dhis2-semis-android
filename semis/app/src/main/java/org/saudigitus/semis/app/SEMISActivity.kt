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
import org.saudigitus.semis.core.data.model.SyncTarget
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
                    activity = this,
                    viewModel = viewModel,
                    teiCardMapper = teiCardMapper,
                    navController = navController,
                    navBack = { finish() },
                    syncData = ::syncTargets,
                    displayImageDetail = ::displayImageDetail,
                )
            }
        }
    }

    /**
     * Offers to send what a screen has just written, in a single question.
     *
     * A screen can have written into more than one program: capturing attendance writes both the
     * learner records and the class summary, which is a program of its own, and sending only the
     * first is what leaves a summary the server never sees. Rather than ask once per program, a
     * screen that touched several is offered everything the device has pending, because being
     * asked twice for one action reads as the app not knowing what it did.
     *
     * A screen that names nothing falls back to the program being worked on.
     */
    private fun syncTargets(targets: List<SyncTarget>) {
        val distinct = targets.distinctBy { it.program }
        val context = when {
            distinct.size > 1 -> SyncContext.Global()
            else -> when (val target = distinct.firstOrNull() ?: SyncTarget.Tracker(program)) {
                is SyncTarget.Tracker -> SyncContext.TrackerProgram(target.program)
                is SyncTarget.Events -> SyncContext.EventProgram(target.program)
            }
        }

        SyncDialog(
            activity = this@SEMISActivity,
            recordUid = context.recordUid(),
            syncContext = context,
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