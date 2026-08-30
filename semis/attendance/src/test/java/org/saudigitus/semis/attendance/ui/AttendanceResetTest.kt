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
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.saudigitus.semis.attendance.ui.repository.AttendanceRepository
import org.saudigitus.semis.core.data.model.app_config.Attendance
import org.saudigitus.semis.core.data.model.app_config.AttendanceStatus
import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.form.data.repository.FormRepository

/**
 * Covers the two configurations the app runs with: the attendance status flow enabled,
 * where being present means holding no record, and disabled, where every learner does.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceResetTest {

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
                profile = null,
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
    fun `resetting asks for confirmation before anything is deleted`() = runTest {
        val viewModel = viewModel(allowStatus = true)

        viewModel.handleUiEvent(AttendanceUiEvent.ResetForm)

        assert(viewModel.uiState.value.displayResetDialog)
        verify(formRepository, never()).deleteAttendance(any())
        verify(attendanceRepository, never()).updateAttendanceStatusSummary(
            orgUnit = any(),
            program = any(),
            date = any(),
            filterDetailsState = any(),
            totalLearners = any(),
            attendanceEvents = any(),
        )
    }

    @Test
    fun `dismissing the warning leaves the recorded attendance alone`() = runTest {
        val viewModel = viewModel(allowStatus = true)

        viewModel.handleUiEvent(AttendanceUiEvent.ResetForm)
        viewModel.handleUiEvent(AttendanceUiEvent.DismissResetDialog)

        assertFalse(viewModel.uiState.value.displayResetDialog)
        verify(formRepository, never()).deleteAttendance(any())
    }

    @Test
    fun `with the status flow on only the coded records go and the summary is rewritten`() =
        runTest {
            val viewModel = viewModel(allowStatus = true)

            viewModel.handleUiEvent(AttendanceUiEvent.ConfirmResetForm)

            verify(formRepository).deleteAttendance(absencesOnly = eq(true))
            verify(attendanceRepository).updateAttendanceStatusSummary(
                orgUnit = any(),
                program = any(),
                date = any(),
                filterDetailsState = any(),
                totalLearners = any(),
                attendanceEvents = any(),
            )
        }

    @Test
    fun `without the status flow the whole date is cleared and no summary is written`() =
        runTest {
            val viewModel = viewModel(allowStatus = false)

            viewModel.handleUiEvent(AttendanceUiEvent.ConfirmResetForm)

            verify(formRepository).deleteAttendance(absencesOnly = eq(false))
            verify(attendanceRepository, never()).updateAttendanceStatusSummary(
                orgUnit = any(),
                program = any(),
                date = any(),
                filterDetailsState = any(),
                totalLearners = any(),
                attendanceEvents = any(),
            )
        }

    @Test
    fun `the attendance status event is never deleted by a reset`() = runTest {
        val viewModel = viewModel(allowStatus = true)

        viewModel.handleUiEvent(AttendanceUiEvent.ConfirmResetForm)

        verify(attendanceRepository, never()).createAttendanceStatus(any(), any(), any(), any())
        verify(attendanceRepository, never()).completeAttendanceStatus(
            orgUnit = any(),
            program = any(),
            date = any(),
            filterDetailsState = any(),
            totalLearners = any(),
            attendanceEvents = any(),
        )
    }
}
