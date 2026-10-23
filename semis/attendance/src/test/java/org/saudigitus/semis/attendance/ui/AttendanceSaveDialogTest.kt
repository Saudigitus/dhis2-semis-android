package org.saudigitus.semis.attendance.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.saudigitus.semis.attendance.ui.model.BottomSheetConfirmAction
import org.saudigitus.semis.attendance.ui.model.BottomSheetType
import org.saudigitus.semis.attendance.ui.repository.AttendanceRepository
import org.saudigitus.semis.core.data.model.app_config.Attendance
import org.saudigitus.semis.core.data.model.app_config.AttendanceStatus
import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.form.data.repository.FormRepository

/**
 * The confirmations attendance asks for before saving: each one can be stepped back from, and
 * stepping back saves nothing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceSaveDialogTest {

    private val formRepository: FormRepository = mock()
    private val attendanceRepository: AttendanceRepository = mock()
    private val appConfigRepository: AppConfigRepository = mock()
    private val resourceManager = mock<org.dhis2.commons.resources.ResourceManager>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        formRepository.stub {
            on { attendanceButtonStateFlow } doReturn MutableStateFlow(AttendanceButtonState())
            onBlocking {
                loadAttendanceEvents(any(), any(), any(), any(), any(), any(), anyOrNull())
            } doReturn AttendanceButtonState()
            onBlocking { deleteAttendance(any()) } doReturn AttendanceButtonState()
        }
        resourceManager.stub {
            on { getString(any()) } doReturn "message"
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun attendanceConfig(allowStatus: Boolean) = Attendance(
        absenceReason = "reason",
        attendanceStatus = AttendanceStatus(
            allowAttendanceStatus = allowStatus,
            program = "status-program",
            programStage = "status-stage",
            totalRecords = "de_total",
        ),
        enabled = true,
        lastUpdate = null,
        programStage = "attendance-stage",
        status = "de_status",
        statusOptions = emptyList(),
    )

    private suspend fun viewModel(allowStatus: Boolean): AttendanceViewModel {
        appConfigRepository.stub {
            onBlocking { getSchoolCalendar() } doReturn null
            onBlocking { getAppConfig(any()) } doReturn SEMISConfigItem(
                absenteeism = null,
                attendance = attendanceConfig(allowStatus),
                defaults = null,
                filters = null,
                finalResult = null,
                key = null,
                lastUpdate = null,
                performance = null,
                program = "program",
                reenroll = null,
                registration = null,
                socioEconomics = null,
                trackedEntityType = null,
                transfer = null,
            )
            onBlocking { allowedCalenderYearDates(any(), any(), anyOrNull()) } doReturn true
        }

        return AttendanceViewModel(
            formRepository = formRepository,
            attendanceRepository = attendanceRepository,
            appConfigRepository = appConfigRepository,
            resourceManager = resourceManager,
        ).also {
            it.initialize(
                program = "program",
                orgUnit = "orgUnit",
                teis = emptyList(),
                filterDetailsState = org.saudigitus.semis.core.designsystem.components
                    .FilterDetailsState(),
            )
        }
    }

    @Test
    fun `cancelling the save confirmation closes it and saves nothing`() = runTest {
        val viewModel = viewModel(allowStatus = false)

        viewModel.handleUiEvent(AttendanceUiEvent.ShowBottomSheet(BottomSheetType.SUMMARY))
        assertTrue(viewModel.uiState.value.displayDialog)

        viewModel.handleUiEvent(AttendanceUiEvent.DismissSaveDialog)

        assertFalse(viewModel.uiState.value.displayDialog)
        verify(formRepository, never()).saveAttendance(any(), any(), any(), any(), any())
    }

    @Test
    fun `confirming closes the save confirmation`() = runTest {
        val viewModel = viewModel(allowStatus = false)

        viewModel.handleUiEvent(AttendanceUiEvent.ShowBottomSheet(BottomSheetType.SUMMARY))
        viewModel.handleUiEvent(
            AttendanceUiEvent.BottomSheetAction(BottomSheetConfirmAction.PERFORM_SAVE),
        )

        assertFalse(viewModel.uiState.value.displayDialog)
    }

    @Test
    fun `cancelling the class wide override closes it`() = runTest {
        val viewModel = viewModel(allowStatus = false)

        viewModel.handleUiEvent(AttendanceUiEvent.DismissBulkOverride)

        assertFalse(viewModel.uiState.value.overrideBulk)
    }
}
