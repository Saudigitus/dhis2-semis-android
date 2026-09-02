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
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.saudigitus.semis.core.data.model.SyncTarget
import org.saudigitus.semis.core.data.model.app_config.SyncMode
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.SyncOutcome
import org.saudigitus.semis.core.data.repository.SyncRepository
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

    @Inject
    lateinit var appConfigRepository: AppConfigRepository

    @Inject
    lateinit var syncRepository: SyncRepository

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
                    syncNow = ::syncNow,
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
        lifecycleScope.launch {
            when (appConfigRepository.getSyncMode()) {
                SyncMode.DEFAULT -> Unit
                SyncMode.PROMPT -> promptToSync(targets)
                SyncMode.AUTO -> sendInBackground(targets)
            }
        }
    }

    /**
     * Sends because the user pressed the button that says so.
     *
     * The configured sync mode governs what happens on its own after a save, never what happens
     * when someone asks for it: a deployment that chose to be left alone after saving still has a
     * button on the toolbar, and a button that decides for itself not to do anything is worse than
     * no button at all.
     */
    private fun syncNow() = promptToSync(emptyList())

    /**
     * Sends what was captured without asking, and says how it went when it is done.
     *
     * The upload is started on a scope that outlives this screen, because leaving straight after
     * saving is the ordinary thing to do and must not be what cancels the sending. Whatever does
     * not go now stays where it is, for the periodic sync or the manual button to carry.
     */
    private fun sendInBackground(targets: List<SyncTarget>) {
        val appContext = applicationContext

        uploadScope.launch {
            val outcome = syncRepository.upload(
                targets.ifEmpty { listOf(SyncTarget.Tracker(program)) },
            )

            val message = when (outcome) {
                SyncOutcome.SENT -> R.string.sync_sent
                SyncOutcome.OFFLINE -> R.string.sync_saved_offline
                SyncOutcome.FAILED -> R.string.sync_saved_not_sent
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(appContext, appContext.getString(message), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun promptToSync(targets: List<SyncTarget>) {
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

/**
 * Where an upload started from a capture screen runs.
 *
 * Deliberately not tied to a screen or to an activity: the teacher saves and leaves, and the
 * sending has to survive that. It lives as long as the process does, which for one small upload
 * is exactly as long as it needs to.
 */
private val uploadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
